package tg.inseed.gestioncourrier.gestioncourrier.decharge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.CourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

@RestController
@RequestMapping("/api/decharges")
public class DechargeController {

    @Autowired
    private final DechargeService dechargeService;

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public DechargeController(DechargeService dechargeService) {
        this.dechargeService = dechargeService;
    }

    /**
     * Accuser réception électroniquement (utilisateur connecté)
     */
    @PostMapping("/courrier/{idCourrier}/accuser-reception")
    public ResponseEntity<?> accuserReception(
            @PathVariable Long idCourrier,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository
                .findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

            Courrier courrier = courrierRepository.findById(idCourrier)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Courrier introuvable"));

            String observation = body != null ? body.getOrDefault("observation", "") : "";

            Decharge decharge = new Decharge();
            decharge.setCourrier(courrier);
            decharge.setUtilisateur(utilisateur);
            decharge.setDateSignature(LocalDateTime.now());
            decharge.setObservation(observation);
            decharge.setTypeSignature("ELECTRONIQUE");
            decharge.setNomSignataire(utilisateur.getPrenomUtilisateur()
                + " " + utilisateur.getNomUtilisateur());

            Decharge saved = dechargeService.createDecharge(decharge);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Réception accusée avec succès",
                "decharge", saved
            ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Enregistrer une décharge physique (secrétariat enregistre pour le coursier)
     */
    @PostMapping("/courrier/{idCourrier}/physique")
    public ResponseEntity<?> enregistrerDechargePhysique(
            @PathVariable Long idCourrier,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            Utilisateur enregistrePar = utilisateurRepository
                .findByEmailUtilisateur(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

            Courrier courrier = courrierRepository.findById(idCourrier)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Courrier introuvable"));

            String nomSignataire = body.get("nomSignataire");
            String observation = body.getOrDefault("observation", "");

            if (nomSignataire == null || nomSignataire.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du signataire est obligatoire"
                ));
            }

            Decharge decharge = new Decharge();
            decharge.setCourrier(courrier);
            decharge.setUtilisateur(enregistrePar);
            decharge.setDateSignature(LocalDateTime.now());
            decharge.setObservation(observation);
            decharge.setTypeSignature("PHYSIQUE");
            decharge.setNomSignataire(nomSignataire.trim());

            Decharge saved = dechargeService.createDecharge(decharge);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Décharge physique enregistrée",
                "decharge", saved
            ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Récupérer toutes les décharges d'un courrier
     */
    @GetMapping("/courrier/{idCourrier}")
    public ResponseEntity<?> getDechargesByCourrier(@PathVariable Long idCourrier) {
        try {
            Courrier courrier = courrierRepository.findById(idCourrier)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Courrier introuvable"));
            List<Decharge> decharges = dechargeService.getDechargesByCourrier(courrier);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "decharges", decharges,
                "total", decharges.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Liste complète (admin)
     */
    @GetMapping("/list")
    public List<Decharge> getAllDecharges() {
        return dechargeService.getAllDecharges();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteDecharge(@PathVariable Long id) {
        dechargeService.deleteDecharge(id);
    }
}