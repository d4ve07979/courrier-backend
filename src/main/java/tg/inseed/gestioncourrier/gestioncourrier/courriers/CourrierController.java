package tg.inseed.gestioncourrier.gestioncourrier.courriers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission.CourrierCompletRequest;
import tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission.FicheDeTransmission;
import tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission.FicheDeTransmissionRepository;
import tg.inseed.gestioncourrier.gestioncourrier.destinataitre.DestinataireRepository;
import tg.inseed.gestioncourrier.gestioncourrier.expediteur.ExpediteurRepository;
import tg.inseed.gestioncourrier.gestioncourrier.fichiers.FichierCourrier;
import tg.inseed.gestioncourrier.gestioncourrier.fichiers.FichierCourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.EntiteConcernee;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.JournalisationService;
import tg.inseed.gestioncourrier.gestioncourrier.notification.NotificationService;
import tg.inseed.gestioncourrier.gestioncourrier.statut.Statut;
import tg.inseed.gestioncourrier.gestioncourrier.statut.StatutRepository;
import tg.inseed.gestioncourrier.gestioncourrier.statut.StatutService;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.TypeCourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

@RestController
@RequestMapping("/api/courriers")
public class CourrierController {

    @Autowired
    private CourrierService courrierService;

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private FichierCourrierRepository fichierCourrierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

     @Autowired
    private FicheDeTransmissionRepository ficheDeTransmissionRepository; // 🆕 AJOUT

    @Autowired
    private ExpediteurRepository expediteurRepository; // 🆕 AJOUT

    @Autowired
    private DestinataireRepository destinataireRepository; // 🆕 AJOUT

    @Autowired
    private TypeCourrierRepository typeCourrierRepository; // 🆕 AJOUT

    @Autowired
    private StatutRepository statutRepository; // 🆕 AJOUT

     @Autowired
    private StatutService statutService; // 🆕 AJOUT

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JournalisationService journalisationService;

    @Value("${courrier.upload.dir:uploads/courriers}")
    private String uploadDir;

