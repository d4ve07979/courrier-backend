package tg.inseed.gestioncourrier.gestioncourrier.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Column(nullable = false)
    private boolean lue = false;

    @ManyToOne
    @JoinColumn(name = "id_destinataire", nullable = false)
    private Utilisateur destinataire;

    // Optionnel : lien vers le courrier concerné
    private Long idCourrier;
}