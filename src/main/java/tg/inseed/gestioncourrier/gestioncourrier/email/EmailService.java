package tg.inseed.gestioncourrier.gestioncourrier.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Envoie l'email de bienvenue à un nouvel utilisateur avec logo embarqué
     */
    public void envoyerEmailBienvenue(String to, String prenom, String email, String motDePasse) {
        System.out.println("Début de l'envoi de l'email de bienvenue à : " + to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Bienvenue sur la plateforme de gestion des courriers - INSEED");

            Context context = new Context();
            context.setVariable("prenom", prenom);
            context.setVariable("email", email);
            context.setVariable("motDePasse", motDePasse);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("email/bienvenue", context);

            helper.setText(htmlContent, true);

            // Embarquer le logo depuis les ressources statiques
            ClassPathResource logo = new ClassPathResource("static/images/logo-inseed.png");
            helper.addInline("logoInseed", logo);

            mailSender.send(message);

            System.out.println("✅ Email de bienvenue envoyé avec succès (logo embarqué) à : " + to);

        } catch (Exception e) {
            System.err.println("❌ Échec de l'envoi de l'email de bienvenue à : " + to);
            e.printStackTrace();
        }
    }

    /**
     * Envoie l'email de réinitialisation de mot de passe (logo embarqué aussi recommandé)
     */
    public void envoyerEmailReinitialisation(String to, String prenom, String motDePasse) {
        System.out.println("Début de l'envoi de l'email de réinitialisation à : " + to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Réinitialisation de votre mot de passe - INSEED");

            Context context = new Context();
            context.setVariable("prenom", prenom);
            context.setVariable("motDePasse", motDePasse);
            context.setVariable("loginUrl", frontendUrl + "/login");

            String htmlContent = templateEngine.process("email/reinitialisation", context);

            helper.setText(htmlContent, true);

            // Embarquer le même logo pour cohérence
            ClassPathResource logo = new ClassPathResource("static/images/logo-inseed.png");
            helper.addInline("logoInseed", logo);

            mailSender.send(message);

            System.out.println("✅ Email de réinitialisation envoyé avec succès (logo embarqué) à : " + to);

        } catch (Exception e) {
            System.err.println("❌ Échec de l'envoi de l'email de réinitialisation à : " + to);
            e.printStackTrace();
        }
    }
}