    /**
     * 🔐 SÉCURISÉ : Récupérer MES courriers 
     * (créés par moi + affectés à moi ou à ma direction)
     */
    @GetMapping("/mes-courriers")
    public ResponseEntity<?> getMesCourriers(Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

            // Récupérer TOUS les courriers accessibles
            List<Courrier> courriers = courrierService.getCourriersAccessiblesParUtilisateur(utilisateur);

            // Statistiques
            long courriersCreesParMoi = courriers.stream()
                .filter(c -> c.getCreateur() != null && 
                           c.getCreateur().getIdUtilisateur().equals(utilisateur.getIdUtilisateur()))
                .count();

            System.out.println("📧 " + courriers.size() + " courrier(s) pour " + email + 
                             " (créés: " + courriersCreesParMoi + ", affectés: " + (courriers.size() - courriersCreesParMoi) + ")");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "total", courriers.size(),
                "crees_par_moi", courriersCreesParMoi,
                "affectes_a_moi", courriers.size() - courriersCreesParMoi,
                "courriers", courriers
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
 * 🔐 SÉCURISÉ : Consulter UN courrier avec TOUTES ses relations chargées
 */
@GetMapping("/{id}")
public ResponseEntity<?> getCourrierById(
        @PathVariable Long id, 
        Authentication authentication) {
    try {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

        // Vérifier l'accès
        if (!courrierService.utilisateurPeutAccederAuCourrier(id, utilisateur)) {
            System.out.println("🔒 Accès refusé au courrier " + id + " pour " + email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "🔒 Vous n'avez pas accès à ce courrier"
            ));
        }

        // 🔑 CORRECTION CRITIQUE : Charger le courrier avec TOUTES les relations
        Courrier courrier = courrierRepository.findByIdWithAllRelations(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "❌ Courrier introuvable"));

        // Récupérer les fichiers (au cas où la relation ne soit pas chargée)
        List<FichierCourrier> fichiers = fichierCourrierRepository.findByCourrier(courrier);

        // Relation utilisateur/courrier
        String relation = "affecté";
        if (courrier.getCreateur() != null && 
            courrier.getCreateur().getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {
            relation = "créateur";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("relation", relation);
        response.put("courrier", courrier);

        // Fiche de transmission (déjà chargée grâce au JOIN FETCH)
        if (courrier.getFicheDeTransmission() != null) {
            response.put("fiche_transmission", courrier.getFicheDeTransmission());
        }

        // Fichiers avec URL de download
        response.put("fichiers", fichiers.stream().map(f -> Map.of(
            "id_fichier", f.getId(),
            "nom_fichier", f.getNomFichier(),
            "url_download", "/api/courriers/fichier/" + f.getId() + "/download"
        )).collect(Collectors.toList()));

        System.out.println("✅ Détails courrier " + id + " chargés pour " + email + " (" + relation + ")");

        return ResponseEntity.ok(response);

    } catch (ResponseStatusException e) {
        throw e; // Spring gère déjà le statut
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur serveur : " + e.getMessage()
        ));
    }
}

    /**
     * ⚠️ ADMIN UNIQUEMENT : Lister tous les courriers
     */
    @GetMapping("/afficher")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllCourriers() {
        try {
            List<Courrier> courriers = courrierService.getAllCourriers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", courriers.size(),
                "courriers", courriers
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 Créer un courrier simple (le créateur est automatiquement défini)
     */
    @PostMapping("/creer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT', 'ROLE_DG')")
    public ResponseEntity<?> createCourrier(
            @RequestBody Courrier courrier,
            Authentication authentication,
        HttpServletRequest request) {
        try {
            String email = authentication.getName();
            Utilisateur createur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

            Courrier saved = courrierService.createCourrier(courrier, createur);

             // 🆕 JOURNALISATION AUTOMATIQUE
        journalisationService.logCreation(
            EntiteConcernee.COURRIER,
            saved.getIdCourrier(),
            "Création du courrier : " + saved.getObjet(),
            createur,
            request
        );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "✅ Courrier créé avec succès",
                "courrier", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

   /**
 * 🆕 Créer un courrier AVEC fichier joint
 * Validation automatique du type de courrier
 */
@PostMapping("/creer-avec-fichier")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT', 'ROLE_DG')")
public ResponseEntity<?> createCourrierAvecFichier(
        @RequestPart("courrier") String courrierJson,
        @RequestPart("file") MultipartFile file,
        Authentication authentication,
        HttpServletRequest request) {
    try {
        String email = authentication.getName();
        Utilisateur createur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

        // Convertir JSON en objet Courrier
        ObjectMapper objectMapper = new ObjectMapper();
        Courrier courrier = objectMapper.readValue(courrierJson, Courrier.class);

        // 🆕 VALIDATION : Vérifier que le type de courrier existe et est actif
        if (courrier.getTypeCourrier() == null || courrier.getTypeCourrier().getIdType() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Le type de courrier est obligatoire"
            ));
        }

        // La validation est maintenant gérée dans le service
        // Si le type est inactif, une exception sera levée
        Courrier savedCourrier = courrierService.createCourrier(courrier, createur);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Fichier manquant ou vide"
            ));
        }

        // Sauvegarder le fichier
        String nomFichier = StringUtils.cleanPath(file.getOriginalFilename());
        Path dossier = Paths.get(uploadDir, String.valueOf(savedCourrier.getIdCourrier()));
        Files.createDirectories(dossier);

        Path chemin = dossier.resolve(nomFichier);
        Files.copy(file.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

        // Enregistrer la référence du fichier
        FichierCourrier fichier = new FichierCourrier();
        fichier.setNomFichier(nomFichier);
        fichier.setCheminFichier(chemin.toString());
        fichier.setCourrier(savedCourrier);
        fichierCourrierRepository.save(fichier);

        // 🆕 JOURNALISATION
        journalisationService.logCreation(
            EntiteConcernee.COURRIER,
            savedCourrier.getIdCourrier(),
            "Création du courrier avec fichier : " + savedCourrier.getObjet(),
            createur,
            request
        );

        System.out.println("📎 Courrier + fichier créés par " + email + 
                         " (Type: " + savedCourrier.getTypeCourrier().getCode() + ")");

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Courrier et fichier créés avec succès",
            "courrier", savedCourrier,
            "fichier", Map.of(
                "id_fichier", fichier.getId(),
                "nom", nomFichier,
                "url_download", "/api/courriers/fichier/" + fichier.getId() + "/download"
            )
        ));

    } catch (RuntimeException e) {
        // Gestion des erreurs de validation
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", e.getMessage()
        ));
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur lors de l'upload : " + e.getMessage()
        ));
    }
}

    /**
     * 🔐 SÉCURISÉ : Télécharger un fichier
     */
    @GetMapping("/fichier/{idFichier}/download")
    public ResponseEntity<?> downloadFichier(
            @PathVariable Long idFichier,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

            FichierCourrier fichier = fichierCourrierRepository.findById(idFichier)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Fichier introuvable"));

            // Vérifier l'accès au courrier associé
            Long idCourrier = fichier.getCourrier().getIdCourrier();
            if (!courrierService.utilisateurPeutAccederAuCourrier(idCourrier, utilisateur)) {
                System.out.println("🔒 Tentative d'accès non autorisé au fichier " + idFichier + " par " + email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "🔒 Vous n'avez pas accès à ce fichier"
                ));
            }

            Path chemin = Paths.get(fichier.getCheminFichier());
            Resource resource = new UrlResource(chemin.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "❌ Fichier inaccessible"
                ));
            }

            System.out.println("📥 Téléchargement autorisé : " + fichier.getNomFichier() + " par " + email);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fichier.getNomFichier() + "\"")
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur lors du téléchargement"
            ));
        }
    }

    /**
     * Ajouter un fichier à un courrier existant
     */
    @PostMapping("/{id}/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT', 'ROLE_DG')")
    public ResponseEntity<?> uploadFichier(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

            // Vérifier l'accès au courrier
            if (!courrierService.utilisateurPeutAccederAuCourrier(id, utilisateur)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "🔒 Vous ne pouvez pas ajouter de fichier à ce courrier"
                ));
            }

            Courrier courrier = courrierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Courrier introuvable"));

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Fichier vide"
                ));
            }

            String nomFichier = StringUtils.cleanPath(file.getOriginalFilename());
            Path dossier = Paths.get(uploadDir, String.valueOf(id));
            Files.createDirectories(dossier);
            Path chemin = dossier.resolve(nomFichier);
            Files.copy(file.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

            FichierCourrier fichier = new FichierCourrier();
            fichier.setNomFichier(nomFichier);
            fichier.setCheminFichier(chemin.toString());
            fichier.setCourrier(courrier);
            fichierCourrierRepository.save(fichier);

            System.out.println("📎 Fichier ajouté au courrier " + id + " par " + email);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Fichier enregistré avec succès",
                "fichier", Map.of(
                    "id", fichier.getId(),
                    "nom", nomFichier,
                    "url_download", "/api/courriers/fichier/" + fichier.getId() + "/download"
                )
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur lors de l'upload"
            ));
        }
    }

    /**
 * Mettre à jour un courrier (seul le créateur ou l'admin peut le faire)
 */
