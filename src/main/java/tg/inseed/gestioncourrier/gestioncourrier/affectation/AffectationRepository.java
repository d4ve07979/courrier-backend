package tg.inseed.gestioncourrier.gestioncourrier.affectation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Repository JPA pour accéder aux affectations.
 * Permet les opérations CRUD standard.
 */
@Repository // Déclare ce composant comme un bean Spring
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    // Méthodes personnalisées possibles : findByUtilisateur, findByCourrier, etc.

    List<Affectation> findByUtilisateur(Utilisateur utilisateur);
}