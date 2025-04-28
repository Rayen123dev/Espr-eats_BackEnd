package tn.esprit.projet_pi.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projet_pi.Repository.AbonnementRepository;
import tn.esprit.projet_pi.Repository.DiscountRepository;
import tn.esprit.projet_pi.Repository.TransactionRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.*;
import tn.esprit.projet_pi.interfaces.IAbonnement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AbonnementService implements IAbonnement {

    private final AbonnementRepository abonnementRepository;
    private final UserRepo userRepo;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final EmailAbonnementService emailService;
    private final DiscountRepository discountRepository;
    private final RestTemplate restTemplate;
    private static final String CPI_API_URL = "http://127.0.0.1:8000/predict";
    private static final double DEFAULT_BASE_CPI = 234.05; // Valeur par défaut si l'API échoue
    private static final LocalDate PREDICTION_DATE = LocalDate.of(2034, 4, 30); // Date fixe pour la prédiction

    @Autowired
    public AbonnementService(AbonnementRepository abonnementRepository, UserRepo userRepo,
                             TransactionService transactionService, EmailAbonnementService emailService,
                             TransactionRepository transactionRepository, DiscountRepository discountRepository) {
        this.abonnementRepository = abonnementRepository;
        this.userRepo = userRepo;
        this.transactionService = transactionService;
        this.emailService = emailService;
        this.transactionRepository = transactionRepository;
        this.discountRepository = discountRepository;
        this.restTemplate = new RestTemplate();
    }

    // Appeler l'API FastAPI pour obtenir le CPI
    private Double getPredictedCPI(LocalDate predictionDate, String modelType) {
        try {
            String url = CPI_API_URL + "?model_type=" + modelType + "&date=" + predictionDate.toString();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.getBody());
            return jsonResponse.get("predicted_CPI").asDouble();
        } catch (Exception e) {
            System.out.println("Erreur lors de l'appel à l'API CPI: " + e.getMessage());
            return null;
        }
    }

    // Obtenir le CPI de référence dynamiquement (aujourd'hui, General CPI)
    private Double getBaseCPI() {
        Double baseCPI = getPredictedCPI(LocalDate.now(), "general");
        return baseCPI != null && baseCPI > 0 ? baseCPI : DEFAULT_BASE_CPI;
    }

    // Calculer le coût ajusté en fonction du CPI, arrondi à l'entier
    private Double calculateCout(TypeAbonnement typeAbonnement) {
        double baseCost;
        switch (typeAbonnement) {
            case MENSUEL:
                baseCost = 30.0;
                break;
            case TRIMESTRIEL:
                baseCost = 80.0;
                break;
            case SEMESTRIEL:
                baseCost = 150.0;
                break;
            case ANNUEL:
                baseCost = 280.0;
                break;
            default:
                baseCost = 30.0;
        }

        // Obtenir le CPI de référence
        Double baseCPI = getBaseCPI();
        // Obtenir le CPI prédit pour la date fixe
        Double predictedCPI = getPredictedCPI(PREDICTION_DATE, "food"); // Food CPI
        if (predictedCPI != null && predictedCPI > 0) {
            // Ajuster le coût et arrondir à l'entier
            return (double) Math.round(baseCost * (predictedCPI / baseCPI));
        } else {
            return baseCost; // Revenir au coût fixe en cas d'erreur
        }
    }

    @Override
    @Transactional
    public Abonnement createAbonnementByUser(Abonnement abonnement, Long userId, String checkoutUrl) {
        User user = userRepo.findByidUser(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        if (user.getAbonnement() != null) {
            throw new RuntimeException("L'utilisateur a déjà un abonnement.");
        }

        // Définir la date de début
        LocalDate dateDebut = LocalDate.now();
        abonnement.setDateDebut(dateDebut);
        abonnement.setAbonnementStatus(AbonnementStatus.PENDING);

        // Calculer la date de fin
        LocalDate dateFin = calculateDateFin(dateDebut, abonnement.getTypeAbonnement());
        abonnement.setDateFin(dateFin);

        // Calculer le coût ajusté
        Map<String, Double> subscriptionCosts = getSubscriptionTypesAndCosts();
        Double cost = subscriptionCosts.get(abonnement.getTypeAbonnement().name());
        if (cost == null) {
            cost = calculateCout(abonnement.getTypeAbonnement());
        }
        abonnement.setCout(cost);
        abonnement.setConfirmed(abonnement.getConfirmed());
        abonnement.setBlocked(abonnement.getBlocked());
        abonnement.setRemainingDays(abonnement.calculateRemainingDays());

        abonnement.setUser(user);
        user.setAbonnement(abonnement);
        Abonnement newAbonnement = abonnementRepository.save(abonnement);

        // Envoyer l'email de confirmation
        emailService.sendConfirmationEmail(user, newAbonnement);

        // Créer et sauvegarder la transaction
        Transaction transaction = new Transaction();
        transaction.setAbonnement(newAbonnement);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMontant(abonnement.getCout());
        transaction.setDateTransaction(LocalDateTime.now().withNano(0));
        transaction.setReferencePaiement("REF-" + newAbonnement.getIdAbonnement());
        transaction.setDetails("Abonnement en attente, paiement non effectué");
        transactionService.createTransaction(transaction);

        return newAbonnement;
    }

    public Map<String, Double> getSubscriptionTypesAndCosts() {
        Map<String, Double> subscriptionTypes = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (TypeAbonnement type : TypeAbonnement.values()) {
            double baseCost = calculateCout(type);
            double discountedCost = baseCost;
            try {
                List<Discount> discounts = discountRepository.findByType(type);
                if (discounts != null) {
                    discounts = discounts.stream()
                            .filter(d -> Boolean.TRUE.equals(d.getActive()) &&
                                    d.getAbonnement() == null &&
                                    d.getStartDate() != null && d.getEndDate() != null &&
                                    (d.getStartDate().isBefore(now) || d.getStartDate().isEqual(now)) &&
                                    (d.getEndDate().isAfter(now) || d.getEndDate().isEqual(now)))
                            .toList();
                    if (!discounts.isEmpty()) {
                        Discount discount = discounts.get(0);
                        discountedCost = Math.round(baseCost - (baseCost * discount.getPercentage() / 100.0)); // Arrondi
                        System.out.println("Appliqué " + discount.getPercentage() + "% de réduction à " + type + ": " + discountedCost);
                    }
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la récupération des réductions pour " + type + ": " + e.getMessage());
            }
            subscriptionTypes.put(type.name(), discountedCost);
        }
        return subscriptionTypes;
    }

    private LocalDate calculateDateFin(LocalDate dateDebut, TypeAbonnement typeAbonnement) {
        return switch (typeAbonnement) {
            case MENSUEL -> dateDebut.plusMonths(1);
            case TRIMESTRIEL -> dateDebut.plusMonths(3);
            case SEMESTRIEL -> dateDebut.plusMonths(6);
            case ANNUEL -> dateDebut.plusYears(1);
        };
    }

    // ... (autres méthodes inchangées : deleteAbonnement, updateAbonnement, confirmAbonnement, etc.)
    @Override
    @Transactional
    public void deleteAbonnement(Long userId, Long idAbonnement) {
        checkIfAbonnementBlocked(idAbonnement);
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + idAbonnement));
        if (!abonnement.getUser().getId_user().equals(userId)) {
            throw new RuntimeException("Abonnement does not belong to user with ID: " + userId);
        }
        List<Transaction> transactions = transactionRepository.findByAbonnement(abonnement);
        if (!transactions.isEmpty()) {
            transactionRepository.deleteAll(transactions);
        }
        User user = abonnement.getUser();
        user.setAbonnement(null);
        abonnementRepository.delete(abonnement);
    }

    @Override
    public Abonnement updateAbonnement(Long userId, Abonnement updatedAbonnement) {
        Abonnement existingAbonnement = abonnementRepository.findById(updatedAbonnement.getIdAbonnement())
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + updatedAbonnement.getIdAbonnement()));
        if (!existingAbonnement.getUser().getId_user().equals(userId)) {
            throw new RuntimeException("Abonnement does not belong to user with ID: " + userId);
        }
        if (existingAbonnement.getBlocked()) {
            throw new RuntimeException("Abonnement is blocked. No further actions can be performed.");
        }
        if (updatedAbonnement.getTypeAbonnement() != null) {
            existingAbonnement.setTypeAbonnement(updatedAbonnement.getTypeAbonnement());
        }
        if (updatedAbonnement.getAbonnementStatus() != null) {
            existingAbonnement.setAbonnementStatus(updatedAbonnement.getAbonnementStatus());
        }
        if (updatedAbonnement.getRenouvellementAutomatique() != null) {
            existingAbonnement.setRenouvellementAutomatique(updatedAbonnement.getRenouvellementAutomatique());
        }
        if (updatedAbonnement.getDateDebut() != null) {
            existingAbonnement.setDateDebut(updatedAbonnement.getDateDebut());
        }
        if (updatedAbonnement.getDateFin() != null) {
            existingAbonnement.setDateFin(updatedAbonnement.getDateFin());
        }
        if (updatedAbonnement.getCout() != null) {
            existingAbonnement.setCout(updatedAbonnement.getCout());
        }
        return abonnementRepository.save(existingAbonnement);
    }

    @Override
    public Abonnement getAbonnementById(Long userId, Long idAbonnement) {
        checkIfAbonnementBlocked(idAbonnement);
        Abonnement abonnement = abonnementRepository.findById(idAbonnement).orElse(null);
        if (abonnement != null && abonnement.getUser().getId_user().equals(userId)) {
            abonnement.setRemainingDays(abonnement.calculateRemainingDays());
            return abonnement;
        }
        return null;
    }

    public void checkIfAbonnementBlocked(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + abonnementId));
        if (abonnement.getBlocked()) {
            abonnement.setAbonnementStatus(AbonnementStatus.SUSPENDED);
            abonnementRepository.save(abonnement);
            throw new RuntimeException("Abonnement is blocked. No further actions can be performed.");
        }
    }

    @Transactional
    public Abonnement confirmAbonnement(String confirmationCode, Long userId, String clientIp) {
        Abonnement abonnement = abonnementRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation code"));
        if (!abonnement.getUser().getId_user().equals(userId)) {
            emailService.sendAlertToOwner(abonnement, userId, clientIp);
            throw new RuntimeException("The code doesn't match the user's abonnement");
        }
        if (abonnement.getBlocked()) {
            throw new RuntimeException("Abonnement is already blocked.");
        }
        if (!abonnement.getConfirmationCode().equals(confirmationCode)) {
            abonnement.setFailedConfirmationAttempts(abonnement.getFailedConfirmationAttempts() + 1);
            if (abonnement.getFailedConfirmationAttempts() >= 3) {
                abonnement.setBlocked(true);
                abonnement.setAbonnementStatus(AbonnementStatus.SUSPENDED);
                abonnementRepository.save(abonnement);
                throw new RuntimeException("Abonnement is blocked due to too many failed attempts.");
            }
            abonnementRepository.save(abonnement);
            throw new RuntimeException("Invalid confirmation code");
        }
        if (abonnement.isCodeExpired()) {
            throw new RuntimeException("Confirmation code has expired");
        }
        abonnement.setConfirmed(true);
        abonnement.setAbonnementStatus(AbonnementStatus.ACTIVE);
        abonnement.setFailedConfirmationAttempts(0);
        abonnementRepository.save(abonnement);
        sendActivationEmail(abonnement);
        Transaction transaction = new Transaction();
        transaction.setAbonnement(abonnement);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setMontant(abonnement.getCout());
        transaction.setReferencePaiement("REF-" + abonnement.getIdAbonnement());
        transaction.setDetails("Abonnement confirmé et actif avec paiement validé");
        transactionRepository.save(transaction);
        return abonnement;
    }

    private void sendActivationEmail(Abonnement abonnement) {
        String userEmail = abonnement.getUser().getEmail();
        String subject = "Votre Abonnement est Maintenant Activé!";
        String text = String.format(
                """
                        Dear %s,

                        Your subscription has been successfully activated!

                        Thank you for being a part of our service.

                        Best regards,
                        Your Service Team""",
                abonnement.getUser().getNom()
        );
        emailService.sendGenericEmail(userEmail, subject, text);
    }

    public Map<String, Object> getSubscriptionReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("activeSubscriptions", abonnementRepository.countActiveSubscriptions());
        report.put("pendingSubscriptions", abonnementRepository.countPendingSubscriptions());
        report.put("expiredSubscriptions", abonnementRepository.countExpiredSubscriptions());
        report.put("blockedSubscriptions", abonnementRepository.countBlockedSubscriptions());
        report.put("totalRevenue", abonnementRepository.calculateTotalRevenue());
        List<Object[]> abonnementsByType = abonnementRepository.countSubscriptionsByType();
        Map<String, Long> typeData = new HashMap<>();
        for (Object[] result : abonnementsByType) {
            TypeAbonnement type = (TypeAbonnement) result[0];
            Long count = (Long) result[1];
            typeData.put(type.name(), count);
        }
        report.put("les abonnements", typeData);
        List<Object[]> monthlyGrowth = abonnementRepository.countMonthlySubscriptions();
        Map<String, Long> monthlyData = new HashMap<>();
        for (Object[] result : monthlyGrowth) {
            monthlyData.put("Month " + result[0], (Long) result[1]);
        }
        report.put("monthlyGrowth", monthlyData);
        return report;
    }

    public List<Abonnement> getAllAbonnementsByType(TypeAbonnement typeAbonnement) {
        return abonnementRepository.findAllByTypeAbonnement(typeAbonnement);
    }

    public List<Abonnement> getAllAbonnementsByStatus(AbonnementStatus abonnementStatus) {
        return abonnementRepository.findAllByAbonnementStatus(abonnementStatus);
    }

    public Abonnement getAbonnementByStripeSessionId(String stripeSessionId) {
        return abonnementRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new RuntimeException("Abonnement n'est pas trouvé pour session ID: " + stripeSessionId));
    }

    public TypeAbonnement getRecommendedSubscriptionType() {
        List<Object[]> typeCounts = abonnementRepository.countSubscriptionsByType();
        if (typeCounts.isEmpty()) {
            return TypeAbonnement.MENSUEL;
        }
        TypeAbonnement recommendedType = null;
        long maxCount = 0;
        for (Object[] typeCount : typeCounts) {
            TypeAbonnement type = (TypeAbonnement) typeCount[0];
            Long count = (Long) typeCount[1];
            if (count > maxCount) {
                maxCount = count;
                recommendedType = type;
            }
        }
        return recommendedType != null ? recommendedType : TypeAbonnement.MENSUEL;
    }

    public Abonnement unblockAbonnement(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement n'est pas trouvé avec ID: " + abonnementId));
        if (!abonnement.getBlocked()) {
            throw new RuntimeException("Abonnement est bloqué.");
        }
        if (abonnement.getUser().getRole() != Role.Admin) {
            throw new RuntimeException("seulement l'admin peut bloquer l'abonnement.");
        }
        abonnement.setBlocked(false);
        abonnement.setAbonnementStatus(AbonnementStatus.ACTIVE);
        Transaction transaction = new Transaction();
        transaction.setAbonnement(abonnement);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setMontant(abonnement.getCout());
        transaction.setDateTransaction(LocalDateTime.now().withNano(0));
        transaction.setReferencePaiement("REF-" + abonnement.getIdAbonnement());
        transaction.setDetails("votre abonnement est débloqué maintenant et vous pouvez faire ");
        transactionService.createTransaction(transaction);
        return abonnementRepository.save(abonnement);
    }

    @Transactional
    public Abonnement blockAbonnement(Long abonnementId, String reason) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement n'est pas trouvé avec ID: " + abonnementId));
        if (abonnement.getBlocked()) {
            throw new RuntimeException("Abonnement est déjà bloqué.");
        }
        abonnement.setBlocked(true);
        abonnement.setAbonnementStatus(AbonnementStatus.SUSPENDED);
        abonnement.setRemainingDays(abonnement.calculateRemainingDays());
        abonnementRepository.save(abonnement);
        Transaction transaction = new Transaction();
        transaction.setAbonnement(abonnement);
        transaction.setStatus(TransactionStatus.BLOCKED);
        transaction.setMontant(abonnement.getCout());
        transaction.setDateTransaction(LocalDateTime.now().withNano(0));
        transaction.setReferencePaiement("REF-" + abonnement.getIdAbonnement() + "-BLOCK");
        transaction.setDetails("Abonnement bloqué: " + reason);
        transactionService.createTransaction(transaction);
        emailService.sendGenericEmail(
                abonnement.getUser().getEmail(),
                "Abonnement Bloqué",
                "Votre abonnement a été bloqué pour la raison suivante: " + reason + ". Veuillez contacter le support pour réactiver."
        );
        return abonnement;
    }

    @Transactional
    public void checkFailedPaymentAttempts(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement n'est pas trouvé avec ID: " + abonnementId));
        long failedAttempts = transactionRepository.findByAbonnementAndStatus(abonnement, TransactionStatus.BLOCKED).size();
        if (failedAttempts >= 3) {
            blockAbonnement(abonnementId, "Trois tentatives de paiement échouées");
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void monitorPaymentFailures() {
        List<Abonnement> activeAbonnements = abonnementRepository.findAllByAbonnementStatusAndRenouvellementAutomatiqueTrue(AbonnementStatus.ACTIVE);
        for (Abonnement abonnement : activeAbonnements) {
            checkFailedPaymentAttempts(abonnement.getIdAbonnement());
        }
    }
}