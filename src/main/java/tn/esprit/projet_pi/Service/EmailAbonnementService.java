package tn.esprit.projet_pi.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projet_pi.entity.Abonnement;
import tn.esprit.projet_pi.entity.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;

@Service

public class EmailAbonnementService {

    private final JavaMailSender javaMailSender;
    private final ResourceLoader resourceLoader;
    private final RestTemplate restTemplate = new RestTemplate();

    public EmailAbonnementService(JavaMailSender javaMailSender, ResourceLoader resourceLoader) {
        this.javaMailSender = javaMailSender;
        this.resourceLoader = resourceLoader;
    }
    @Async
    public void sendConfirmationEmail(User user, Abonnement abonnement) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Confirmation de votre abonnement");

            // Charger le modèle HTML
            Resource resource = resourceLoader.getResource("classpath:templates/confirmation-email.html");
            String htmlTemplate = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

            // Remplacer les placeholders (sans lien)
            String htmlContent = htmlTemplate
                    .replace("${userName}", user.getNom())
                    .replace("${confirmationCode}", abonnement.getConfirmationCode())
                    .replace("${expirationDate}", abonnement.getCodeExpiration().toString());

            helper.setText(htmlContent, true); // true pour indiquer du HTML
            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace(); // Gérer les exceptions selon vos besoins
        }
    }

    @Async
    public void sendExpirationEmail(Abonnement abonnement) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(abonnement.getUser().getEmail());
        message.setSubject("Avis d’expiration de votre abonnement");

        String text = String.format(
                "Chère %s,\n\n" +
                        "Votre abonnement arrivera à expiration le %s.\n\n" +
                        "Puisque vous avez désactivé le renouvellement automatique, veuillez le renouveler manuellement si vous souhaitez continuer à utiliser nos services.\n\n" +
                        "Cordialement,\nL’équipe Service",
                abonnement.getUser().getNom(),
                abonnement.getDateFin().toString()
        );

        message.setText(text);
        javaMailSender.send(message);
    }

    @Async
    public void sendGenericEmail(String email, String subject, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(messageContent);
        javaMailSender.send(message);
    }

    @Async
    public void sendAlertToOwner(Abonnement abonnement, Long attemptingUserId, String clientIp) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            User owner = abonnement.getUser();
            helper.setTo(owner.getEmail());
            helper.setSubject("Security Alert: Someone Tried to Use Your Confirmation Code");

            Resource resource = resourceLoader.getResource("classpath:templates/security-alert.html");
            String htmlTemplate = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

            // Fetch geolocation data
            String location = getLocationFromIp(clientIp);

            String htmlContent = htmlTemplate
                    .replace("${userName}", owner.getNom())
                    .replace("${confirmationCode}", abonnement.getConfirmationCode())
                    .replace("${attemptDate}", LocalDateTime.now().toString())
                    .replace("${location}", location); // Add location placeholder

            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getLocationFromIp(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String region = (String) response.get("regionName");
                String country = (String) response.get("country");
                return String.format("%s, %s, %s", city, region, country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

}