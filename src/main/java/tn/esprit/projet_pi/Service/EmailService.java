package tn.esprit.projet_pi.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.entity.Menu;
import tn.esprit.projet_pi.entity.Reclamation;
import tn.esprit.projet_pi.entity.Role;
import tn.esprit.projet_pi.entity.User;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Cliquez sur le lien suivant pour réinitialiser votre mot de passe : " + resetUrl);

        mailSender.send(message);
        System.out.println("Email envoyé à : " + toEmail);
    }

    public void sendalertpwd(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour,\n\n"
                + "Nous avons reçu une demande de réinitialisation de votre mot de passe. "
                + "Si vous êtes à l'origine de cette demande, veuillez cliquer sur le lien ci-dessous pour réinitialiser votre mot de passe :\n\n"
                + "http://votre-domaine.com/reset-password?token=" + token + "\n\n"
                + "Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet e-mail. "
                + "Votre mot de passe restera inchangé.\n\n"
                + "Cordialement,\n"
                + "L'équipe de sécurité");

        mailSender.send(message);
        System.out.println("Email envoyé à : " + toEmail);
    }


    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:4200/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vérification de votre e-mail");
        message.setText("Cliquez sur le lien suivant pour vérifier votre e-mail : " + verificationUrl);

        mailSender.send(message);
        System.out.println("Email de vérification envoyé à : " + toEmail);
    }

    public void sendReclamationResponse(String recipientEmail, String subject, String message) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(recipientEmail); // This is where the error occurs
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendReclamationResponse(Reclamation savedReclamation) {
        if (savedReclamation.getUser() == null ||
                savedReclamation.getUser().getEmail() == null) {
            return; // Early return if no user or email
        }

        String recipientEmail = savedReclamation.getUser().getEmail();
        String subject = "Réclamation Reçue - Accusé de Réception";
        String message = String.format(
                "Votre réclamation a été reçue.\n" +
                        "Sujet: %s\n" +
                        "Description: %s\n" +
                        "Date: %s",
                savedReclamation.getSubject(),
                savedReclamation.getDescription(),
                savedReclamation.getDateCreated()
        );

        sendReclamationResponse(recipientEmail, subject, message);
    }
    public void sendMenuValidatedNotification(List<User> users, List<Menu> validatedMenus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Récupérer les emails des utilisateurs avec le rôle USER
            String[] userEmails = users.stream()
                    .filter(user -> user.getRole().equals(Role.User))
                    .map(User::getEmail)
                    .toArray(String[]::new);

            if (userEmails.length == 0) {
                LOGGER.warning("⚠️ Aucun utilisateur avec le rôle USER trouvé pour envoyer la notification.");
                return;
            }

            helper.setTo(userEmails);
            helper.setSubject("Menu de la semaine est pres");
            helper.setText(buildEmailBody(validatedMenus), true); // true pour activer le HTML

            mailSender.send(message);
            LOGGER.info("✅ Email de notification envoyé à " + userEmails.length + " utilisateurs.");
        } catch (MessagingException e) {
            LOGGER.severe("❌ Erreur lors de l'envoi de l'email : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    private String buildEmailBody(List<Menu> validatedMenus) {
        StringBuilder body = new StringBuilder();
        // Conteneur principal avec style moderne
        body.append("<div style=\"font-family: 'Helvetica Neue', Arial, sans-serif; max-width: 650px; margin: 0 auto; background-color: #f9f9f9; border-radius: 10px; overflow: hidden;\">");

        // En-tête avec couleur de fond
        body.append("<div style=\"background-color: #2ecc71; padding: 20px; text-align: center; color: white;\">");
        body.append("<h1 style=\"margin: 0; font-size: 24px;\">Menu de la semaine prêt</h1>");
        body.append("</div>");

        // Corps principal
        body.append("<div style=\"padding: 25px; color: #444;\">");
        body.append("<p style=\"font-size: 16px; line-height: 1.5;\">Bonjour,<br>Nous sommes ravis de vous informer que le menu de la semaine a été préparé et est prêt à être consulté. Voici un aperçu :</p>");

        // Tableau des menus avec style amélioré
        body.append("<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0; background-color: white; box-shadow: 0 2px 5px rgba(0,0,0,0.1);\">");
        body.append("<thead>");
        body.append("<tr style=\"background-color: #ecf0f1; color: #333;\">");
        body.append("<th style=\"padding: 12px; border-bottom: 2px solid #ddd; text-align: left;\">Date</th>");
        body.append("<th style=\"padding: 12px; border-bottom: 2px solid #ddd; text-align: left;\">Régime</th>");
        body.append("<th style=\"padding: 12px; border-bottom: 2px solid #ddd; text-align: left;\">Calories</th>");
        body.append("</tr>");
        body.append("</thead>");
        body.append("<tbody>");

        for (Menu menu : validatedMenus) {
            body.append("<tr style=\"border-bottom: 1px solid #eee;\">");
            body.append("<td style=\"padding: 12px;\">").append(menu.getDate()).append("</td>");
            body.append("<td style=\"padding: 12px;\">").append(menu.getRegime()).append("</td>");
            body.append("<td style=\"padding: 12px; color: #e67e22;\">").append(menu.getTotalCalories()).append(" kcal</td>");
            body.append("</tr>");
        }

        body.append("</tbody>");
        body.append("</table>");

        // Appel à l'action avec un bouton
        body.append("<p style=\"font-size: 16px; line-height: 1.5;\">Consultez les détails complets et personnalisez vos préférences dans l'application :</p>");
        body.append("<p style=\"text-align: center;\">");
        body.append("<a href=\"http://localhost:4200/menu\" style=\"display: inline-block; padding: 12px 25px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;\">Voir dans l'application</a>");
        body.append("</p>");

        // Pied de page
        body.append("<div style=\"margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; text-align: center; font-size: 14px; color: #777;\">");
        body.append("<p>Cordialement,<br>L'équipe de gestion des menus</p>");
        body.append("<p style=\"font-size: 12px;\">Cet email est automatique, merci de ne pas y répondre.</p>");
        body.append("</div>");

        body.append("</div>"); // Fin du corps principal
        body.append("</div>"); // Fin du conteneur principal

        return body.toString();
    }
    public void sendMenuRejectionNotification(List<User> staffUsers, List<Menu> rejectedMenus, String rejectionReason) {
        SimpleMailMessage message = new SimpleMailMessage();

        // Collecter les emails des membres du staff
        String[] staffEmails = staffUsers.stream()
                .map(User::getEmail)
                .toArray(String[]::new);

        message.setTo(staffEmails);
        message.setSubject("Notification de rejet de menus");

        // Construire le corps de l'email
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Les menus suivants ont été rejetés :\n\n");
        for (Menu menu : rejectedMenus) {
            emailBody.append("Menu ID: ").append(menu.getId())
                    .append(" - Date: ").append(menu.getDate())
                    .append("\n");
        }
        emailBody.append("\nRaison du rejet : ").append(rejectionReason).append("\n");
        emailBody.append("\nVeuillez prendre les mesures nécessaires pour regénérer un autre .");

        message.setText(emailBody.toString());

        try {
            mailSender.send(message);
            LOGGER.info("📧 Email de notification de rejet envoyé à " + staffEmails.length + " membres du staff");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de l'envoi de l'email de rejet : " + e.getMessage());
        }
    }

    private String buildRejectionEmailBody(List<Menu> rejectedMenus, String reason) {
        StringBuilder body = new StringBuilder();
        body.append("<h2>Rejet de menus</h2>");
        body.append("<p>Bonjour,<br>Certains menus ont été rejetés par le médecin. Voici les détails :</p>");
        body.append("<ul>");

        for (Menu menu : rejectedMenus) {
            body.append("<li>")
                    .append(menu.getDate())
                    .append(" - Régime : ")
                    .append(menu.getRegime())
                    .append("</li>");
        }

        body.append("</ul>");
        body.append("<p><strong>Raison du rejet :</strong> ").append(reason).append("</p>");
        body.append("<p>Merci de bien vouloir corriger ces menus et les soumettre à nouveau.</p>");
        body.append("<p>Cordialement,<br>L'équipe de gestion des menus</p>");

        return body.toString();
    }




    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


}
