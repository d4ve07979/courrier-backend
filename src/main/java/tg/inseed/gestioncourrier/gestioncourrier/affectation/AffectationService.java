package tg.inseed.gestioncourrier.gestioncourrier.affectation;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.CourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.direction.Direction;
import tg.inseed.gestioncourrier.gestioncourrier.direction.DirectionRepository;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.EntiteConcernee;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.JournalisationService;
import tg.inseed.gestioncourrier.gestioncourrier.notification.NotificationService;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

@Service
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final CourrierRepository courrierRepository;
    private final DirectionRepository directionRepository;
    private final NotificationService notificationService;
    private final JournalisationService journalisationService;

    @Autowired
    public AffectationService(
            AffectationRepository affectationRepository,
            CourrierRepository courrierRepository,
            DirectionRepository directionRepository,
            NotificationService notificationService,
            JournalisationService journalisationService) {
        this.affectationRepository = affectationRepository;
        this.courrierRepository = courrierRepository;
        this.directionRepository = directionRepository;
        this.notificationService = notificationService;
        this.journalisationService = journalisationService;
    }

    public Affectation createFromDto(AffectationRequest request, Utilisateur utilisateurAffecte, Utilisateur affecteur, HttpServletRequest httpRequest) {

        Courrier courrier = courrierRepository.findById(request.getIdCourrier())
                .orElseThrow(() -> new RuntimeException("Courrier introuvable avec l'id : " + request.getIdCourrier()));

        Direction direction = null;
        if (request.getIdDirection() != null) {
            direction = directionRepository.findById(request.getIdDirection())
                    .orElseThrow(() -> new RuntimeException("Direction introuvable avec l'id : " + request.getIdDirection()));
        }

        Affectation affectation = new Affectation();
        affectation.setCourrier(courrier);
        affectation.setUtilisateur(utilisateurAffecte);
        affectation.setDirection(direction);
        affectation.setDateAffectation(LocalDate.now());
        affectation.setMotif(request.getMotif());

        Affectation savedAffectation = affectationRepository.save(affectation);

        // =============================================
        // NOTIFICATION ENRICHI ET PROFESSIONNELLE
        // =============================================
        StringBuilder messageNotif = new StringBuilder();
        messageNotif.append("🔔 Nouveau courrier affecté\n\n");
        messageNotif.append("📧 Objet : ").append(courrier.getObjet()).append("\n");
        messageNotif.append("📅 Date de réception : ").append(courrier.getDateReception()).append("\n\n");

        messageNotif.append("👤 Affecté par :\n");
        messageNotif.append("   ").append(affecteur.getPrenomUtilisateur())
                    .append(" ").append(affecteur.getNomUtilisateur());

        if (affecteur.getDirection() != null) {
            messageNotif.append(" (").append(affecteur.getDirection().getNomDirection()).append(")");
        }
        messageNotif.append("\n");

        // Contact de l'affecteur
        messageNotif.append("📞 Contact :\n");
        messageNotif.append("   ✉️ ").append(affecteur.getEmailUtilisateur()).append("\n");
        if (affecteur.getTelephone() != null) {
            messageNotif.append("   📱 ").append(affecteur.getTelephone()).append("\n");
        }
        if (affecteur.getBureau() != null) {
            messageNotif.append("   📍 ").append(affecteur.getBureau()).append("\n");
        }

        // Direction affectée (si différente)
        if (direction != null && (affecteur.getDirection() == null || !direction.getIdDirection().equals(affecteur.getDirection().getIdDirection()))) {
            messageNotif.append("\n🏢 Direction concernée :\n");
            messageNotif.append("   ").append(direction.getNomDirection()).append("\n");
            if (direction.getResponsable() != null) {
                messageNotif.append("   Responsable : ").append(direction.getResponsable()).append("\n");
            }
            if (direction.getContactTelephone() != null) {
                messageNotif.append("   📞 ").append(direction.getContactTelephone()).append("\n");
            }
            if (direction.getEmailDirection() != null) {
                messageNotif.append("   ✉️ ").append(direction.getEmailDirection()).append("\n");
            }
        }

        // Motif
        if (request.getMotif() != null && !request.getMotif().trim().isEmpty()) {
            messageNotif.append("\n📝 Motif :\n   ").append(request.getMotif().trim()).append("\n");
        } else {
            messageNotif.append("\n📝 Motif : Aucun motif précisé\n");
        }

        messageNotif.append("\n👆 Cliquez ici pour consulter le courrier");

        notificationService.creerNotification(
                utilisateurAffecte,
                messageNotif.toString(),
                courrier.getIdCourrier()
        );

        // =============================================
        // JOURNALISATION
        // =============================================
        String descriptionLog = String.format(
                "Affectation du courrier n°%d (« %s ») à %s %s%s par %s %s%s",
                courrier.getIdCourrier(),
                courrier.getObjet(),
                utilisateurAffecte.getPrenomUtilisateur(),
                utilisateurAffecte.getNomUtilisateur(),
                direction != null ? " (Direction : " + direction.getNomDirection() + ")" : "",
                affecteur.getPrenomUtilisateur(),
                affecteur.getNomUtilisateur(),
                affecteur.getDirection() != null ? " (" + affecteur.getDirection().getNomDirection() + ")" : ""
        );

        journalisationService.logCreation(
                EntiteConcernee.AFFECTATION,
                savedAffectation.getId(),
                descriptionLog,
                affecteur,
                httpRequest
        );

        return savedAffectation;
    }

    // ==================================================================
    // MÉTHODES UTILITAIRES ET AUTRES (inchangées)
    // ==================================================================
    public Courrier getCourrierById(Long id) {
        return courrierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Courrier introuvable avec l'id: " + id));
    }

    public Direction getDirectionById(Long id) {
        return directionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Direction introuvable avec l'id: " + id));
    }

    public Affectation createAffectation(Affectation affectation) {
        return affectationRepository.save(affectation);
    }

    public List<Affectation> getAllAffectations() {
        return affectationRepository.findAll();
    }

    public Affectation getAffectationById(Long id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable avec l'id: " + id));
    }

    public List<Affectation> getMesAffectations(Utilisateur utilisateur) {
        return affectationRepository.findByUtilisateur(utilisateur);
    }

    public Affectation updateAffectation(Long id, Affectation newAffectation) {
        Affectation affectation = getAffectationById(id);
        affectation.setCourrier(newAffectation.getCourrier());
        affectation.setUtilisateur(newAffectation.getUtilisateur());
        affectation.setDirection(newAffectation.getDirection());
        affectation.setDateAffectation(newAffectation.getDateAffectation());
        affectation.setMotif(newAffectation.getMotif());
        return affectationRepository.save(affectation);
    }

    public void deleteAffectation(Long id) {
        if (!affectationRepository.existsById(id)) {
            throw new RuntimeException("L’affectation avec l'id " + id + " n'existe pas.");
        }
        affectationRepository.deleteById(id);
    }

    public AffectationDTO toDto(Affectation aff) {
    AffectationDTO dto = new AffectationDTO();
    dto.setId(aff.getId());
    dto.setCourrierId(aff.getCourrier().getIdCourrier());
    dto.setObjetCourrier(aff.getCourrier().getObjet());
    dto.setDateReceptionCourrier(aff.getCourrier().getDateReception().toLocalDate());
    dto.setStatutCourrier(aff.getCourrier().getStatut() != null ? aff.getCourrier().getStatut().getLibelleStatut() : null);
    dto.setUtilisateurNom(aff.getUtilisateur().getNomUtilisateur());
    dto.setUtilisateurPrenom(aff.getUtilisateur().getPrenomUtilisateur());
    dto.setDirectionNom(aff.getDirection() != null ? aff.getDirection().getNomDirection() : null);
    dto.setDateAffectation(aff.getDateAffectation());
    dto.setMotif(aff.getMotif());
    return dto;
}

public List<AffectationDTO> getMesAffectationsDto(Utilisateur utilisateur) {
    return affectationRepository.findByUtilisateur(utilisateur)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
}
}