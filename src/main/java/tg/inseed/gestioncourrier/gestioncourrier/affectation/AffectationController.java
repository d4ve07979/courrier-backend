package tg.inseed.gestioncourrier.gestioncourrier.affectation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

/**
 * Contrôleur REST pour gérer les affectations.
 * Permet de créer, consulter, modifier et supprimer les affectations.
 */
@RestController // Indique que cette classe est un contrôleur REST
@RequestMapping("/api/affectations") // Préfixe des routes
public class AffectationController {

    @Autowired // Injection du service
    private final AffectationService affectationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AffectationRepository affectationRepository;
    public List<Affectation> getMesAffectations(Utilisateur utilisateur) {
    return affectationRepository.findByUtilisateur(utilisateur); // ✅ Correct
}


    public AffectationController(AffectationService affectationService) {
        this.affectationService = affectationService;
    }

    @PostMapping("/ajouter")
public ResponseEntity<?> createAffectation(
        @Valid @RequestBody AffectationRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest) {  // ← Ajout indispensable

    try {
        // 🔐 Récupérer l'utilisateur qui effectue l'affectation (l'affecteur)
        String email = authentication.getName();
        Utilisateur affecteur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Utilisateur affecteur introuvable"
                ));

        // 🎯 Récupérer l'utilisateur destinataire de l'affectation
        Utilisateur utilisateurAffecte = utilisateurRepository.findById(request.getIdUtilisateur())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Utilisateur à affecter introuvable"
                ));

        // 📝 Créer l'affectation avec notification en temps réel + journalisation
        Affectation affectation = affectationService.createFromDto(
                request, 
                utilisateurAffecte, 
                affecteur,          // ← Nouveau paramètre
                httpRequest         // ← Nouveau paramètre
        );

        // Retour JSON structuré (comme dans vos autres contrôleurs)
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Courrier affecté avec succès à " + 
                           utilisateurAffecte.getPrenomUtilisateur() + " " + 
                           utilisateurAffecte.getNomUtilisateur(),
                "affectation", affectation
        ));

    } catch (ResponseStatusException e) {
        // Erreurs connues (404, etc.)
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of(
                        "success", false,
                        "message", e.getReason()
                ));
    } catch (Exception e) {
        // Erreurs inattendues
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "message", "❌ Erreur lors de l'affectation : " + e.getMessage()
                ));
    }
}

    @GetMapping("/list")
    public List<Affectation> getAllAffectations() {
        return affectationService.getAllAffectations();
    }

    @GetMapping("/{id}")
    public Affectation getAffectationById(@PathVariable Long id) {
        return affectationService.getAffectationById(id);
    }

    @PutMapping("/update/{id}")
    public Affectation updateAffectation(@PathVariable Long id, @RequestBody Affectation affectation) {
        return affectationService.updateAffectation(id, affectation);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAffectation(@PathVariable Long id) {
        affectationService.deleteAffectation(id);
    }

    /**
     * Endpoint GET pour consulter les affectations du Directeur Général connecté.
     * Accessible uniquement aux utilisateurs ayant le rôle DG.
     *
     * @param authentication Contexte d’authentification Spring Security (utilisateur connecté)
     * @return Liste des affectations liées au DG
     * @throws ResponseStatusException si l’utilisateur n’est pas trouvé en base
     */
  @GetMapping("/mes-affectations")
public ResponseEntity<List<AffectationDTO>> getMesAffectations(Authentication authentication) {
    Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    List<AffectationDTO> affectations = affectationService.getMesAffectationsDto(utilisateur);
    return ResponseEntity.ok(affectations);
}

}


