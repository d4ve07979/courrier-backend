package tg.inseed.gestioncourrier.gestioncourrier.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireOrderByDateEnvoiDesc(Utilisateur destinataire);

    @Query("SELECT n FROM Notification n WHERE n.destinataire = :destinataire AND n.lue = false")
    List<Notification> findNonLuesByDestinataire(@Param("destinataire") Utilisateur destinataire);
}