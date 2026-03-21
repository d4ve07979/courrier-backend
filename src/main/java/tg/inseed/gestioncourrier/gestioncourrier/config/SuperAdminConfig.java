package tg.inseed.gestioncourrier.gestioncourrier.config;
    

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.RoleUtilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;




/**
 * Initialisation du compte Super Admin par défaut
 * À EXÉCUTER UNE SEULE FOIS au démarrage de l'application
 */
@Component
public class SuperAdminConfig implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Créer le Super Admin si aucun admin n'existe
        if (utilisateurRepository.count() == 0) {
            Utilisateur superAdmin = new Utilisateur();
            superAdmin.setNomUtilisateur("ADMIN");
            superAdmin.setPrenomUtilisateur("INSEED");
            superAdmin.setEmailUtilisateur("admin@inseed.tg");
            superAdmin.setMotDePasse(passwordEncoder.encode("Admin@INSEED2024")); // ⚠️ À CHANGER
            superAdmin.setRole(RoleUtilisateur.ADMIN);
            superAdmin.setActif(true);
            superAdmin.setTelephone("+228 22 21 45 95");
            
            utilisateurRepository.save(superAdmin);
            
            System.out.println("✅ Super Admin créé avec succès !");
            System.out.println("📧 Email    : admin@inseed.tg");
            System.out.println("🔑 Password : Admin@INSEED2024");
            System.out.println("⚠️  VEUILLEZ CHANGER CE MOT DE PASSE IMMÉDIATEMENT !");
        }
    }
}


