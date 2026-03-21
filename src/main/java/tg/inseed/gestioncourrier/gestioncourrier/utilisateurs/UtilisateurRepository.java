package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface permettant d'accéder aux opérations CRUD sur les utilisateurs
 * 
 * @author KENKOU
 */

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    /* 
     * Recherche d'un utilisateur par son email
     * @param emailUtilisateur Email à rechercher
     * @return Utilisateur correspondant
     
    Utilisateur findByEmailUtilisateur(String emailUtilisateur);

    /**
     * Vérifie l'existence d'un utilisateur par email
     * @param emailUtilisateur Email à vérifier
     * @return true si l'utilisateur existe, false sinon
     
    boolean existsByEmailUtilisateur(String emailUtilisateur);
     */


    
    // 🆕 NOUVEAU : Rechercher les utilisateurs d'une direction
    List<Utilisateur> findByDirection_IdDirection(Long idDirection);
    
    // 🆕 NOUVEAU : Compter les utilisateurs d'une direction
    long countByDirection_IdDirection(Long idDirection);
     
      /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param emailUtilisateur adresse email de l'utilisateur
     * @return utilisateur correspondant, s'il existe
     */
    Optional<Utilisateur> findByEmailUtilisateur(String emailUtilisateur);

     /**
     * 🆕 Récupérer les utilisateurs par rôle
     */
    List<Utilisateur> findByRole(RoleUtilisateur role);
    
    /**
     * 🆕 Compter les utilisateurs par rôle
     */
    long countByRole(RoleUtilisateur role);
}
