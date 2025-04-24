package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.ReclamationRepository;
import tn.esprit.projet_pi.entity.Reclamation;
import tn.esprit.projet_pi.entity.ReclamationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ReclamationAutoResponseService {

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private EmailService emailService;

    private static final Logger LOGGER = Logger.getLogger(ReclamationAutoResponseService.class.getName());


    @Scheduled(fixedDelay = 1000)
    public void processUnrespondedReclamations() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(1);
        List<Reclamation> unrespondedReclamations = reclamationRepository
                .findByStatusAndDateCreatedBefore(ReclamationStatus.PENDING, recentTime);


        for (Reclamation reclamation : unrespondedReclamations) {
            // Send an automated response email
            sendAutomatedResponse(reclamation);

            // Update the status to indicate an automated response has been sent
            reclamation.setStatus(ReclamationStatus.IN_PROGRESS);
            reclamationRepository.save(reclamation);
        }
    }

    private void sendAutomatedResponse(Reclamation reclamation) {
        // Ensure user and email are not null
        if (reclamation.getUser() == null || reclamation.getUser().getEmail() == null) {
            LOGGER.warning("Impossible d'envoyer la réponse automatisée: utilisateur ou email manquant");
            return;
        }

        String recipientEmail = reclamation.getUser().getEmail();
        String subject = "Accusé de réception - Réclamation #" + reclamation.getId();
        String message = buildAutomatedResponseMessage(reclamation);
        Reclamation reclamation1=reclamation;

        try {
            emailService.sendReclamationResponse(recipientEmail, subject, message, reclamation1);
            LOGGER.info("Réponse automatisée envoyée pour la réclamation #" + reclamation.getId());
        } catch (Exception e) {
            LOGGER.severe("Échec d'envoi de la réponse automatisée pour la réclamation #" + reclamation.getId() + ": " + e.getMessage());
        }
    }

    private String buildAutomatedResponseMessage(Reclamation reclamation) {
        // Format de la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = reclamation.getDateCreated().format(formatter);

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Accusé de réception de votre réclamation</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            line-height: 1.6;\n" +
                "            color: #333;\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .container {\n" +
                "            background-color: #f9f9f9;\n" +
                "            border-radius: 5px;\n" +
                "            padding: 20px;\n" +
                "            border: 1px solid #ddd;\n" +
                "        }\n" +
                "        .header {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 20px;\n" +
                "            padding-bottom: 15px;\n" +
                "            border-bottom: 2px solid #e0e0e0;\n" +
                "        }\n" +
                "        .header h2 {\n" +
                "            color: #2c3e50;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        .ref-number {\n" +
                "            background-color: #3498db;\n" +
                "            color: white;\n" +
                "            padding: 5px 10px;\n" +
                "            border-radius: 3px;\n" +
                "            font-weight: bold;\n" +
                "            display: inline-block;\n" +
                "            margin-top: 10px;\n" +
                "        }\n" +
                "        .details {\n" +
                "            background-color: #fff;\n" +
                "            padding: 15px;\n" +
                "            border-radius: 4px;\n" +
                "            border-left: 4px solid #f39c12;\n" +
                "            margin: 15px 0;\n" +
                "        }\n" +
                "        .details table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "        }\n" +
                "        .details td {\n" +
                "            padding: 8px;\n" +
                "            border-bottom: 1px solid #eee;\n" +
                "        }\n" +
                "        .details td:first-child {\n" +
                "            font-weight: bold;\n" +
                "            width: 30%;\n" +
                "            color: #555;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            margin-top: 20px;\n" +
                "            font-size: 12px;\n" +
                "            text-align: center;\n" +
                "            color: #777;\n" +
                "            border-top: 1px solid #eee;\n" +
                "            padding-top: 10px;\n" +
                "        }\n" +
                "        .status-badge {\n" +
                "            background-color: #e67e22;\n" +
                "            color: white;\n" +
                "            padding: 3px 8px;\n" +
                "            border-radius: 12px;\n" +
                "            font-size: 12px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>Accusé de réception de votre réclamation</h2>\n" +
                "            <div class=\"ref-number\">Référence #" + reclamation.getId() + "</div>\n" +
                "        </div>\n" +
                "        <p>Bonjour " + (reclamation.getUser().getNom() != null ? reclamation.getUser().getNom() : "") + ",</p>\n" +
                "        <p>Nous vous confirmons la bonne réception de votre réclamation. Celle-ci a été enregistrée dans notre système et sera traitée dans les meilleurs délais.</p>\n" +
                "        <div class=\"details\">\n" +
                "            <table>\n" +
                "                <tr>\n" +
                "                    <td>Référence</td>\n" +
                "                    <td>#" + reclamation.getId() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>Sujet</td>\n" +
                "                    <td>" + reclamation.getSubject() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>Description</td>\n" +
                "                    <td>" + reclamation.getDescription() + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>Date de soumission</td>\n" +
                "                    <td>" + formattedDate + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>Statut actuel</td>\n" +
                "                    <td><span class=\"status-badge\">" + reclamation.getStatus() + "</span></td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        <p>Un membre de notre équipe examinera votre réclamation et vous contactera dans les plus brefs délais.</p>\n" +
                "        <p>Si vous souhaitez fournir des informations complémentaires, n'hésitez pas à nous répondre en mentionnant la référence de votre réclamation.</p>\n" +
                "        <p>Nous vous remercions de votre patience et de votre compréhension.</p>\n" +
                "        <p>Cordialement,<br><strong>L'équipe du Service Client</strong></p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Cet e-mail a été envoyé automatiquement, merci de ne pas y répondre directement.</p>\n" +
                "            <p>© " + LocalDate.now().getYear() + " - Tous droits réservés</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}