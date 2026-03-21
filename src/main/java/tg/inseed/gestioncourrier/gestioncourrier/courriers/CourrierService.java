package tg.inseed.gestioncourrier.gestioncourrier.courriers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission.FicheDeTransmission;
import tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission.FicheDeTransmissionRepository;
import tg.inseed.gestioncourrier.gestioncourrier.destinataitre.Destinataire;
import tg.inseed.gestioncourrier.gestioncourrier.destinataitre.DestinataireRepository;
import tg.inseed.gestioncourrier.gestioncourrier.expediteur.Expediteur;
import tg.inseed.gestioncourrier.gestioncourrier.expediteur.ExpediteurRepository;
import tg.inseed.gestioncourrier.gestioncourrier.statut.Statut;
import tg.inseed.gestioncourrier.gestioncourrier.statut.StatutRepository;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.TypeCourrier;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.TypeCourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Service de gestion des courriers
 * 
 * @author KENKOU Marê Dave Christian
 * @version 2.0
 */
@Service
@Transactional
public class CourrierService {

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private TypeCourrierRepository typeCourrierRepository; // 🆕 AJOUT

    @Autowired
private ExpediteurRepository expediteurRepository;

@Autowired
private DestinataireRepository destinataireRepository;

@Autowired
private StatutRepository statutRepository;

@Autowired
private FicheDeTransmissionRepository ficheDeTransmissionRepository;

    /**
     * 🆕 Valider qu'un type de courrier peut être utilisé
     * Vérifie que le type existe et est actif
     * 
     * @param idType Identifiant du type de courrier
     * @throws RuntimeException si le type n'existe pas ou est inactif
     */
    public void validateTypeCourrier(Long idType) {
        if (idType == null) {
            throw new RuntimeException("❌ Le type de courrier est obligatoire");
        }

        TypeCourrier type = typeCourrierRepository.findById(idType)
            .orElseThrow(() -> new RuntimeException("❌ Type de courrier introuvable (ID: " + idType + ")"));
        
        if (!Boolean.TRUE.equals(type.getActif())) {
            throw new RuntimeException("❌ Le type de courrier '" + type.getLibelle() + "' est inactif et ne peut pas être utilisé");
        }

        System.out.println("✅ Type de courrier validé : " + type.getCode() + " - " + type.getLibelle());
    }

    /**
     * 🆕 Créer un courrier avec validation complète
     * - Valide que le type de courrier est actif
     * - Définit automatiquement le créateur
     * 
     * @param courrier Courrier à créer
     * @param createur Utilisateur créateur du courrier
     * @return Courrier créé et sauvegardé
     */
    public Courrier createCourrier(Courrier courrier, Utilisateur createur) {
        // Validation du type de courrier
        if (courrier.getTypeCourrier() != null && courrier.getTypeCourrier().getIdType() != null) {
            validateTypeCourrier(courrier.getTypeCourrier().getIdType());
        } else {
            throw new RuntimeException("❌ Le type de courrier est obligatoire");
        }
        
        // Définir le créateur
        courrier.setCreateur(createur);
        
        System.out.println("📧 Création courrier : " + courrier.getObjet() + 
                         " par " + createur.getEmailUtilisateur() +
                         " (Type: " + courrier.getTypeCourrier().getCode() + " - " + 
                         courrier.getTypeCourrier().getLibelle() + ")");
        
        return courrierRepository.save(courrier);
    }

    /**
     * ⚠️ ADMIN UNIQUEMENT : Récupérer tous les courriers
     */
    public List<Courrier> getAllCourriers() {
        return courrierRepository.findAll();
    }

