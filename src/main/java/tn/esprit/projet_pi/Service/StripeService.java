package tn.esprit.projet_pi.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.dto.AbonnementRequest;
import tn.esprit.projet_pi.dto.StripeResponse;
import tn.esprit.projet_pi.entity.TypeAbonnement;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    private String successUrl; // e.g., "http://localhost:4200/success" - Add to application.properties

    private String cancelUrl;  // e.g., "http://localhost:4200/cancel" - Add to application.properties

    // Match costs with AbonnementService.calculateCout()
    private Long getUnitAmount(TypeAbonnement type) {
        return switch (type) {
            case MENSUEL -> 3000L;    // 30.00 EUR (in cents)
            case TRIMESTRIEL -> 8000L; // 80.00 EUR (in cents)
            case SEMESTRIEL -> 15000L; // 150.00 EUR (in cents)
            case ANNUEL -> 28000L;    // 280.00 EUR (in cents)
        };
    }

    private String getRecurringInterval(TypeAbonnement type) {
        return switch (type) {
            case MENSUEL -> "month";
            case TRIMESTRIEL -> "month"; // 3 months
            case SEMESTRIEL -> "month";  // 6 months
            case ANNUEL -> "year";
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
                            .setCurrency("eur")
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