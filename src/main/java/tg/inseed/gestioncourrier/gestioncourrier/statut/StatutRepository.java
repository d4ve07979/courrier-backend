package tg.inseed.gestioncourrier.gestioncourrier.statut;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour la gestion des statuts.
 *
 * <p>Cette interface fournit des méthodes pour interroger et manipuler
 * les données liées aux statuts des courriers. Elle hérite de
 * {@link JpaRepository}, offrant ainsi toutes les opérations CRUD de base,
 * et définit des requêtes personnalisées pour des besoins spécifiques.</p>
 *
 * <p>Les statuts permettent de suivre le cycle de vie des courriers
 * (exemple : "En attente", "Validé", "Archivé").</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Repository
public interface StatutRepository extends JpaRepository<Statut, Long> {
    
    /**
     * Recherche un statut par son code unique.
     *
     * @param codeStatut code du statut (exemple : "EN_ATTENTE")
     * @return un {@link Optional} contenant le statut si trouvé
     */
    Optional<Statut> findByCodeStatut(String codeStatut);
    
    /**
     * Recherche un statut par son libellé.
     *
     * @param libelleStatut libellé du statut (exemple : "En attente")
     * @return un {@link Optional} contenant le statut si trouvé
     */
    Optional<Statut> findByLibelleStatut(String libelleStatut);
    
    /**
     * Récupère tous les statuts actifs, triés par ordre d’affichage.
     *
     * @return liste des statuts actifs triés par ordre croissant
     */
    List<Statut> findByActifTrueOrderByOrdreAsc();
    
    /**
     * Vérifie si un statut existe en fonction de son code.
     *
     * @param codeStatut code du statut
     * @return true si le statut existe, false sinon
     */
    boolean existsByCodeStatut(String codeStatut);
    
    /**
     * Compte le nombre de courriers associés à chaque statut.
     *
     * <p>Retourne une liste d’objets contenant le libellé du statut
     * et le nombre de courriers liés.</p>
     *
     * @return liste de tableaux d’objets [libelleStatut, nombreCourriers]
     */
    @Query("SELECT s.libelleStatut, COUNT(c) FROM Statut s LEFT JOIN s.courriers c GROUP BY s.idStatut, s.libelleStatut")
    List<Object[]> countCourriersParStatut();
}
