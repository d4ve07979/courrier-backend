package tg.inseed.gestioncourrier.gestioncourrier.decharge;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;

/**
 * Interface permettant d'accéder aux opérations CRUD sur les décharges.
 * Étend JpaRepository pour bénéficier des méthodes standards.
 * 
 * @author KENKOU Dave
 */
@Repository
public interface DechargeRepository extends JpaRepository<Decharge, Long> {
    
    /**
     * Récupère toutes les décharges associées à un courrier spécifique.
     *
     * @param courrier Le courrier dont on veut les décharges
     * @return Liste des décharges de ce courrier
     */
    List<Decharge> findByCourrier(Courrier courrier);
    
    /**
     * Vérifie si un courrier possède au moins une décharge.
     *
     * @param courrier Le courrier à vérifier
     * @return true si une décharge existe pour ce courrier, false sinon
     */
    boolean existsByCourrier(Courrier courrier);
}