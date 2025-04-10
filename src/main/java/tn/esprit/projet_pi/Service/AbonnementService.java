package tn.esprit.projet_pi.Service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    final AbonnementRepository abonnementRepository;
    final UserRepo userRepo;
    private final TransactionRepository transactionRepository;
    TransactionService transactionService;
    EmailAbonnementService emailService;
    private final DiscountRepository discountRepository;

    @Autowired
    public AbonnementService(AbonnementRepository abonnementRepository, UserRepo userRepo, TransactionService transactionService, EmailAbonnementService emailService, TransactionRepository transactionRepository, DiscountRepository discountRepository) {
        this.abonnementRepository = abonnementRepository;
        this.userRepo = userRepo;
        this.transactionService = transactionService;
        this.emailService = emailService;
        this.transactionRepository = transactionRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    @Transactional
    public Abonnement createAbonnementByUser(Abonnement abonnement, Long userId, String checkoutUrl) {
        User user = userRepo.findByidUser(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        if (user.getAbonnement() != null) {
            throw new RuntimeException("L'utilisateur a déjà un abonnement.");
        }

        // Set the start date
        LocalDate dateDebut = LocalDate.now();
        abonnement.setDateDebut(dateDebut);
        abonnement.setAbonnementStatus(AbonnementStatus.PENDING);
        Map<String, Double> subscriptionCosts = getSubscriptionTypesAndCosts();
        Double cost = subscriptionCosts.get(abonnement.getTypeAbonnement().name());
        if (cost == null) {
            // Fallback to base cost if something goes wrong
            cost = calculateCout(abonnement.getTypeAbonnement());
        }
        abonnement.setCout(cost);
        abonnement.setConfirmed(abonnement.getConfirmed());
        abonnement.setBlocked(abonnement.getBlocked());

        // Calculate the end date based on the type of subscription
        LocalDate dateFin = calculateDateFin(dateDebut, abonnement.getTypeAbonnement());
        abonnement.setDateFin(dateFin);
        abonnement.setRemainingDays(abonnement.calculateRemainingDays());

        abonnement.setUser(user);
        user.setAbonnement(abonnement);
        Abonnement newAbonnement = abonnementRepository.save(abonnement);

        // Send email with Stripe Checkout URL instead of confirmation email
        //emailService.sendStripeUrl(user, checkoutUrl);
        emailService.sendConfirmationEmail(user, newAbonnement);

        // Create and save the transaction for this abonnement
        Transaction transaction = new Transaction();
        transaction.setAbonnement(newAbonnement);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setMontant(abonnement.getCout());
        transaction.setDateTransaction(LocalDateTime.now().withNano(0));
        transaction.setReferencePaiement("REF-" + newAbonnement.getIdAbonnement());
        transaction.setDetails("Abonnement est encore en attente , le paiement n'est pas encore effectué");
        transactionService.createTransaction(transaction);

        return newAbonnement;
    }

    @Override
    @Transactional
    public void deleteAbonnement(Long userId, Long idAbonnement) {
        // Check if the abonnement is blocked
        checkIfAbonnementBlocked(idAbonnement);

        // Fetch the abonnement
        Abonnement abonnement = abonnementRepository.findById(idAbonnement)
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + idAbonnement));

        // Verify that the abonnement belongs to the user
        if (!abonnement.getUser().getId_user().equals(userId)) {
            throw new RuntimeException("Abonnement does not belong to user with ID: " + userId);
        }
        // Fetch and delete all associated transactions
        List<Transaction> transactions = transactionRepository.findByAbonnement(abonnement);
        if (!transactions.isEmpty()) {
            transactionRepository.deleteAll(transactions);
        }

        // Remove the abonnement from the user
        User user = abonnement.getUser();
        user.setAbonnement(null);

        // Delete the abonnement
        abonnementRepository.delete(abonnement);
    }

    @Override
    public Abonnement updateAbonnement(Long userId, Abonnement updatedAbonnement) {
        // Fetch the existing abonnement
        Abonnement existingAbonnement = abonnementRepository.findById(updatedAbonnement.getIdAbonnement())
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + updatedAbonnement.getIdAbonnement()));

        // Check if the abonnement belongs to the user
        if (!existingAbonnement.getUser().getId_user().equals(userId)) {
            throw new RuntimeException("Abonnement does not belong to user with ID: " + userId);
        }

        // Check if the abonnement is blocked
        if (existingAbonnement.getBlocked()) {
            throw new RuntimeException("Abonnement is blocked. No further actions can be performed.");
        }

        // Update only non-null fields from the updatedAbonnement
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

        // Save and return the updated abonnement
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

    private LocalDate calculateDateFin(LocalDate dateDebut, TypeAbonnement typeAbonnement) {
        return switch (typeAbonnement) {
            case MENSUEL -> dateDebut.plusMonths(1);
            case TRIMESTRIEL -> dateDebut.plusMonths(3);
            case SEMESTRIEL -> dateDebut.plusMonths(6);
            case ANNUEL -> dateDebut.plusYears(1);
        };
    }

    private Double calculateCout(TypeAbonnement typeAbonnement) {
        return switch (typeAbonnement) {
            case MENSUEL -> 30.0;       // Monthly subscription
            case TRIMESTRIEL -> 80.0;   // 3 months (save 10Dt)
            case SEMESTRIEL -> 150.0;   // 6 months (save 30Dt)
            case ANNUEL -> 280.0;       // Annual (save 80Dt)
        };
    }

    // Prevent actions if the abonnement is blocked
    public void checkIfAbonnementBlocked(Long abonnementId) {
        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RuntimeException("Abonnement not found with ID: " + abonnementId));

        if (abonnement.getBlocked()) {
            abonnement.setAbonnementStatus(AbonnementStatus.SUSPENDED); // Assuming status is a String; adjust if using an enum
            abonnementRepository.save(abonnement);
            throw new RuntimeException("Abonnement is blocked. No further actions can be performed.");
        }
    }

    @Transactional
    public Abonnement confirmAbonnement(String confirmationCode, Long userId,String clientIp) {
        // Find the abonnement by confirmation code
        Abonnement abonnement = abonnementRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation code"));

        // Check if the abonnement belongs to the user
        if (!abonnement.getUser().getId_user().equals(userId)) {
            // Send alert to the owner using EmailAbonnementService
            emailService.sendAlertToOwner(abonnement, userId, clientIp);
            throw new RuntimeException("The code doesn't match the user's abonnement");
        }

        // Check if the code has expired
        if (abonnement.isCodeExpired()) {
            throw new RuntimeException("Confirmation code has expired");
        }

        // Confirm the abonnement
        abonnement.setConfirmed(true);
        abonnement.setAbonnementStatus(AbonnementStatus.ACTIVE);
        abonnementRepository.save(abonnement);
        sendActivationEmail(abonnement);

        // Create and save the transaction
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

        // Send the email using the EmailAbonnementService
        emailService.sendGenericEmail(userEmail, subject, text);
    }

    //reports and statistics on subscriptions
    public Map<String, Object> getSubscriptionReport() {
        Map<String, Object> report = new HashMap<>();

        // Count subscriptions by specific statuses
        report.put("activeSubscriptions", abonnementRepository.countActiveSubscriptions());
        report.put("pendingSubscriptions", abonnementRepository.countPendingSubscriptions());
        report.put("expiredSubscriptions", abonnementRepository.countExpiredSubscriptions());
        report.put("blockedSubscriptions", abonnementRepository.countBlockedSubscriptions()); // Already separate

        // Total revenue should include all subscriptions, regardless of status (except perhaps CANCELED)
        report.put("totalRevenue", abonnementRepository.calculateTotalRevenue());

        // Subscriptions by type (should include all types, regardless of status)
        List<Object[]> abonnementsByType = abonnementRepository.countSubscriptionsByType();
        Map<String, Long> typeData = new HashMap<>();
        for (Object[] result : abonnementsByType) {
            TypeAbonnement type = (TypeAbonnement) result[0];
            Long count = (Long) result[1];
            typeData.put(type.name(), count);
        }
        report.put("les abonnements", typeData);

        // Monthly growth (should include all new subscriptions, regardless of current status)
        List<Object[]> monthlyGrowth = abonnementRepository.countMonthlySubscriptions();
        Map<String, Long> monthlyData = new HashMap<>();
        for (Object[] result : monthlyGrowth) {
            monthlyData.put("Month " + result[0], (Long) result[1]);
        }
        report.put("monthlyGrowth", monthlyData);

        return report;
    }

    public Map<String, Double> getSubscriptionTypesAndCosts() {
        Map<String, Double> subscriptionTypes = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (TypeAbonnement type : TypeAbonnement.values()) {
            double baseCost = calculateCout(type);
            double discountedCost = baseCost;

            // Safely query discounts with fallback
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
                        discountedCost = baseCost - (baseCost * discount.getPercentage() / 100.0);
                        System.out.println("Applied " + discount.getPercentage() + "% discount to " + type + ": " + discountedCost);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error fetching discounts for " + type + ": " + e.getMessage());
                // Fallback to base cost if discount query fails
            }

            subscriptionTypes.put(type.name(), discountedCost);
        }
        return subscriptionTypes;
    }


    public List<Abonnement> getAllAbonnementsByType(TypeAbonnement typeAbonnement) {
        return abonnementRepository.findAllByTypeAbonnement(typeAbonnement);
    }

    //get all abonnements by status
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
            // Default to MENSUEL if no subscriptions exist
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

        // If no type is found (unlikely), default to MENSUEL
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

}