@PutMapping("/{id}")
public ResponseEntity<?> updateCourrier(
        @PathVariable Long id,
        @RequestBody CourrierUpdateRequest request,  // ← Changé ici
        Authentication authentication) {
    try {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

        Courrier courrierExistant = courrierService.getCourrierById(id);

        // Vérifier que l'utilisateur est le créateur ou admin
        boolean estCreateur = courrierExistant.getCreateur() != null && 
                             courrierExistant.getCreateur().getIdUtilisateur()
                                 .equals(utilisateur.getIdUtilisateur());
        boolean estAdmin = utilisateur.getRole().getCode().equals("ADMIN");

        if (!estCreateur && !estAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "🔒 Seul le créateur ou un admin peut modifier ce courrier"
            ));
        }

        // Logs pour déboguer
        System.out.println("📥 Requête PUT reçue pour ID: " + id);
        System.out.println("📦 Données reçues: " + request);
        System.out.println("📅 Date: " + request.getDateReception());
        System.out.println("📤 Expéditeur ID: " + request.getIdExpediteur());
        System.out.println("📥 Destinataire ID: " + request.getIdDestinataire());
        System.out.println("📊 Type ID: " + request.getIdTypeCourrier());
        System.out.println("📋 Statut ID: " + request.getIdStatut());

        // Mettre à jour le courrier
        Courrier updated = courrierService.updateCourrier(id, request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Courrier mis à jour",
            "courrier", updated
        ));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

    /**
     * Supprimer un courrier (seul le créateur ou l'admin peut le faire)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourrier(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

            Courrier courrier = courrierService.getCourrierById(id);

            // Vérifier que l'utilisateur est le créateur ou admin
            boolean estCreateur = courrier.getCreateur() != null &&
            courrier.getCreateur().getIdUtilisateur().equals(utilisateur.getIdUtilisateur());
            boolean estAdmin = utilisateur.getRole().getCode().equals("ADMIN");
            if (!estCreateur && !estAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "🔒 Seul le créateur ou un admin peut supprimer ce courrier"
            ));
        }

        courrierService.deleteCourrier(id);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Courrier supprimé"
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

/**
 * 🆕 Créer un courrier COMPLET (courrier + fichier + fiche de transmission)
 * Tout est créé en une seule requête avec validation complète
 */