    /**
     * 🆕 Récupérer TOUS les courriers accessibles par un utilisateur :
     * - Courriers créés par lui
     * - Courriers affectés à lui
     * - Courriers affectés à sa direction
     */
    public List<Courrier> getCourriersAccessiblesParUtilisateur(Utilisateur utilisateur) {
        Long idUtilisateur = utilisateur.getIdUtilisateur();
        Long idDirection = utilisateur.getDirection() != null 
            ? utilisateur.getDirection().getIdDirection() 
            : null;

        List<Courrier> courriers = courrierRepository.findCourriersAccessiblesParUtilisateur(
            idUtilisateur, idDirection
        );

        System.out.println("📊 " + courriers.size() + " courrier(s) accessible(s) pour " + 
                         utilisateur.getEmailUtilisateur() + 
                         " (créés: " + courrierRepository.findByCreateur(utilisateur).size() + 
                         ", affectés: " + (courriers.size() - courrierRepository.findByCreateur(utilisateur).size()) + ")");

        return courriers;
    }

    /**
     * 🆕 Vérifier si un utilisateur peut accéder à un courrier
     */
    public boolean utilisateurPeutAccederAuCourrier(Long idCourrier, Utilisateur utilisateur) {
        Long idUtilisateur = utilisateur.getIdUtilisateur();
        Long idDirection = utilisateur.getDirection() != null 
            ? utilisateur.getDirection().getIdDirection() 
            : null;

        return courrierRepository.utilisateurPeutAcceder(idCourrier, idUtilisateur, idDirection);
    }

    /**
     * Récupérer un courrier par ID
     */
   public Courrier getCourrierById(Long id) {
    return courrierRepository.findByIdWithAllRelations(id)
        .orElseThrow(() -> new RuntimeException("❌ Courrier introuvable avec l'id: " + id));
}

    /**
 * Mettre à jour un courrier avec validation
 */
public Courrier updateCourrier(Long id, CourrierUpdateRequest request) {
    Courrier courrier = getCourrierById(id);
    
    // Logs pour déboguer
    System.out.println("📝 Mise à jour courrier ID: " + id);
    System.out.println("📦 Request reçue: " + request);
    
    // Mise à jour des champs simples
    courrier.setObjet(request.getObjet());
    
    // Conversion de la date (String → java.sql.Date)
    if (request.getDateReception() != null) {
        courrier.setDateReception(Date.valueOf(request.getDateReception()));
    }
    
    // Mise à jour des relations par ID
    if (request.getIdExpediteur() != null) {
        Expediteur expediteur = expediteurRepository.findById(request.getIdExpediteur())
            .orElseThrow(() -> new RuntimeException("❌ Expéditeur introuvable"));
        courrier.setExpediteur(expediteur);
    }
    
    if (request.getIdDestinataire() != null) {
        Destinataire destinataire = destinataireRepository.findById(request.getIdDestinataire())
            .orElseThrow(() -> new RuntimeException("❌ Destinataire introuvable"));
        courrier.setDestinataire(destinataire);
    }
    
    if (request.getIdTypeCourrier() != null) {
        TypeCourrier typeCourrier = typeCourrierRepository.findById(request.getIdTypeCourrier())
            .orElseThrow(() -> new RuntimeException("❌ Type de courrier introuvable"));
        
        // Valider le nouveau type de courrier
        if (!typeCourrier.getIdType().equals(courrier.getTypeCourrier().getIdType())) {
            validateTypeCourrier(request.getIdTypeCourrier());
        }
        courrier.setTypeCourrier(typeCourrier);
    }
    
    if (request.getIdStatut() != null) {
        Statut statut = statutRepository.findById(request.getIdStatut())
            .orElseThrow(() -> new RuntimeException("❌ Statut introuvable"));
        courrier.setStatut(statut);
    }
    
    // Fiche de transmission (optionnel)
    if (request.getIdFiche() != null) {
        FicheDeTransmission fiche = ficheDeTransmissionRepository.findById(request.getIdFiche())
            .orElse(null);
        courrier.setFicheDeTransmission(fiche);
    }
    
    System.out.println("✅ Courrier mis à jour: " + courrier.getIdCourrier());
    
    return courrierRepository.save(courrier);
}

