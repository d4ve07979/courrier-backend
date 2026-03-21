package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {
    
    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping("/ajouter")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public ResponseEntity<?> createUtilisateur(@RequestBody Utilisateur utilisateur) {
    try {
        // ✅ Ajoutez ces logs pour déboguer
        System.out.println("📥 Utilisateur reçu: " + utilisateur);
        System.out.println("📥 Direction reçue: " + utilisateur.getDirection());
        
        if (utilisateur.getDirection() == null || utilisateur.getDirection().getIdDirection() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Une direction est obligatoire pour les employés INSEED"
            ));
        }

        Utilisateur created = utilisateurService.createUtilisateur(utilisateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "✅ Utilisateur créé avec succès",
            "utilisateur", created
        ));
    } catch (Exception e) {
        e.printStackTrace(); // ✅ Affichez la stack trace complète
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}
    /**
     * 🆕 ADMIN : Réinitialiser le mot de passe d'un utilisateur
     */
    @PutMapping("/{id}/reinitialiser-mot-de-passe")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> reinitialiserMotDePasse(@PathVariable Long id) {
        try {
            String motDePasseTemporaire = utilisateurService.reinitialiserMotDePasse(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Mot de passe réinitialisé",
                "mot_de_passe_temporaire", motDePasseTemporaire,
                "note", "L'utilisateur devra changer ce mot de passe à sa première connexion"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🆕 ADMIN : Déverrouiller un compte
     */
    @PutMapping("/{id}/deverrouiller")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deverrouillerCompte(@PathVariable Long id) {
        try {
            utilisateurService.deverrouillerCompte(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Compte déverrouillé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 👤 Récupérer son propre profil
     */
    @GetMapping("/profil")
    public ResponseEntity<?> getMonProfil(Authentication authentication) {
        try {
            Utilisateur utilisateur = utilisateurService.getByEmail(authentication.getName());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "utilisateur", utilisateur
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "❌ Utilisateur non trouvé"
            ));
        }
    }

    @GetMapping("/list")
     @PreAuthorize("isAuthenticated()")
    public List<UtilisateurAssignationResponse> getUtilisateursPourAffectation() {
        return utilisateurService.getUtilisateursPourAffectation();
    }
    /**
 * 🆕 Récupérer les utilisateurs ayant le rôle DG
 */
@GetMapping("/dg")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIAT')")
public ResponseEntity<?> getDirecteursGeneraux() {
    try {
        List<UtilisateurAssignationResponse> dgs = 
            utilisateurService.getUtilisateursByRoleDTO(RoleUtilisateur.DG);
        
        System.out.println("📋 " + dgs.size() + " Directeur(s) Général(aux) trouvé(s)");
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", dgs.size(),
            "utilisateurs", dgs
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

/**
 * 🆕 Récupérer les utilisateurs par rôle (générique)
 */
@GetMapping("/role/{roleCode}")
 @PreAuthorize("isAuthenticated()")
public ResponseEntity<?> getUtilisateursByRole(@PathVariable String roleCode) {
    try {
        // Convertir le code en RoleUtilisateur
        RoleUtilisateur role;
        try {
            role = RoleUtilisateur.fromString(roleCode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Rôle invalide : " + roleCode,
                "roles_valides", Arrays.stream(RoleUtilisateur.values())
                    .map(RoleUtilisateur::getCode)
                    .collect(Collectors.toList())
            ));
        }
        
        List<UtilisateurAssignationResponse> utilisateurs = 
            utilisateurService.getUtilisateursByRoleDTO(role);
        
        System.out.println("📋 " + utilisateurs.size() + " utilisateur(s) avec le rôle " + roleCode);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "role", roleCode,
            "count", utilisateurs.size(),
            "utilisateurs", utilisateurs
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

/**
 * 🆕 Statistiques des utilisateurs par rôle
 */
@GetMapping("/statistiques/roles")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public ResponseEntity<?> getStatistiquesParRole() {
    try {
        Map<String, Object> stats = new HashMap<>();
        
        for (RoleUtilisateur role : RoleUtilisateur.values()) {
            long count = utilisateurService.countUtilisateursByRole(role);
            stats.put(role.getCode(), Map.of(
                "count", count,
                "authority", role.getAuthority()
            ));
        }
        
        long total = utilisateurService.getAllUtilisateurs().size();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "total", total,
            "par_role", stats
        ));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", "❌ Erreur : " + e.getMessage()
        ));
    }
}

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Utilisateur getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Utilisateur updateUtilisateur(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        return utilisateurService.updateUtilisateur(id, utilisateur);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
    }
    @GetMapping("/all")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public List<Utilisateur> getAllUtilisateurs() {
    return utilisateurService.getAllUtilisateurs();
}

// src/main/java/tg/inseed/gestioncourrier/gestioncourrier/utilisateurs/UtilisateurController.java
    // ✅ NOUVELLE MÉTHODE - Endpoint public pour tous les utilisateurs authentifiés
    @GetMapping("/public/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUtilisateursPublics() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            
            // Transformer en Map pour ne garder que les champs nécessaires
            List<Map<String, Object>> result = utilisateurs.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("idUtilisateur", u.getIdUtilisateur());
                    map.put("nomUtilisateur", u.getNomUtilisateur());
                    map.put("prenomUtilisateur", u.getPrenomUtilisateur());
                    map.put("emailUtilisateur", u.getEmailUtilisateur());
                    map.put("roleUtilisateur", u.getRole() != null ? u.getRole().getCode() : null);
                    return map;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des utilisateurs"));
        }
    }

    // ✅ NOUVELLE MÉTHODE - Endpoint public pour les utilisateurs par rôle
    @GetMapping("/public/role/{roleCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUtilisateursPublicsByRole(@PathVariable String roleCode) {
        try {
            RoleUtilisateur role = RoleUtilisateur.fromString(roleCode);
            List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursByRole(role);
            
            List<Map<String, Object>> result = utilisateurs.stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("idUtilisateur", u.getIdUtilisateur());
                    map.put("nomUtilisateur", u.getNomUtilisateur());
                    map.put("prenomUtilisateur", u.getPrenomUtilisateur());
                    map.put("emailUtilisateur", u.getEmailUtilisateur());
                    map.put("roleUtilisateur", u.getRole() != null ? u.getRole().getCode() : null);
                    return map;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Rôle invalide: " + roleCode));
        }
    }


}