@PostMapping("/creer-complet")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT', 'ROLE_DG')")
public ResponseEntity<?> createCourrierComplet(
        @RequestPart("courrier") String courrierJson,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Authentication authentication,
        HttpServletRequest httpRequest) {
    try {
        String email = authentication.getName();
        Utilisateur createur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "❌ Utilisateur introuvable"));

        // 1️⃣ Convertir JSON en objet
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        CourrierCompletRequest request = objectMapper.readValue(courrierJson, CourrierCompletRequest.class);

        // 🆕 VALIDATION : Vérifier que le type de courrier existe et est actif
        if (request.getIdTypeCourrier() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Le type de courrier est obligatoire"
            ));
        }

        // Valider le type AVANT de créer les autres entités
        courrierService.validateTypeCourrier(request.getIdTypeCourrier());

        // 2️⃣ Créer la fiche de transmission si des données sont fournies
        FicheDeTransmission fiche = null;
        if (request.getReference() != null || request.getObservation() != null) {
            fiche = new FicheDeTransmission();
            fiche.setDateEnvoi(LocalDate.now());
            fiche.setReference(request.getReference());
            fiche.setObservation(request.getObservation());
            fiche.setTresUrgent(request.isTresUrgent());
            fiche.setUrgent(request.isUrgent());
            fiche.setMenParler(request.isMenParler());
            fiche.setPourAttribution(request.isPourAttribution());
            fiche.setPourEtudeEtAvis(request.isPourEtudeEtAvis());
            fiche.setPourDisposition(request.isPourDisposition());
            fiche.setElementDeReponse(request.isElementDeReponse());
            fiche.setFinRetour(request.isFinRetour());
            fiche.setPourSuiteADonner(request.isPourSuiteADonner());
            fiche.setPourVisaPrealable(request.isPourVisaPrealable());
            fiche.setPourNecessaire(request.isPourNecessaire());
            fiche.setNotePourLeDG(request.isNotePourLeDG());
            fiche.setPourResumerSuccinct(request.isPourResumerSuccinct());
            fiche.setNoteMinistre(request.isNoteMinistre());
            fiche.setCopieA(request.isCopieA());
            fiche.setMeRepresenter(request.isMeRepresenter());
            fiche.setPourEtude(request.isPourEtude());
            fiche.setLettreDeTransmission(request.isLettreDeTransmission());
            fiche.setBordeauEnvoi(request.isBordeauEnvoi());
            fiche.setPourInformation(request.isPourInformation());
            fiche.setATouteFinUtile(request.isATouteFinUtile());
            fiche.setEnInstance(request.isEnInstance());
            fiche.setAClasser(request.isAClasser());
            fiche.setCourrierReserve(request.isCourrierReserve());
            
            // Sauvegarder la fiche
            fiche = ficheDeTransmissionRepository.save(fiche);
            System.out.println("📋 Fiche de transmission créée : ID " + fiche.getIdFiche());
        }

        // 3️⃣ Créer le courrier
        Courrier courrier = new Courrier();
        courrier.setObjet(request.getObjet());
        courrier.setDateReception(Date.valueOf(request.getDateReception()));
        
        // Associer les entités liées
        courrier.setExpediteur(expediteurRepository.findById(request.getIdExpediteur())
            .orElseThrow(() -> new RuntimeException("❌ Expéditeur introuvable")));
        courrier.setDestinataire(destinataireRepository.findById(request.getIdDestinataire())
            .orElseThrow(() -> new RuntimeException("❌ Destinataire introuvable")));
        courrier.setTypeCourrier(typeCourrierRepository.findById(request.getIdTypeCourrier())
            .orElseThrow(() -> new RuntimeException("❌ Type courrier introuvable")));
        courrier.setStatut(statutRepository.findById(request.getIdStatut())
            .orElseThrow(() -> new RuntimeException("❌ Statut introuvable")));
        
        // Associer la fiche si elle existe
        if (fiche != null) {
            courrier.setFicheDeTransmission(fiche);
        }

        // Sauvegarder le courrier avec validation du type (fait dans createCourrier)
        Courrier savedCourrier = courrierService.createCourrier(courrier, createur);
        System.out.println("📧 Courrier créé : ID " + savedCourrier.getIdCourrier() + 
                         " (Type: " + savedCourrier.getTypeCourrier().getCode() + ")");

        // 4️⃣ Sauvegarder le fichier si fourni
        FichierCourrier fichierCourrier = null;
        if (file != null && !file.isEmpty()) {
            String nomFichier = StringUtils.cleanPath(file.getOriginalFilename());
            Path dossier = Paths.get(uploadDir, String.valueOf(savedCourrier.getIdCourrier()));
            Files.createDirectories(dossier);

            Path chemin = dossier.resolve(nomFichier);
            Files.copy(file.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);

            fichierCourrier = new FichierCourrier();
            fichierCourrier.setNomFichier(nomFichier);
            fichierCourrier.setCheminFichier(chemin.toString());
            fichierCourrier.setCourrier(savedCourrier);
            fichierCourrierRepository.save(fichierCourrier);

            System.out.println("📎 Fichier enregistré : " + nomFichier);
        }

        // 🆕 JOURNALISATION
        journalisationService.logCreation(
            EntiteConcernee.COURRIER,
            savedCourrier.getIdCourrier(),
            "Création courrier complet : " + savedCourrier.getObjet() + 
            " (Type: " + savedCourrier.getTypeCourrier().getLibelle() + ")",
            createur,
            httpRequest
        );

        // 5️⃣ Construire la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "✅ Courrier complet créé avec succès");
        response.put("courrier", Map.of(
            "id", savedCourrier.getIdCourrier(),
            "objet", savedCourrier.getObjet(),
            "date_reception", savedCourrier.getDateReception(),
            "type_courrier", Map.of(
                "id", savedCourrier.getTypeCourrier().getIdType(),
                "code", savedCourrier.getTypeCourrier().getCode(),
                "libelle", savedCourrier.getTypeCourrier().getLibelle()
            )
        ));
        
        if (fiche != null) {
            response.put("fiche_transmission", Map.of(
                "id", fiche.getIdFiche(),
                "reference", fiche.getReference() != null ? fiche.getReference() : "",
                "observation", fiche.getObservation() != null ? fiche.getObservation() : ""
            ));
        }
        
        if (fichierCourrier != null) {
            response.put("fichier", Map.of(
                "id", fichierCourrier.getId(),
                "nom", fichierCourrier.getNomFichier(),
                "url_download", "/api/courriers/fichier/" + fichierCourrier.getId() + "/download"
            ));
        }

        System.out.println("✅ Courrier complet créé par " + email);

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        // Gestion des erreurs de validation
        e.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", e.getMessage()
        ));
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur lors de l'upload : " + e.getMessage()
        ));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

