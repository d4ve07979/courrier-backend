package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tg.inseed.gestioncourrier.gestioncourrier.direction.Direction;
import tg.inseed.gestioncourrier.gestioncourrier.direction.DirectionRepository;
import tg.inseed.gestioncourrier.gestioncourrier.email.EmailService; // ← NOUVEAU IMPORT

/**
 * Service métier pour la gestion des utilisateurs
 */
@Service
@Transactional
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DirectionRepository directionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // ← INJECTION DU SERVICE D'EMAIL

    /**
     * ✅ CRÉER UN UTILISATEUR AVEC ENCODAGE DU MOT DE PASSE + ENVOI EMAIL BIENVENUE
     */
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
    System.out.println("🔧 Création utilisateur : " + utilisateur.getEmailUtilisateur());
    
    // 1️⃣ GÉNÉRER UN MOT DE PASSE SI ABSENT
    String motDePasseClair;
    if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
        motDePasseClair = genererMotDePasseTemporaire();
        System.out.println("🔑 Mot de passe généré automatiquement");
    } else {
        motDePasseClair = utilisateur.getMotDePasse();
    }
    
    // ENCODAGE DU MOT DE PASSE
    String motDePasseEncode = passwordEncoder.encode(motDePasseClair);
    utilisateur.setMotDePasse(motDePasseEncode);
    
    System.out.println("🔐 Mot de passe clair : " + motDePasseClair);
    System.out.println("🔐 Mot de passe encodé : " + motDePasseEncode.substring(0, 20) + "...");
    
    // 2️⃣ GESTION DE LA DIRECTION (obligatoire)
    if (utilisateur.getDirection() == null || utilisateur.getDirection().getIdDirection() == null) {
        throw new RuntimeException("❌ Une direction est obligatoire");
    }
    Direction direction = directionRepository.findById(utilisateur.getDirection().getIdDirection())
        .orElseThrow(() -> new RuntimeException("❌ Direction introuvable"));
    utilisateur.setDirection(direction);
    System.out.println("📁 Direction : " + direction.getNomDirection());
    
    // 3️⃣ PARAMÈTRES PAR DÉFAUT
    utilisateur.setActif(true);
    utilisateur.setVerrouille(false);
    utilisateur.setTentativesEchec(0);
    
    // 4️⃣ SAUVEGARDE
    Utilisateur saved = utilisateurRepository.save(utilisateur);
    
    System.out.println("✅ Utilisateur créé : ID=" + saved.getIdUtilisateur() + 
                     ", Email=" + saved.getEmailUtilisateur() + 
                     ", Rôle=" + saved.getRoleString());
    
    // 5️⃣ ENVOI DE L'EMAIL DE BIENVENUE
    try {
        emailService.envoyerEmailBienvenue(
            saved.getEmailUtilisateur(),
            saved.getPrenomUtilisateur(),
            saved.getEmailUtilisateur(),
            motDePasseClair
        );
    } catch (Exception e) {
        System.err.println("⚠️ Erreur lors de l'envoi de l'email de bienvenue à " + saved.getEmailUtilisateur());
        e.printStackTrace();
    }
    
    return saved;
}

    /**
     * 🔑 Réinitialiser le mot de passe d'un utilisateur (admin) + ENVOI EMAIL
     */
    public String reinitialiserMotDePasse(Long id) {
        Utilisateur utilisateur = getUtilisateurById(id);
        
        String motDePasseTemporaire = genererMotDePasseTemporaire();
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        utilisateur.reinitialiserTentativesEchec();
        
        utilisateurRepository.save(utilisateur);
        
        System.out.println("🔑 Mot de passe réinitialisé pour : " + utilisateur.getEmailUtilisateur());
        
        // ENVOI DE L'EMAIL DE RÉINITIALISATION
        try {
            emailService.envoyerEmailReinitialisation(
                utilisateur.getEmailUtilisateur(),
                utilisateur.getPrenomUtilisateur(),
                motDePasseTemporaire
            );
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'envoi de l'email de réinitialisation à " + utilisateur.getEmailUtilisateur());
            e.printStackTrace();
        }
        
        return motDePasseTemporaire;
    }

    // === LES AUTRES MÉTHODES RESTENT INCHANGÉES ===

    

    public List<UtilisateurAssignationResponse> getUtilisateursPourAffectation() {
        return utilisateurRepository.findAll().stream()
            .map(u -> {
                UtilisateurAssignationResponse dto = new UtilisateurAssignationResponse();
                dto.setIdUtilisateur(u.getIdUtilisateur());
                dto.setNomUtilisateur(u.getNomUtilisateur());
                dto.setPrenomUtilisateur(u.getPrenomUtilisateur());
                dto.setEmailUtilisateur(u.getEmailUtilisateur());
                dto.setRoleUtilisateur(u.getRole().name());
                dto.setDirectionUtilisateur(u.getDirection() != null 
                    ? u.getDirection().getNomDirection() 
                    : "Aucune direction");
                return dto;
            })
            .collect(Collectors.toList());
    }

    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("❌ Utilisateur introuvable avec l'id: " + id));
    }

    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new RuntimeException("❌ Utilisateur non trouvé avec l'email: " + email));
    }

    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurModifie) {
        Utilisateur utilisateurExistant = getUtilisateurById(id);
        
        System.out.println("🔧 Mise à jour utilisateur ID: " + id);
        
        utilisateurExistant.setNomUtilisateur(utilisateurModifie.getNomUtilisateur());
        utilisateurExistant.setPrenomUtilisateur(utilisateurModifie.getPrenomUtilisateur());
        utilisateurExistant.setEmailUtilisateur(utilisateurModifie.getEmailUtilisateur());
        utilisateurExistant.setRole(utilisateurModifie.getRole());
        utilisateurExistant.setTelephone(utilisateurModifie.getTelephone());
        utilisateurExistant.setSexe(utilisateurModifie.getSexe());
        
        if (utilisateurModifie.getDirection() != null && 
            utilisateurModifie.getDirection().getIdDirection() != null) {
            Direction direction = directionRepository.findById(utilisateurModifie.getDirection().getIdDirection())
                .orElseThrow(() -> new RuntimeException(
                    "❌ Direction avec l'ID " + utilisateurModifie.getDirection().getIdDirection() + " n'existe pas"
                ));
            utilisateurExistant.setDirection(direction);
        }
        
        if (utilisateurModifie.getMotDePasse() != null && 
            !utilisateurModifie.getMotDePasse().isEmpty() &&
            !utilisateurModifie.getMotDePasse().startsWith("$2a$")) {
            
            String motDePasseEncode = passwordEncoder.encode(utilisateurModifie.getMotDePasse());
            utilisateurExistant.setMotDePasse(motDePasseEncode);
            System.out.println("🔑 Mot de passe mis à jour");
        }
        
        return utilisateurRepository.save(utilisateurExistant);
    }

    public void deleteUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new RuntimeException("❌ L'utilisateur avec l'id " + id + " n'existe pas");
        }
        utilisateurRepository.deleteById(id);
        System.out.println("🗑️ Utilisateur supprimé : ID " + id);
    }

    public void deverrouillerCompte(Long id) {
        Utilisateur utilisateur = getUtilisateurById(id);
        utilisateur.reinitialiserTentativesEchec();
        utilisateurRepository.save(utilisateur);
        
        System.out.println("🔓 Compte déverrouillé : " + utilisateur.getEmailUtilisateur());
    }

    private String genererMotDePasseTemporaire() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$%";
        StringBuilder motDePasse = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            motDePasse.append(chars.charAt(random.nextInt(chars.length())));
        }
        return motDePasse.toString();
    }

    public List<Utilisateur> getUtilisateursByRole(RoleUtilisateur role) {
        return utilisateurRepository.findByRole(role);
    }

    public List<Utilisateur> getDirecteursGeneraux() {
        return utilisateurRepository.findByRole(RoleUtilisateur.DG);
    }

    public List<UtilisateurAssignationResponse> getUtilisateursByRoleDTO(RoleUtilisateur role) {
        return utilisateurRepository.findByRole(role).stream()
            .map(u -> {
                UtilisateurAssignationResponse dto = new UtilisateurAssignationResponse();
                dto.setIdUtilisateur(u.getIdUtilisateur());
                dto.setNomUtilisateur(u.getNomUtilisateur());
                dto.setPrenomUtilisateur(u.getPrenomUtilisateur());
                dto.setEmailUtilisateur(u.getEmailUtilisateur());
                dto.setRoleUtilisateur(u.getRole().name());
                dto.setDirectionUtilisateur(u.getDirection() != null 
                    ? u.getDirection().getNomDirection() 
                    : "Aucune direction");
                return dto;
            })
            .collect(Collectors.toList());
    }

    public long countUtilisateursByRole(RoleUtilisateur role) {
        return utilisateurRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
public List<Utilisateur> getAllUtilisateurs() {
    return utilisateurRepository.findAll();
}
}