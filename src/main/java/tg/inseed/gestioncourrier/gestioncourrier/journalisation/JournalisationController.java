package tg.inseed.gestioncourrier.gestioncourrier.journalisation;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.EntiteConcernee;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.TypeAction;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

/**
 * Contrôleur REST pour la gestion des journalisations.
 *
 * <p>Ce contrôleur expose des endpoints permettant aux administrateurs
 * et aux utilisateurs de consulter les logs d’actions effectuées dans
 * le système (connexion, modification, suppression, etc.).</p>
 *
 * <p>Les endpoints sont sécurisés par Spring Security et certains
 * nécessitent le rôle ADMIN.</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@RestController
@RequestMapping("/api/journalisation")
public class JournalisationController {

    @Autowired
    private JournalisationService journalisationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Récupère tous les logs avec pagination.
     * Accessible uniquement aux administrateurs.
     *
     * @param page numéro de la page (par défaut 0)
     * @param size taille de la page (par défaut 50)
     * @return réponse contenant les logs paginés
     */
    @GetMapping("/tous")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<Journalisation> logs = journalisationService.getLogsPagines(page, size);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "page", page,
                "size", size,
                "total", logs.getTotalElements(),
                "total_pages", logs.getTotalPages(),
                "logs", logs.getContent()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les logs avec filtres multiples (utilisateur, type d’action,
     * entité concernée, période).
     * Accessible uniquement aux administrateurs.
     *
     * @param idUtilisateur identifiant de l’utilisateur (optionnel)
     * @param typeAction type d’action (optionnel)
     * @param entite entité concernée (optionnel)
     * @param debut date de début (optionnel)
     * @param fin date de fin (optionnel)
     * @param page numéro de la page
     * @param size taille de la page
     * @return réponse contenant les logs filtrés
     */
    @GetMapping("/filtrer")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getLogsAvecFiltres(
            @RequestParam(required = false) Long idUtilisateur,
            @RequestParam(required = false) TypeAction typeAction,
            @RequestParam(required = false) EntiteConcernee entite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Utilisateur utilisateur = null;
            if (idUtilisateur != null) {
                utilisateur = utilisateurRepository.findById(idUtilisateur).orElse(null);
            }
            
            Page<Journalisation> logs = journalisationService.getLogsAvecFiltres(
                utilisateur, typeAction, entite, debut, fin, page, size
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filtres", Map.of(
                    "utilisateur", idUtilisateur != null ? idUtilisateur : "tous",
                    "type_action", typeAction != null ? typeAction : "tous",
                    "entite", entite != null ? entite : "toutes"
                ),
                "total", logs.getTotalElements(),
                "logs", logs.getContent()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les logs d’un utilisateur spécifique.
     * Accessible uniquement aux administrateurs.
     *
     * @param idUtilisateur identifiant de l’utilisateur
     * @return réponse contenant les logs de l’utilisateur
     */
    @GetMapping("/utilisateur/{idUtilisateur}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getLogsUtilisateur(@PathVariable Long idUtilisateur) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            var logs = journalisationService.getLogsUtilisateur(utilisateur);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "utilisateur", Map.of(
                    "id", utilisateur.getIdUtilisateur(),
                    "nom", utilisateur.getNomUtilisateur(),
                    "prenom", utilisateur.getPrenomUtilisateur(),
                    "email", utilisateur.getEmailUtilisateur()
                ),
                "count", logs.size(),
                "logs", logs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les logs de l’utilisateur connecté (ses propres actions).
     *
     * @param authentication objet Spring Security contenant l’utilisateur connecté
     * @return réponse contenant les logs de l’utilisateur
     */
    @GetMapping("/mes-actions")
    public ResponseEntity<?> getMesActions(Authentication authentication) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            var logs = journalisationService.getLogsUtilisateur(utilisateur);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", logs.size(),
                "logs", logs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère l’historique des actions liées à une entité spécifique
     * (exemple : tous les logs d’un courrier).
     * Accessible uniquement aux administrateurs.
     *
     * @param entite entité concernée
     * @param idEntite identifiant de l’entité
     * @return réponse contenant les logs de l’entité
     */
    @GetMapping("/entite/{entite}/{idEntite}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getLogsEntite(
            @PathVariable EntiteConcernee entite,
            @PathVariable Long idEntite) {
        try {
            var logs = journalisationService.getLogsEntite(entite, idEntite);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "entite", entite,
                "id_entite", idEntite,
                "count", logs.size(),
                "logs", logs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les statistiques globales des logs.
     * Accessible uniquement aux administrateurs.
     *
     * @return réponse contenant les statistiques (total, par type, derniers logs)
     */
    @GetMapping("/statistiques")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getStatistiques() {
        try {
            var stats = journalisationService.getStatistiques();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistiques", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

        /**
     * Supprime une entrée de journalisation par son identifiant.
     * Accessible uniquement aux administrateurs.
     *
     * @param id identifiant du log à supprimer
     * @return réponse confirmant la suppression ou indiquant une erreur
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteLog(@PathVariable Long id) {
        try {
            journalisationService.deleteJournalisation(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Log supprimé"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }
}
