package tn.esprit.projet_pi.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.AbonnementService;
import tn.esprit.projet_pi.Service.EmailAbonnementService;
import tn.esprit.projet_pi.Service.StripeService;
import tn.esprit.projet_pi.dto.AbonnementRequest;
import tn.esprit.projet_pi.dto.StripeResponse;
import tn.esprit.projet_pi.entity.Abonnement;
import tn.esprit.projet_pi.entity.AbonnementStatus;
import tn.esprit.projet_pi.entity.TypeAbonnement;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import tn.esprit.projet_pi.entity.User;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/abonnement")
public class AbonnementController {
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;


    private final AbonnementService abonnementService;
    private final StripeService stripeService;
    private final EmailAbonnementService emailAbonnementService;
    private final UserRepo userRepo;

    @Autowired
    public AbonnementController(AbonnementService abonnementService, StripeService stripeService, EmailAbonnementService emailAbonnementService, UserRepo userRepo) {
        this.abonnementService = abonnementService;
        this.stripeService = stripeService;
        this.emailAbonnementService = emailAbonnementService;
        this.userRepo = userRepo;
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addAbonnement(@RequestBody AbonnementRequest abonnementRequest, @PathVariable("userId") Long userId) {
        try {
            Abonnement abonnement = new Abonnement();
            abonnement.setTypeAbonnement(abonnementRequest.getTypeAbonnement());
            abonnement.setRenouvellementAutomatique(abonnementRequest.getRenouvellementAutomatique());

            StripeResponse stripeResponse = stripeService.checkoutAbonnements(abonnementRequest, userId);
            if ("open".equals(stripeResponse.getStatus())) {
                Abonnement createdAbonnement = abonnementService.createAbonnementByUser(abonnement, userId, stripeResponse.getMessage());
                createdAbonnement.setStripeSessionId(stripeResponse.getPaymentId());
                abonnementService.updateAbonnement(userId, createdAbonnement);
                Map<String, Object> response = new HashMap<>();
                response.put("stripeResponse", stripeResponse);
                response.put("abonnement", createdAbonnement);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to create Stripe checkout session", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{userId}/{idAbonnement}")
    public ResponseEntity<Void> deleteAbonnement(@PathVariable Long userId, @PathVariable Long idAbonnement) {
        try {
            abonnementService.deleteAbonnement(userId, idAbonnement);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/update/{userId}")
    public ResponseEntity<Abonnement> updateAbonnement(@PathVariable Long userId, @RequestBody Abonnement abonnement) {
        try {
            Abonnement updatedAbonnement = abonnementService.updateAbonnement(userId, abonnement);
            return new ResponseEntity<>(updatedAbonnement, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/get/{userId}/{idAbonnement}")
    public ResponseEntity<Abonnement> getAbonnementById(@PathVariable Long userId, @PathVariable Long idAbonnement) {
        Abonnement abonnement = abonnementService.getAbonnementById(userId, idAbonnement);
        if (abonnement != null) {
            return new ResponseEntity<>(abonnement, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/confirm/{userId}/{confirmationCode}")
    public ResponseEntity<Abonnement> confirmAbonnement(
            @PathVariable Long userId,
            @PathVariable String confirmationCode,
            HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            Abonnement confirmedAbonnement = abonnementService.confirmAbonnement(confirmationCode, userId, clientIp);
            return new ResponseEntity<>(confirmedAbonnement, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getSubscriptionReport() {
        Map<String, Object> report = abonnementService.getSubscriptionReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/types-and-costs")
    public ResponseEntity<Map<String, Double>> getSubscriptionTypesAndCosts() {
        Map<String, Double> typesAndCosts = abonnementService.getSubscriptionTypesAndCosts();
        return ResponseEntity.ok(typesAndCosts);
    }

    @GetMapping("/getAll-ByType/{type}")
    public ResponseEntity<Iterable<Abonnement>> getAbonnementsByType(@PathVariable TypeAbonnement type) {
        Iterable<Abonnement> abonnements = abonnementService.getAllAbonnementsByType(type);
        return ResponseEntity.ok(abonnements);
    }

    @GetMapping("/getAll-ByStatus/{status}")
    public ResponseEntity<Iterable<Abonnement>> getAbonnementsByStatus(@PathVariable AbonnementStatus status) {
        Iterable<Abonnement> abonnements = abonnementService.getAllAbonnementsByStatus(status);
        return ResponseEntity.ok(abonnements);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            // Verify the webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Handle the checkout.session.completed event
            if ("checkout.session.completed".equals(event.getType())) {
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    Session session = (Session) dataObjectDeserializer.getObject().get();
                    String sessionId = session.getId();
                    String userIdStr = session.getMetadata().get("userId");
                    Long userId = Long.parseLong(userIdStr);

                    // Find the abonnement by stripeSessionId
                    Abonnement abonnement = abonnementService.getAbonnementByStripeSessionId(sessionId);
                    if (abonnement != null && abonnement.getAbonnementStatus() == AbonnementStatus.PENDING) {
                        // Update status to ACTIVE
                        abonnement.setAbonnementStatus(AbonnementStatus.ACTIVE);
                        abonnement.setConfirmed(true);
                        abonnementService.updateAbonnement(userId, abonnement);

                        // Fetch the user and send confirmation email
                        User user = userRepo.findByidUser(userId)
                                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
                        emailAbonnementService.sendConfirmationEmail(user, abonnement); // Direct call to EmailAbonnementService
                    }
                }
            }
            return new ResponseEntity<>("Webhook handled", HttpStatus.OK);
        } catch (SignatureVerificationException e) {
            return new ResponseEntity<>("Invalid signature", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Webhook error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}