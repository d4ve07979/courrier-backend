package tg.inseed.gestioncourrier.gestioncourrier.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public boolean envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            Email from = new Email(fromEmail, "INSEED");
            Email to = new Email(destinataire);
            Content content = new Content("text/html", contenu);
            Mail mail = new Mail(from, sujet, to, content);

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
}