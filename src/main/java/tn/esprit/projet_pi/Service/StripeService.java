package tn.esprit.projet_pi.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.DiscountRepository;
import tn.esprit.projet_pi.dto.AbonnementRequest;
import tn.esprit.projet_pi.dto.StripeResponse;
import tn.esprit.projet_pi.entity.Discount;
import tn.esprit.projet_pi.entity.TypeAbonnement;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    private String successUrl; // e.g., "http://localhost:4200/success" - Add to application.properties
    private String cancelUrl;  // e.g., "http://localhost:4200/cancel" - Add to application.properties

    private final DiscountRepository discountRepository;

    @Autowired
    public StripeService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    // Match costs with AbonnementService.calculateCout(), in cents (1 EUR = 100 cents)
    private Long getUnitAmount(TypeAbonnement type) {
        // Base costs in EUR (from AbonnementService.calculateCout())
        double baseCostInEuros = switch (type) {
            case MENSUEL -> 30.0;    // 30.00 EUR
            case TRIMESTRIEL -> 80.0; // 80.00 EUR
            case SEMESTRIEL -> 150.0; // 150.00 EUR
            case ANNUEL -> 280.0;    // 280.00 EUR
        };

        // Convert to cents (1 EUR = 100 cents)
        double baseCostInCents = baseCostInEuros * 100;

        // Check for active discounts
        double discountedCostInCents = baseCostInCents;
        LocalDateTime now = LocalDateTime.now();

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
                    double discountPercentage = discount.getPercentage();
                    discountedCostInCents = baseCostInCents - (baseCostInCents * discountPercentage / 100.0);
                    System.out.println("Applied " + discountPercentage + "% discount to " + type + ": " + (discountedCostInCents / 100.0) + " EUR");
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching discounts for " + type + ": " + e.getMessage());
            // Fallback to base cost if discount query fails
        }

        // Stripe expects the amount as a long (in cents for EUR)
        return Math.round(discountedCostInCents);
    }

    private String getRecurringInterval(TypeAbonnement type) {
        return switch (type) {
            case MENSUEL -> "MONTH";
            case TRIMESTRIEL -> "MONTH"; // 3 months
            case SEMESTRIEL -> "MONTH";  // 6 months
            case ANNUEL -> "YEAR";
        };
    }

    private Long getIntervalCount(TypeAbonnement type) {
        return switch (type) {
            case MENSUEL -> 1L;    // 1 month
            case TRIMESTRIEL -> 3L; // 3 months
            case SEMESTRIEL -> 6L;  // 6 months
            case ANNUEL -> 1L;     // 1 year
        };
    }

    public StripeResponse checkoutAbonnements(AbonnementRequest abonnementRequest, Long userId) {
        // Set the Stripe API key
        Stripe.apiKey = secretKey;

        try {
            // Build the product data
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(abonnementRequest.getTypeAbonnement().name() + " Abonnement")
                            .build();

            // Build the price data with recurring or one-time payment
            SessionCreateParams.LineItem.PriceData.Builder priceDataBuilder =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur") // Keep currency as EUR
                            .setUnitAmount(getUnitAmount(abonnementRequest.getTypeAbonnement()))
                            .setProductData(productData);

            if (abonnementRequest.getRenouvellementAutomatique()) {
                priceDataBuilder.setRecurring(
                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.valueOf(getRecurringInterval(abonnementRequest.getTypeAbonnement())))
                                .setIntervalCount(getIntervalCount(abonnementRequest.getTypeAbonnement()))
                                .build()
                );
            }

            SessionCreateParams.LineItem.PriceData priceData = priceDataBuilder.build();

            // Build the line item
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setPriceData(priceData)
                            .setQuantity(1L)
                            .build();

            // Build the Checkout Session parameters
            SessionCreateParams.Builder paramsBuilder =
                    SessionCreateParams.builder()
                            .addLineItem(lineItem)
                            .setMode(abonnementRequest.getRenouvellementAutomatique() ? SessionCreateParams.Mode.SUBSCRIPTION : SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:4200/abonnement/payment-confirmation")
                            .setCancelUrl("http://localhost:8081/api/abonnement/cancel");

            // Add metadata using putMetadata
            paramsBuilder.putMetadata("userId", userId.toString());

            SessionCreateParams params = paramsBuilder.build();

            // Create the Checkout Session
            Session session = Session.create(params);

            // Return the StripeResponse with the session ID and URL
            return new StripeResponse(session.getId(), session.getStatus(), session.getUrl());
        } catch (StripeException e) {
            // Handle Stripe errors
            return new StripeResponse(null, "failed", "Checkout failed: " + e.getMessage());
        }
    }
}