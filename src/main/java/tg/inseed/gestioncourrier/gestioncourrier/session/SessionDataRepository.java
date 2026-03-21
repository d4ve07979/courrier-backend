package tg.inseed.gestioncourrier.gestioncourrier.session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

@Repository
public interface SessionDataRepository extends JpaRepository<Session, Long> {
    
    /**
     * 🆕 Récupérer toutes les sessions d'un utilisateur
     */
    List<Session> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * 🆕 Récupérer les sessions actives d'un utilisateur
     */
    List<Session> findByUtilisateurAndActiveTrue(Utilisateur utilisateur);
    
    /**
     * 🆕 Récupérer toutes les sessions actives
     */
    List<Session> findByActiveTrue();
    
    /**
     * 🆕 Récupérer une session par token JWT
     */
    Optional<Session> findByTokenJwt(String tokenJwt);
    
    /**
     * 🆕 Récupérer les sessions dans une période
     */
    @Query("SELECT s FROM Session s WHERE s.dateConnexion BETWEEN :debut AND :fin")
    List<Session> findSessionsBetween(
        @Param("debut") LocalDateTime debut, 
        @Param("fin") LocalDateTime fin
    );
    
    /**
     * 🆕 Compter les sessions actives
     */
    long countByActiveTrue();
    
    /**
     * 🆕 Compter les sessions d'un utilisateur
     */
    long countByUtilisateur(Utilisateur utilisateur);
}