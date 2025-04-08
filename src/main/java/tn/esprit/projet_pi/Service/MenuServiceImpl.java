package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projet_pi.Repository.MenuRepository;
import tn.esprit.projet_pi.Repository.PlatRepository;
import tn.esprit.projet_pi.Repository.RegimeAlimentaireRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    private static final Logger LOGGER = Logger.getLogger(MenuServiceImpl.class.getName());

    private final PlatRepository platRepository;
    private final MenuRepository menuRepository;
    private final UserRepo userRepository;
    private final RegimeAlimentaireRepository regimeAlimentaireRepository;
    private final EmailService emailService;

    @Autowired
    private SmsService twilioService;



    @Autowired
    public MenuServiceImpl(
            PlatRepository platRepository,
            MenuRepository menuRepository,
            UserRepo userRepository,
            RegimeAlimentaireRepository regimeAlimentaireRepository,
            EmailService emailService) {
        this.platRepository = platRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.regimeAlimentaireRepository = regimeAlimentaireRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void generateWeeklyMenus(Long userId) {
        try {
            User user = userRepository.findByidUser(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSunday = nextMonday.plusDays(6);

            menuRepository.deleteByIsValidatedFalseAndDateBetween(nextMonday, nextSunday);

            List<RegimeAlimentaire> regimes = regimeAlimentaireRepository.findAll();
            if (regimes.isEmpty()) {
                LOGGER.warning("⚠️ Aucun régime alimentaire trouvé dans la base de données");
                return;
            }

            Map<RegimeAlimentaireType, RegimeAlimentaire> regimeMap = regimes.stream()
                    .collect(Collectors.toMap(RegimeAlimentaire::getType, regime -> regime));

            for (int i = 0; i < 7; i++) {
                LocalDate date = nextMonday.plusDays(i);
                for (RegimeAlimentaireType regimeType : RegimeAlimentaireType.values()) {
                    RegimeAlimentaire regime = regimeMap.get(regimeType);
                    if (regime == null) {
                        LOGGER.warning("⚠️ Régime non trouvé pour le type: " + regimeType);
                        continue;
                    }

                    if (menuRepository.existsByDateAndRegime(date, regimeType)) {
                        LOGGER.info("ℹ️ Menu déjà existant pour " + date + " - " + regimeType);
                        continue;
                    }

                    List<Plat> platsDuJour = generateCompleteMenuForRegime(regime, date);
                    if (platsDuJour.isEmpty()) {
                        LOGGER.warning("⚠️ Aucun plat disponible pour " + regimeType + " à la date " + date);
                        continue;
                    }

                    Menu menu = new Menu();
                    menu.setDate(date);
                    menu.setRegime(regimeType);
                    menu.setPlats(platsDuJour);
                    menu.setCreatedBy(user);
                    menu.setIsValidated(false);
                    menu.calculateTotalCalories();

                    menuRepository.save(menu);
                    LOGGER.info("✅ Menu enregistré pour " + date + " - Régime: " + regimeType +
                            " - Calories: " + menu.getTotalCalories());
                }
            }

            // Envoi des SMS aux médecins
            List<User> doctors = userRepository.findByRole(Role.Medecin);
            LOGGER.info("Nombre de médecins trouvés: " + doctors.size());
            if (!doctors.isEmpty()) {
                String smsMessage = "Les menus de la semaine prochaine ont été générés. Veuillez les valider.";
                for (User doctor : doctors) {
                    LOGGER.info("Vérification du médecin ID: " + doctor.getIdUser() + ", Téléphone: " + doctor.getTelephone());
                    if (doctor.getTelephone() != null && !doctor.getTelephone().isEmpty()) {
                        String phone = doctor.getTelephone().startsWith("+") ? doctor.getTelephone() : "+216" + doctor.getTelephone();
                        try {
                            LOGGER.info("Tentative d'envoi SMS à: " + phone);
                            twilioService.sendSms(phone, smsMessage);
                            LOGGER.info("📱 SMS envoyé au médecin ID: " + doctor.getIdUser() + " - Numéro: " + phone);
                        } catch (Exception e) {
                            LOGGER.severe("❌ Erreur lors de l'envoi du SMS au médecin ID: " + doctor.getIdUser() + " - " + e.getMessage());
                        }
                    } else {
                        LOGGER.warning("⚠️ Numéro de téléphone manquant pour le médecin ID: " + doctor.getIdUser());
                    }
                }
            } else {
                LOGGER.warning("⚠️ Aucun médecin trouvé pour recevoir le SMS");
            }
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la génération des menus : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la génération des menus", e);
        }
    }


    private List<Plat> generateCompleteMenuForRegime(RegimeAlimentaire regimeAlimentaire, LocalDate date) {
        List<Plat> plats = new ArrayList<>();
        Set<Plat> usedPlats = new HashSet<>();

        LOGGER.info("🔍 Génération du menu pour le régime : " + regimeAlimentaire.getType());

        Plat entree = getPlatByCategorie(regimeAlimentaire, CategoriePlat.ENTREE, usedPlats);
        Plat platPrincipal = getPlatByCategorie(regimeAlimentaire, CategoriePlat.PLAT_PRINCIPAL, usedPlats);
        Plat dessert = getPlatByCategorie(regimeAlimentaire, CategoriePlat.DESSERT, usedPlats);

        if (entree != null) plats.add(entree);
        if (platPrincipal != null) plats.add(platPrincipal);
        if (dessert != null) plats.add(dessert);

        return plats;
    }

    private Plat getPlatByCategorie(RegimeAlimentaire regimeAlimentaire, CategoriePlat categorie, Set<Plat> usedPlats) {
        List<Plat> plats = regimeAlimentaire.getPlatsRecommandes()
                .stream()
                .filter(plat -> plat.getCategorie() == categorie)
                .filter(plat -> !usedPlats.contains(plat))
                .collect(Collectors.toList());

        if (plats.isEmpty()) {
            LOGGER.warning("⚠️ Aucun plat disponible pour " + categorie + " dans le régime " + regimeAlimentaire.getType());
            return null;
        }

        Plat plat = plats.get(new Random().nextInt(plats.size()));
        usedPlats.add(plat);
        return plat;
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findByOrderByDateAscIdAsc();
    }

    @Override
    public List<Menu> getValidatedMenus() {
        return menuRepository.findByIsValidatedTrue();
    }

    @Override
    @Transactional
    public void validateMenus(Long doctorId, List<Long> menuIds) {
        User doctor = userRepository.findByidUser(doctorId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        if (!doctor.getRole().equals(Role.Medecin)) {
            throw new RuntimeException("Seuls les médecins peuvent valider les menus");
        }

        List<Menu> menusToValidate = menuRepository.findAllById(menuIds);
        if (menusToValidate.isEmpty()) {
            LOGGER.warning("⚠️ Aucun menu trouvé pour les IDs fournis");
            return;
        }

        // Validate the requested menus
        menusToValidate.forEach(menu -> {
            menu.setIsValidated(true);
            menu.setValidatedBy(doctor);
        });

        menuRepository.saveAll(menusToValidate);
        LOGGER.info("✅ " + menusToValidate.size() + " menus validés par le médecin ID: " + doctorId);

        // Check if all menus for the week are now validated
        LocalDate monday = menusToValidate.get(0).getDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        List<Menu> weekMenus = menuRepository.findByDateBetween(monday, sunday);
        boolean allValidated = weekMenus.stream().allMatch(Menu::getIsValidated);

        if (allValidated && !weekMenus.isEmpty()) {
            try {
                List<User> users = userRepository.findAll().stream()
                        .filter(user -> user.getRole() == Role.User)
                        .collect(Collectors.toList());

                if (!users.isEmpty()) {
                    emailService.sendMenuValidatedNotification(users, weekMenus);
                    LOGGER.info("📧 Email de notification envoyé à " + users.size() + " utilisateurs");
                } else {
                    LOGGER.warning("⚠️ Aucun utilisateur avec le rôle USER trouvé pour la notification");
                }
            } catch (Exception e) {
                LOGGER.severe("❌ Erreur lors de l'envoi de la notification email : " + e.getMessage());
            }
        } else {
            LOGGER.info("ℹ️ Notification email non envoyée - tous les menus de la semaine ne sont pas encore validés");
        }
    }
    @Override
    @Transactional
    public void rejectMenus(Long doctorId, List<Long> menuIds, String rejectionReason) {
        User doctor = userRepository.findByidUser(doctorId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        if (!doctor.getRole().equals(Role.Medecin)) {
            throw new RuntimeException("Seuls les médecins peuvent rejeter les menus");
        }

        List<Menu> menusToReject = menuRepository.findAllById(menuIds);
        if (menusToReject.isEmpty()) {
            LOGGER.warning("⚠️ Aucun menu trouvé pour les IDs fournis");
            return;
        }

        menusToReject.forEach(menu -> {
            if (menu.getIsValidated()) {
                LOGGER.warning("⚠️ Le menu ID " + menu.getId() + " est déjà validé et ne peut pas être rejeté");
                throw new RuntimeException("Le menu ID " + menu.getId() + " est déjà validé");
            }

            menu.setIsValidated(false);
            menu.setValidatedBy(null);
        });

        menuRepository.saveAll(menusToReject);
        LOGGER.info("❌ " + menusToReject.size() + " menus rejetés par le médecin ID: " + doctorId);

        List<User> staffUsers = userRepository.findByRole(Role.Staff);
        emailService.sendMenuRejectionNotification(staffUsers, menusToReject, rejectionReason);
    }


    @Override
    @Transactional
    public void regenerateWeeklyMenus(Long staffId) {
        User user = userRepository.findByidUser(staffId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Menu> nonValidatedMenus = menuRepository.findByIsValidatedFalse();
        if (nonValidatedMenus.isEmpty()) {
            LOGGER.info("ℹ️ Aucun menu non validé à régénérer");
            return;
        }

        LOGGER.info("🔄 Suppression de " + nonValidatedMenus.size() + " menus non validés");
        menuRepository.deleteAll(nonValidatedMenus);

        Map<LocalDate, Set<RegimeAlimentaireType>> menusToRegenerate = new HashMap<>();
        nonValidatedMenus.forEach(menu ->
                menusToRegenerate.computeIfAbsent(menu.getDate(), k -> new HashSet<>())
                        .add(menu.getRegime())
        );

        List<RegimeAlimentaire> regimes = regimeAlimentaireRepository.findAll();
        Map<RegimeAlimentaireType, RegimeAlimentaire> regimeMap = regimes.stream()
                .collect(Collectors.toMap(RegimeAlimentaire::getType, regime -> regime));

        for (Map.Entry<LocalDate, Set<RegimeAlimentaireType>> entry : menusToRegenerate.entrySet()) {
            LocalDate date = entry.getKey();
            for (RegimeAlimentaireType regimeType : entry.getValue()) {
                RegimeAlimentaire regime = regimeMap.get(regimeType);
                if (regime == null) {
                    LOGGER.warning("⚠️ Régime non trouvé pour " + regimeType);
                    continue;
                }

                List<Plat> platsDuJour = generateCompleteMenuForRegime(regime, date);
                if (platsDuJour.isEmpty()) {
                    LOGGER.warning("⚠️ Aucun plat disponible pour " + regimeType + " à la date " + date);
                    continue;
                }

                Menu menu = new Menu();
                menu.setDate(date);
                menu.setRegime(regimeType);
                menu.setPlats(platsDuJour);
                menu.setCreatedBy(user);
                menu.setIsValidated(false);
                menu.calculateTotalCalories();

                menuRepository.save(menu);
                LOGGER.info("✅ Menu régénéré pour " + date + " - Régime: " + regimeType +
                        " - Calories: " + menu.getTotalCalories());
            }
        }
    }
    //"0 * * * * *"
    @Override
    @Scheduled(cron = "0 0 0 * * FRI")
    public void scheduleMenuGeneration() {
        LOGGER.info("🔄 Début de la génération planifiée des menus");
        try {
            User staffUser = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == Role.Staff)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucun utilisateur Staff trouvé"));

            generateWeeklyMenus(staffUser.getId_user());
            LOGGER.info("✅ Génération automatique des menus terminée");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la génération automatique : " + e.getMessage());
            throw e;
        }
    }


    @Override
    public List<RegimeAlimentaire> returnregime() {
        return regimeAlimentaireRepository.findAll();
    }
}