// Changer le statut d'un courrier
@PutMapping("/{id}/statut")
public ResponseEntity<?> changerStatut(
        @PathVariable Long id,
        @RequestBody Map<String, Long> request,
        Authentication authentication,
        HttpServletRequest httpRequest) {

    try {
        // 1. Récupérer l'utilisateur qui fait l'action
        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 2. Récupérer le courrier et le nouveau statut
        Courrier courrier = courrierService.getCourrierById(id);
        Long idNouveauStatut = request.get("id_statut");
        if (idNouveauStatut == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ id_statut obligatoire"
            ));
        }

        Statut nouveauStatut = statutService.getStatutById(idNouveauStatut);

        // 3. Vérification règle métier
        if (!statutService.peutChangerStatut(courrier.getStatut(), nouveauStatut)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "❌ Changement de statut non autorisé"
            ));
        }

        // 4. Mise à jour
        Statut ancienStatut = courrier.getStatut();
        courrier.setStatut(nouveauStatut);
        courrierRepository.save(courrier);

        // 5. Notification → uniquement si créateur différent de l'utilisateur actuel
        Utilisateur createur = courrier.getCreateur();
        if (createur != null && !createur.getIdUtilisateur().equals(utilisateur.getIdUtilisateur())) {

            String message = String.format(
                    "Le statut du courrier « %s » (ID %d) a été modifié par %s %s : %s → %s",
                    courrier.getObjet(),
                    courrier.getIdCourrier(),
                    utilisateur.getPrenomUtilisateur(),
                    utilisateur.getNomUtilisateur(),
                    ancienStatut.getLibelleStatut(),
                    nouveauStatut.getLibelleStatut()
            );

            try {
                notificationService.creerNotification(
                        createur,
                        message,
                        courrier.getIdCourrier()
                );
            } catch (Exception e) {
                // Ne PAS faire échouer toute la requête si la notification plante
                System.err.println("Échec création notification pour statut courrier " + id + " : " + e.getMessage());
                // Option : logger via SLF4J plutôt que System.err
            }
        }

        // 6. Journalisation (inchangée)
        journalisationService.logModification(
                EntiteConcernee.COURRIER,
                courrier.getIdCourrier(),
                "Changement de statut : " + ancienStatut.getLibelleStatut() + " → " + nouveauStatut.getLibelleStatut(),
                utilisateur,
                httpRequest,
                Map.of("ancien_statut", ancienStatut.getCodeStatut()),
                Map.of("nouveau_statut", nouveauStatut.getCodeStatut())
        );

        // 7. Réponse
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Statut modifié avec succès",
                "courrier", courrier
        ));

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

