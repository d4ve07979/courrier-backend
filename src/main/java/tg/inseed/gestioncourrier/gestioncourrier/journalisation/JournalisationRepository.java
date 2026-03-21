package tg.inseed.gestioncourrier.gestioncourrier.journalisation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.EntiteConcernee;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.TypeAction;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Repository JPA pour la gestion des entrées de journalisation.
 * 
 * <p>Cette interface fournit des méthodes pour interroger et manipuler
 * les données de journalisation, permettant ainsi de tracer les actions
 * effectuées par les utilisateurs dans le système.</p>
 *
 * <p>Elle hérite de {@link JpaRepository}, offrant ainsi toutes les
 * opérations CRUD de base, et définit des requêtes personnalisées
 * pour des besoins spécifiques (filtrage, comptage, recherche par période, etc.).</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Repository
public interface JournalisationRepository extends JpaRepository<Journalisation, Long> {
    
    /**
     * Récupère toutes les entrées de journalisation associées à un utilisateur donné.
     *
     * @param utilisateur l’utilisateur concerné
     * @return liste des entrées de journalisation
     */
    List<Journalisation> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Récupère les entrées de journalisation d’un utilisateur avec pagination.
     *
     * @param utilisateur l’utilisateur concerné
     * @param pageable objet définissant la pagination et le tri
     * @return page des entrées de journalisation
     */
    Page<Journalisation> findByUtilisateur(Utilisateur utilisateur, Pageable pageable);
    
    /**
     * Récupère toutes les entrées de journalisation par type d’action.
     *
     * @param typeAction type d’action (CREATE, UPDATE, DELETE, etc.)
     * @return liste des entrées correspondantes
     */
    List<Journalisation> findByTypeAction(TypeAction typeAction);
    
    /**
     * Récupère toutes les entrées de journalisation par entité concernée.
     *
     * @param entite entité concernée (UTILISATEUR, COURRIER, etc.)
     * @return liste des entrées correspondantes
     */
    List<Journalisation> findByEntiteConcernee(EntiteConcernee entite);
    
    /**
     * Récupère toutes les entrées de journalisation pour une entité spécifique.
     *
     * @param entite entité concernée
     * @param idEntite identifiant de l’entité
     * @return liste des entrées correspondantes
     */
    List<Journalisation> findByEntiteConcerneeAndIdEntite(EntiteConcernee entite, Long idEntite);
    
    /**
     * Récupère toutes les entrées de journalisation dans une période donnée.
     *
     * @param debut date et heure de début
     * @param fin date et heure de fin
     * @return liste des entrées correspondantes, triées par date décroissante
     */
    @Query("SELECT j FROM Journalisation j WHERE j.dateAction BETWEEN :debut AND :fin ORDER BY j.dateAction DESC")
    List<Journalisation> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    
    /**
     * Récupère les entrées de journalisation avec filtres multiples.
     * 
     * <p>Les filtres peuvent inclure l’utilisateur, le type d’action,
     * l’entité concernée et une période. Les paramètres peuvent être nuls
     * pour ignorer certains filtres.</p>
     *
     * @param utilisateur utilisateur concerné (optionnel)
     * @param typeAction type d’action (optionnel)
     * @param entite entité concernée (optionnel)
     * @param debut date de début (optionnel)
     * @param fin date de fin (optionnel)
     * @param pageable objet définissant la pagination et le tri
     * @return page des entrées correspondantes
     */
    @Query("SELECT j FROM Journalisation j WHERE " +
           "(:utilisateur IS NULL OR j.utilisateur = :utilisateur) AND " +
           "(:typeAction IS NULL OR j.typeAction = :typeAction) AND " +
           "(:entite IS NULL OR j.entiteConcernee = :entite) AND " +
           "(:debut IS NULL OR j.dateAction >= :debut) AND " +
           "(:fin IS NULL OR j.dateAction <= :fin) " +
           "ORDER BY j.dateAction DESC")
    Page<Journalisation> findWithFilters(
        @Param("utilisateur") Utilisateur utilisateur,
        @Param("typeAction") TypeAction typeAction,
        @Param("entite") EntiteConcernee entite,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin,
        Pageable pageable
    );
    
    /**
     * Compte le nombre d’actions par type.
     *
     * @param typeAction type d’action
     * @return nombre d’entrées correspondantes
     */
    long countByTypeAction(TypeAction typeAction);
    
    /**
     * Compte le nombre d’actions effectuées par un utilisateur.
     *
     * @param utilisateur utilisateur concerné
     * @return nombre d’entrées correspondantes
     */
    long countByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Récupère les 10 dernières entrées de journalisation.
     *
     * @return liste des 10 entrées les plus récentes
     */
    List<Journalisation> findTop10ByOrderByDateActionDesc();
}
