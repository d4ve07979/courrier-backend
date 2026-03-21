package tg.inseed.gestioncourrier.gestioncourrier.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer toutes les notifications de l'utilisateur connecté
     * Triées par date décroissante
     */
    @GetMapping("/mes-notifications")
    public ResponseEntity<?> getMesNotifications(Authentication authentication) {
        try {
            Utilisateur utilisateur = getUtilisateurConnecte(authentication);

            List<Notification> notifications = notificationService.getNotificationsUtilisateur(utilisateur);

            long nonLues = notifications.stream().filter(n -> !n.isLue()).count();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", notifications.size());
            response.put("non_lues", nonLues);
            response.put("notifications", notifications);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Erreur lors de la récupération des notifications"
                    ));
        }
    }

    /**
     * Marquer une notification spécifique comme lue
     */
    @PutMapping("/{id}/lire")
    public ResponseEntity<?> marquerCommeLue(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Utilisateur utilisateur = getUtilisateurConnecte(authentication);

            Notification notif = notificationService.getNotificationById(id); // À ajouter dans le service si besoin

            // Sécurité : on vérifie que la notification appartient bien à l'utilisateur
            if (!notif.getDestinataire().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Accès refusé à cette notification"));
            }

            notificationService.marquerCommeLue(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification marquée comme lue"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Marquer TOUTES les notifications comme lues
     */
    @PutMapping("/tout-lire")
    public ResponseEntity<?> marquerToutCommeLu(Authentication authentication) {
        try {
            Utilisateur utilisateur = getUtilisateurConnecte(authentication);

            notificationService.marquerToutCommeLu(utilisateur);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Toutes les notifications ont été marquées comme lues"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Méthode utilitaire
    private Utilisateur getUtilisateurConnecte(Authentication authentication) {
        String email = authentication.getName();
        return utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable"
                ));
    }
}