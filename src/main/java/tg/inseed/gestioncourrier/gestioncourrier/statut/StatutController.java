package tg.inseed.gestioncourrier.gestioncourrier.statut;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des statuts.
 *
 * <p>Ce contrôleur expose des endpoints permettant de consulter,
 * créer, modifier et supprimer des statuts. Les statuts définissent
 * le cycle de vie des courriers (exemple : "En attente", "Traité", "Classé").</p>
 *
 * <p>Certains endpoints sont accessibles à tous les utilisateurs,
 * tandis que d’autres nécessitent des rôles spécifiques (ADMIN, DG).</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@RestController
@RequestMapping("/api/statuts")
public class StatutController {

    @Autowired
    private StatutService statutService;

    /**
     * Récupère tous les statuts actifs.
     * <p>Utilisé notamment pour alimenter les menus déroulants (dropdowns).</p>
     *
     * @return réponse contenant la liste des statuts actifs
     */
    @GetMapping("/actifs")
    public ResponseEntity<?> getStatutsActifs() {
        try {
            List<Statut> statuts = statutService.getStatutsActifs();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", statuts.size(),
                "statuts", statuts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les statuts actifs avec le nombre de courriers associés.
     *
     * @return réponse contenant la liste des statuts et leur comptage
     */
    @GetMapping("/avec-count")
    public ResponseEntity<?> getStatutsAvecCount() {
        try {
            List<Map<String, Object>> statuts = statutService.getStatutsAvecCount();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statuts", statuts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère les statistiques globales des courriers par statut.
     * Accessible uniquement aux administrateurs et directeurs généraux.
     *
     * @return réponse contenant les statistiques (total, par statut, nombre de statuts)
     */
    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DG')")
    public ResponseEntity<?> getStatistiques() {
        try {
            Map<String, Object> stats = statutService.getStatistiques();
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
     * Récupère la liste complète des statuts.
     * Accessible uniquement aux administrateurs.
     *
     * @return réponse contenant tous les statuts
     */
    @GetMapping("/lister")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT', 'ROLE_DG', 'ROLE_DIRECTION')")
    public ResponseEntity<?> getAllStatuts() {
        try {
            List<Statut> statuts = statutService.getAllStatuts();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", statuts.size(),
                "statuts", statuts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère un statut par son identifiant.
     *
     * @param id identifiant du statut
     * @return réponse contenant le statut correspondant
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStatutById(@PathVariable Long id) {
        try {
            Statut statut = statutService.getStatutById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statut", statut
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Récupère un statut par son code unique.
     *
     * @param code code du statut
     * @return réponse contenant le statut correspondant
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getStatutByCode(@PathVariable String code) {
        try {
            Statut statut = statutService.getStatutByCode(code);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "statut", statut
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Crée un nouveau statut.
     * Accessible uniquement aux administrateurs.
     *
     * @param statut objet Statut à créer
     * @return réponse confirmant la création avec le statut créé
     */
    @PostMapping("/ajouter")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createStatut(@RequestBody Statut statut) {
        try {
            Statut created = statutService.createStatut(statut);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "✅ Statut créé avec succès",
                "statut", created
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Met à jour un statut existant.
     * Accessible uniquement aux administrateurs.
     *
     * @param id identifiant du statut à modifier
     * @param statut objet contenant les nouvelles valeurs
     * @return réponse confirmant la modification avec le statut mis à jour
     */
    @PutMapping("/modify/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestBody Statut statut) {
        try {
            Statut updated = statutService.updateStatut(id, statut);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Statut modifié avec succès",
                "statut", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Supprime un statut existant.
     * Accessible uniquement aux administrateurs.
     *
     * @param id identifiant du statut à supprimer
     * @return réponse confirmant la suppression
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteStatut(@PathVariable Long id) {
        try {
            statutService.deleteStatut(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Statut supprimé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }
}
