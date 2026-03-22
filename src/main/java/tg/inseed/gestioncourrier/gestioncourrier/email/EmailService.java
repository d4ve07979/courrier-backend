package tg.inseed.gestioncourrier.gestioncourrier.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${spring.mail.from:inseedmail@gmail.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Méthode générique d'envoi via SendGrid
     */
    private boolean envoyerEmail(String to, String sujet, String htmlContent) {
        try {
            Email from = new Email(fromEmail, "INSEED");
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, sujet, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("📧 SendGrid status: " + response.getStatusCode());
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (IOException e) {
            System.err.println("❌ Erreur SendGrid: " + e.getMessage());
            return false;
        }
    }

    /**
     * Envoie l'email de bienvenue à un nouvel utilisateur
     */
    public void envoyerEmailBienvenue(String to, String prenom, String email, String motDePasse) {
        System.out.println("Début de l'envoi de l'email de bienvenue à : " + to);

        try {
            Context context = new Context();
            context.setVariable("prenom", prenom);
            context.setVariable("email", email);
            context.setVariable("motDePasse", motDePasse);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("email/bienvenue", context);

            boolean success = envoyerEmail(
                to,
                "Bienvenue sur la plateforme de gestion des courriers - INSEED",
                htmlContent
            );

            if (success) {
                System.out.println("✅ Email de bienvenue envoyé avec succès à : " + to);
            } else {
                System.err.println("❌ Échec de l'envoi de l'email de bienvenue à : " + to);
            }

        } catch (Exception e) {
            System.err.println("❌ Échec de l'envoi de l'email de bienvenue à : " + to);
            e.printStackTrace();
        }
    }

    /**
     * Envoie l'email de réinitialisation de mot de passe
     */
    public void envoyerEmailReinitialisation(String to, String prenom, String motDePasse) {
        System.out.println("Début de l'envoi de l'email de réinitialisation à : " + to);

        try {
            Context context = new Context();
            context.setVariable("prenom", prenom);
            context.setVariable("motDePasse", motDePasse);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("email/reinitialisation", context);

            boolean success = envoyerEmail(
                to,
                "Réinitialisation de votre mot de passe - INSEED",
                htmlContent
            );

            if (success) {
                System.out.println("✅ Email de réinitialisation envoyé avec succès à : " + to);
            } else {
                System.err.println("❌ Échec de l'envoi de l'email de réinitialisation à : " + to);
            }

        } catch (Exception e) {
            System.err.println("❌ Échec de l'envoi de l'email de réinitialisation à : " + to);
            e.printStackTrace();
        }
    }
}