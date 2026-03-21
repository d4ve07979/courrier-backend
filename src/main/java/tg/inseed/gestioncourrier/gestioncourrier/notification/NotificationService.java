package tg.inseed.gestioncourrier.gestioncourrier.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification creerNotification(Utilisateur destinataire, String message, Long idCourrier) {
        Notification notif = new Notification();
        notif.setDestinataire(destinataire);
        notif.setMessage(message);
        notif.setIdCourrier(idCourrier);
        notif = notificationRepository.save(notif);

        // Envoi en temps réel via WebSocket
        messagingTemplate.convertAndSendToUser(
            destinataire.getEmailUtilisateur(),
            "/queue/notifications",
            notif
        );

        return notif;
    }

    public List<Notification> getNotificationsUtilisateur(Utilisateur utilisateur) {
        return notificationRepository.findByDestinataireOrderByDateEnvoiDesc(utilisateur);
    }

    public void marquerCommeLue(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLue(true);
            notificationRepository.save(n);
        });
    }
    // Récupérer une notification par ID (pour sécurité)
public Notification getNotificationById(Long id) {
    return notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification introuvable"));
}

// Marquer tout comme lu pour un utilisateur
public void marquerToutCommeLu(Utilisateur utilisateur) {
    List<Notification> nonLues = notificationRepository.findNonLuesByDestinataire(utilisateur);
    nonLues.forEach(n -> n.setLue(true));
    notificationRepository.saveAll(nonLues);
}
}