/**
 * api de recherche
 */
    @GetMapping("/rechercher")
public ResponseEntity<?> rechercher(
        @RequestParam String q,
        Authentication authentication) {
    try {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        List<Courrier> resultats = courrierService.rechercherCourriersAccessibles(q.trim(), utilisateur);

        List<Map<String, Object>> mapped = resultats.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getIdCourrier());
            map.put("title", c.getObjet());
            map.put("type", "courrier");
            map.put("status", c.getStatut().getCodeStatut().toLowerCase());
            map.put("description", "Courrier reçu le " + c.getDateReception());
            map.put("date", c.getDateReception().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "resultats", mapped
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}

/**
 * Récupère les statistiques détaillées des courriers accessibles par l'utilisateur connecté.
 * Les paramètres de requête sont optionnels et permettent de filtrer les courriers pris en compte.
 *
 * @param recherche   texte libre (objet, expéditeur)
 * @param statut      code du statut pour filtrer les courriers
 * @param type        code du type pour filtrer les courriers
 * @param dateDebut   date de début (inclusive)
 * @param dateFin     date de fin (inclusive)
 * @param authentication
 * @return statistiques détaillées
 */
@GetMapping("/mes-statistiques")
public ResponseEntity<?> getMesStatistiques(
        @RequestParam(required = false) String recherche,
        @RequestParam(required = false) String statut,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
        Authentication authentication) {
    
    try {
        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        
        MesStatistiquesDTO stats = courrierService.getMesStatistiques(
            utilisateur, recherche, statut, type, dateDebut, dateFin);
        
        return ResponseEntity.ok(stats);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "Erreur lors du calcul des statistiques : " + e.getMessage()
        ));
    }
}
}