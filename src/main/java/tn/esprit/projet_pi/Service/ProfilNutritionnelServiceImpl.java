package tn.esprit.projet_pi.Service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ProfilNutritionnelRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfilNutritionnelServiceImpl implements IProfilNutritionnelService {

    private final ProfilNutritionnelRepository profilNutritionnelRepository;
    private final UserRepo userRepo;
    private final HistoriqueProfilNutritionnelServiceImpl historiqueService;

    public ProfilNutritionnelServiceImpl(ProfilNutritionnelRepository profilNutritionnelRepository,
                                         UserRepo userRepo,
                                         HistoriqueProfilNutritionnelServiceImpl historiqueService) {
        this.profilNutritionnelRepository = profilNutritionnelRepository;
        this.userRepo = userRepo;
        this.historiqueService = historiqueService;
    }

    @Override
    public ProfilNutritionnel addProfil(ProfilNutritionnel profil) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Non authentifié !");
        }

        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        profil.setUser(user);

        calculerImcEtBesoins(profil);

        return profilNutritionnelRepository.save(profil);
    }

    @Override
    public ProfilNutritionnel updateProfil(Long id, ProfilNutritionnel newProfil) {
        ProfilNutritionnel ancien = profilNutritionnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        HistoriqueProfilNutritionnel historique = new HistoriqueProfilNutritionnel();
        historique.setProfil(ancien);
        historique.setDateEnregistrement(LocalDateTime.now());
        historique.setPoids(ancien.getPoidsActuel());
        historique.setNiveauActivite(ancien.getNiveauActivite());
        historique.setObjectif(ancien.getObjectif());
        historique.setImc(ancien.getImc());
        historique.setBesoinCalorique(ancien.getBesoinCalorique());

        historique.setCommentaire(user.getRole() == Role.Medcin ?
                "Mise à jour effectuée par le médecin." :
                "Mise à jour effectuée par l'étudiant.");
        historiqueService.save(historique);

        ancien.setTaille(newProfil.getTaille());
        ancien.setPoidsActuel(newProfil.getPoidsActuel());
        ancien.setNiveauActivite(newProfil.getNiveauActivite());
        ancien.setObjectif(newProfil.getObjectif());
        ancien.setRegimeAlimentaire(newProfil.getRegimeAlimentaire());
        ancien.setAllergies(newProfil.getAllergies());
        ancien.setDerniereEvolution(newProfil.getDerniereEvolution());
        ancien.setSexe(newProfil.getSexe());
        ancien.setDerniereMiseAJour(LocalDateTime.now());

        calculerImcEtBesoins(ancien);

        return profilNutritionnelRepository.save(ancien);
    }

    private void calculerImcEtBesoins(ProfilNutritionnel profil) {
        Double poids = profil.getPoidsActuel();
        Double tailleCm = profil.getTaille();
        Sexe sexe = profil.getSexe();

        if (poids != null && tailleCm != null && sexe != null && tailleCm > 0) {
            double tailleM = tailleCm / 100.0;
            double imc = poids / (tailleM * tailleM);
            profil.setImc(Math.round(imc * 10.0) / 10.0);

            double bmr = (sexe == Sexe.HOMME)
                    ? (13.75 * poids) + (5.003 * tailleCm) + 66.47
                    : (9.563 * poids) + (1.850 * tailleCm) + 655.1;

            double coeff = switch (profil.getNiveauActivite()) {
                case SEDENTAIRE -> 1.2;
                case MODERE     -> 1.55;
                case INTENSE    -> 1.9;
                default         -> 1.2;
            };

            int besoin = (int) Math.round(bmr * coeff);
            profil.setBesoinCalorique(besoin);
        }
    }

    @Override
    public void deleteProfil(Long id) {
        profilNutritionnelRepository.deleteById(id);
    }

    @Override
    public ProfilNutritionnel getProfilById(Long id) {
        return profilNutritionnelRepository.findById(id).orElse(null);
    }

    @Override
    public ProfilNutritionnel getProfilByUserId(Long userId) {
        return profilNutritionnelRepository.findByUser_IdUser(userId);
    }

    @Override
    public List<ProfilNutritionnel> getAllProfils() {
        return profilNutritionnelRepository.findAll();
    }
}
