package tg.inseed.gestioncourrier.gestioncourrier.courriers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Interface permettant d'accéder aux opérations CRUD sur les courriers.
 * Étend JpaRepository pour bénéficier des méthodes standards.
 * 
 * @author KENKOU
 */
@Repository
public interface CourrierRepository extends JpaRepository<Courrier, Long> {
    // Méthodes personnalisées possibles : findByStatut, findByExpediteur, etc.

    
     List<Courrier> findByIdCourrierIn(List<Long> ids);
    
    /**
     * 🆕 Récupérer les courriers créés par un utilisateur
     */
    List<Courrier> findByCreateur(Utilisateur createur);
    
    /**
     * 🆕 Récupérer TOUS les courriers accessibles par un utilisateur :
     * - Courriers qu'il a créés
     * - Courriers qui lui sont affectés directement
     * - Courriers affectés à sa direction
     */
    @Query("SELECT DISTINCT c FROM Courrier c " +
           "LEFT JOIN c.affectation a " +
           "WHERE c.createur.idUtilisateur = :idUtilisateur " +
           "OR a.utilisateur.idUtilisateur = :idUtilisateur " +
           "OR (a.direction.idDirection = :idDirection AND :idDirection IS NOT NULL)")
    List<Courrier> findCourriersAccessiblesParUtilisateur(
        @Param("idUtilisateur") Long idUtilisateur,
        @Param("idDirection") Long idDirection
    );
    
    /**
     * 🆕 Vérifier si un utilisateur peut accéder à un courrier spécifique
     */
    @Query("SELECT COUNT(c) > 0 FROM Courrier c " +
           "LEFT JOIN c.affectation a " +
           "WHERE c.idCourrier = :idCourrier " +
           "AND (c.createur.idUtilisateur = :idUtilisateur " +
           "OR a.utilisateur.idUtilisateur = :idUtilisateur " +
           "OR (a.direction.idDirection = :idDirection AND :idDirection IS NOT NULL))")
    boolean utilisateurPeutAcceder(
        @Param("idCourrier") Long idCourrier,
        @Param("idUtilisateur") Long idUtilisateur,
        @Param("idDirection") Long idDirection
    );

    @EntityGraph(attributePaths = {"affectation", "affectation.direction"})
@Query("SELECT c FROM Courrier c")
List<Courrier> findAllWithAffectationsAndDirections();

// Dans CourrierRepository.java
@Query("SELECT c FROM Courrier c " +
       "LEFT JOIN FETCH c.expediteur " +
       "LEFT JOIN FETCH c.destinataire " +
       "LEFT JOIN FETCH c.typeCourrier " +
       "LEFT JOIN FETCH c.statut " +
       "LEFT JOIN FETCH c.fichiers " +
       "WHERE c.idCourrier = :id")
Optional<Courrier> findByIdWithAllRelations(@Param("id") Long id);

// ============================================================
// Statistiques SANS recherche (pour éviter l'erreur bytea)
// ============================================================

@Query("SELECT c.statut.codeStatut, COUNT(c) FROM Courrier c " +
       "LEFT JOIN c.affectation a " +
       "WHERE (c.createur.idUtilisateur = :userId " +
       "OR a.utilisateur.idUtilisateur = :userId " +
       "OR (a.direction.idDirection = :directionId AND :directionId IS NOT NULL)) " +
       "AND (:statut IS NULL OR c.statut.codeStatut = :statut) " +
       "AND (:type IS NULL OR c.typeCourrier.code = :type) " +
       "AND (cast(:dateDebut as date) IS NULL OR c.dateReception >= :dateDebut) " +
       "AND (cast(:dateFin as date) IS NULL OR c.dateReception <= :dateFin) " +
       "GROUP BY c.statut.codeStatut")
List<Object[]> countByStatutAccessibleWithoutRecherche(
        @Param("userId") Long userId,
        @Param("directionId") Long directionId,
        @Param("statut") String statut,
        @Param("type") String type,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin);

@Query("SELECT c.typeCourrier.code, COUNT(c) FROM Courrier c " +
       "LEFT JOIN c.affectation a " +
       "WHERE (c.createur.idUtilisateur = :userId " +
       "OR a.utilisateur.idUtilisateur = :userId " +
       "OR (a.direction.idDirection = :directionId AND :directionId IS NOT NULL)) " +
       "AND (:statut IS NULL OR c.statut.codeStatut = :statut) " +
       "AND (:type IS NULL OR c.typeCourrier.code = :type) " +
       "AND (cast(:dateDebut as date) IS NULL OR c.dateReception >= :dateDebut) " +
       "AND (cast(:dateFin as date) IS NULL OR c.dateReception <= :dateFin) " +
       "GROUP BY c.typeCourrier.code")
List<Object[]> countByTypeAccessibleWithoutRecherche(
        @Param("userId") Long userId,
        @Param("directionId") Long directionId,
        @Param("statut") String statut,
        @Param("type") String type,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin);

@Query("SELECT COUNT(c) FROM Courrier c " +
       "LEFT JOIN c.affectation a " +
       "WHERE (c.createur.idUtilisateur = :userId " +
       "OR a.utilisateur.idUtilisateur = :userId " +
       "OR (a.direction.idDirection = :directionId AND :directionId IS NOT NULL)) " +
       "AND (:statut IS NULL OR c.statut.codeStatut = :statut) " +
       "AND (:type IS NULL OR c.typeCourrier.code = :type) " +
       "AND (cast(:dateDebut as date) IS NULL OR c.dateReception >= :dateDebut) " +
       "AND (cast(:dateFin as date) IS NULL OR c.dateReception <= :dateFin)")
long countAccessibleWithFiltersWithoutRecherche(
        @Param("userId") Long userId,
        @Param("directionId") Long directionId,
        @Param("statut") String statut,
        @Param("type") String type,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin);

    

}
