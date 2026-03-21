package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Interface permettant d'accéder aux opérations CRUD sur les types de courrier.
 * Étend JpaRepository pour bénéficier des méthodes standards.
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 */
@Repository
public interface TypeCourrierRepository extends JpaRepository<TypeCourrier, Long> {
    
    /**
     * Recherche un type de courrier par son libellé (insensible à la casse)
     */
    Optional<TypeCourrier> findByLibelleIgnoreCase(String libelle);
    
    /**
     * Recherche un type de courrier par son code
     */
    Optional<TypeCourrier> findByCode(String code);
    
    /**
     * Vérifie si un type avec ce libellé existe déjà (pour un autre id)
     * Utilisé lors de la mise à jour pour éviter les doublons
     */
    boolean existsByLibelleIgnoreCaseAndIdTypeNot(String libelle, Long idType);
    
    /**
     * Vérifie si un type avec ce code existe déjà (pour un autre id)
     */
    boolean existsByCodeAndIdTypeNot(String code, Long idType);
    
    /**
     * Vérifie si un type avec ce libellé existe
     */
    boolean existsByLibelleIgnoreCase(String libelle);
    
    /**
     * Vérifie si un type avec ce code existe
     */
    boolean existsByCode(String code);
    
    /**
     * Récupère tous les types de courrier actifs
     */
    List<TypeCourrier> findByActifTrue();
    
    /**
     * Récupère tous les types de courrier système
     */
    List<TypeCourrier> findByTypeSystemeTrue();
    
    /**
     * Récupère un type avec tous ses courriers (fetch eager)
     * Utile pour éviter le problème N+1 lors du chargement des courriers
     */
    @Query("SELECT t FROM TypeCourrier t LEFT JOIN FETCH t.courriers WHERE t.idType = :id")
    Optional<TypeCourrier> findByIdWithCourriers(@Param("id") Long id);
    
    /**
     * Compte le nombre de courriers par type
     */
    @Query("SELECT t.idType, COUNT(c) FROM TypeCourrier t LEFT JOIN t.courriers c GROUP BY t.idType")
    List<Object[]> countCourriersByType();
    
    /**
     * Récupère les statistiques des types de courrier avec le nombre de courriers
     */
    @Query("SELECT t.idType as idType, t.libelle as libelle, t.code as code, " +
           "COUNT(c) as nombreCourriers " +
           "FROM TypeCourrier t LEFT JOIN t.courriers c " +
           "WHERE t.actif = true " +
           "GROUP BY t.idType, t.libelle, t.code " +
           "ORDER BY nombreCourriers DESC")
    List<TypeCourrierStats> getStatistiques();
    
    /**
     * Interface de projection pour les statistiques
     */
    interface TypeCourrierStats {
        Long getIdType();
        String getLibelle();
        String getCode();
        Long getNombreCourriers();
    }
    
    /**
     * Recherche des types par libellé (recherche partielle)
     */
    @Query("SELECT t FROM TypeCourrier t WHERE LOWER(t.libelle) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TypeCourrier> searchByLibelle(@Param("keyword") String keyword);
}