package tg.inseed.gestioncourrier.gestioncourrier.fichiers;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;

/**
 * Repository Spring Data JPA pour gérer les fichiers liés aux courriers.
 * Permet les opérations CRUD sur l'entité FichierCourrier.
 *
 * Exemple d’usage :
 * - fichierCourrierRepository.save(fichier)
 * - fichierCourrierRepository.findById(id)
 */
@Repository
public interface FichierCourrierRepository extends JpaRepository<FichierCourrier, Long> {

    /**
     * 🆕 Récupérer tous les fichiers d'un courrier
     */
    List<FichierCourrier> findByCourrier(Courrier courrier);
}