    /**
     * Supprimer un courrier
     */
    public void deleteCourrier(Long id) {
        if (!courrierRepository.existsById(id)) {
            throw new RuntimeException("❌ Le courrier avec l'id " + id + " n'existe pas");
        }
        courrierRepository.deleteById(id);
        System.out.println("🗑️ Courrier supprimé : ID " + id);
    }

public List<Courrier> rechercherCourriersAccessibles(String query, Utilisateur utilisateur) {
    String lowerQuery = query.toLowerCase();

    return courrierRepository.findCourriersAccessiblesParUtilisateur(
        utilisateur.getIdUtilisateur(),
        utilisateur.getDirection() != null ? utilisateur.getDirection().getIdDirection() : null
    ).stream()
     .filter(c -> 
         (c.getObjet() != null && c.getObjet().toLowerCase().contains(lowerQuery)) ||
         (c.getExpediteur() != null && c.getExpediteur().getNomDeStructure() != null && 
          c.getExpediteur().getNomDeStructure().toLowerCase().contains(lowerQuery)) ||
         (c.getDestinataire() != null && c.getDestinataire().getNomDeStructure() != null && 
          c.getDestinataire().getNomDeStructure().toLowerCase().contains(lowerQuery)) ||
         (c.getFicheDeTransmission() != null && 
          ((c.getFicheDeTransmission().getReference() != null && 
            c.getFicheDeTransmission().getReference().toLowerCase().contains(lowerQuery)) ||
           (c.getFicheDeTransmission().getObservation() != null && 
            c.getFicheDeTransmission().getObservation().toLowerCase().contains(lowerQuery))))
     )
     .collect(Collectors.toList());
}

public MesStatistiquesDTO getMesStatistiques(
        Utilisateur utilisateur,
        String recherche,   // ⚠️ Ignoré (car problème bytea)
        String statutFiltre,
        String typeFiltre,
        LocalDate dateDebut,
        LocalDate dateFin) {
    
    Long directionId = utilisateur.getDirection() != null 
        ? utilisateur.getDirection().getIdDirection() 
        : null;
    
    // Utilisation des méthodes SANS recherche
    List<Object[]> statutCounts = courrierRepository.countByStatutAccessibleWithoutRecherche(
        utilisateur.getIdUtilisateur(), directionId, statutFiltre, typeFiltre, dateDebut, dateFin);
    
    List<Object[]> typeCounts = courrierRepository.countByTypeAccessibleWithoutRecherche(
        utilisateur.getIdUtilisateur(), directionId, statutFiltre, typeFiltre, dateDebut, dateFin);
    
    long total = courrierRepository.countAccessibleWithFiltersWithoutRecherche(
        utilisateur.getIdUtilisateur(), directionId, statutFiltre, typeFiltre, dateDebut, dateFin);
    
    // Construction des maps (inchangé)
    Map<String, Long> parStatut = new HashMap<>();
    for (Object[] row : statutCounts) {
        parStatut.put((String) row[0], (Long) row[1]);
    }
    
    Map<String, Long> parType = new HashMap<>();
    for (Object[] row : typeCounts) {
        parType.put((String) row[0], (Long) row[1]);
    }
    
    // Champs dérivés
    long entrants = parType.getOrDefault("ENT", 0L);
    long sortants = parType.getOrDefault("SOR", 0L);
    long enAttente = parStatut.getOrDefault("EN_ATTENTE", 0L);
    long enCours = parStatut.getOrDefault("EN_COURS", 0L);
    long traites = parStatut.getOrDefault("TRAITE", 0L);
    long archives = parStatut.getOrDefault("ARCHIVE", 0L);
    long classes = parStatut.getOrDefault("CLASSE", 0L);
    long rejetes = parStatut.getOrDefault("REJETE", 0L);
    long urgents = parStatut.getOrDefault("URGENT", 0L);
    
    MesStatistiquesDTO dto = new MesStatistiquesDTO();
    dto.setTotal(total);
    dto.setParStatut(parStatut);
    dto.setParType(parType);
    dto.setEntrants(entrants);
    dto.setSortants(sortants);
    dto.setEnAttente(enAttente);
    dto.setEnCours(enCours);
    dto.setTraites(traites);
    dto.setArchives(archives);
    dto.setClasses(classes);
    dto.setRejetes(rejetes);
    dto.setUrgents(urgents);
    
    return dto;
}


}