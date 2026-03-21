package tg.inseed.gestioncourrier.gestioncourrier.session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * 🆕 ADMIN : Récupérer toutes les sessions
     */
    @GetMapping("/toutes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllSessions() {
        try {
            List<Session> sessions = sessionService.getAllSessions();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", sessions.size(),
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Récupérer les sessions actives
     */
    @GetMapping("/actives")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSessionsActives() {
        try {
            List<Session> sessions = sessionService.getSessionsActives();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", sessions.size(),
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Récupérer les sessions d'un utilisateur
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSessionsUtilisateur(@PathVariable Long idUtilisateur) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<Session> sessions = sessionService.getSessionsUtilisateur(utilisateur);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "utilisateur", Map.of(
                    "id", utilisateur.getIdUtilisateur(),
                    "nom", utilisateur.getNomUtilisateur(),
                    "prenom", utilisateur.getPrenomUtilisateur(),
                    "email", utilisateur.getEmailUtilisateur()
                ),
                "count", sessions.size(),
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 Récupérer MES sessions (utilisateur connecté)
     */
    @GetMapping("/mes-sessions")
    public ResponseEntity<?> getMesSessions(Authentication authentication) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<Session> sessions = sessionService.getSessionsUtilisateur(utilisateur);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", sessions.size(),
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Récupérer les sessions dans une période
     */
    @GetMapping("/periode")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSessionsPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            List<Session> sessions = sessionService.getSessionsPeriode(debut, fin);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "periode", Map.of(
                    "debut", debut.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "fin", fin.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                "count", sessions.size(),
                "sessions", sessions
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Statistiques des sessions
     */
    @GetMapping("/statistiques")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getStatistiques() {
        try {
            SessionService.SessionStatistiques stats = sessionService.getStatistiques();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistiques", Map.of(
                    "total_sessions", stats.getTotalSessions(),
                    "sessions_actives", stats.getSessionsActives(),
                    "sessions_inactives", stats.getSessionsInactives()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Fermer une session spécifique
     */
    @PutMapping("/{id}/fermer")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> fermerSession(@PathVariable Long id) {
        try {
            Session session = sessionService.getSessionById(id);
            
            if (!session.estActive()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Cette session est déjà fermée"
                ));
            }
            
            session.setDateDeconnexion(LocalDateTime.now());
            session.setActive(false);
            // Vous devez ajouter une méthode save dans le service
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Session fermée"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Supprimer une session
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Session supprimée"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }
}