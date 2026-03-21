package tg.inseed.gestioncourrier.gestioncourrier.FicheDeTransmission;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO pour créer un courrier complet avec fiche de transmission
 */
@Data
public class CourrierCompletRequest {
    // Données du courrier
    @JsonProperty("objet")
    private String objet;
    
    @JsonProperty("dateReception")
    private String dateReception; // Format: "2025-12-27"
    
    @JsonProperty("idExpediteur")
    private Long idExpediteur;
    
    @JsonProperty("idDestinataire")
    private Long idDestinataire;
    
    @JsonProperty("idTypeCourrier")
    private Long idTypeCourrier;
    
    @JsonProperty("idStatut")
    private Long idStatut;
    
    // Données de la fiche de transmission (optionnelles)
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("observation")
    private String observation;
    
    @JsonProperty("tresUrgent")
    private boolean tresUrgent;
    
    @JsonProperty("urgent")
    private boolean urgent;
    
    @JsonProperty("menParler")
    private boolean menParler;
    
    @JsonProperty("pourAttribution")
    private boolean pourAttribution;
    
    @JsonProperty("pourEtudeEtAvis")
    private boolean pourEtudeEtAvis;
    
    @JsonProperty("pourDisposition")
    private boolean pourDisposition;
    
    @JsonProperty("elementDeReponse")
    private boolean elementDeReponse;
    
    @JsonProperty("finRetour")
    private boolean finRetour;
    
    @JsonProperty("pourSuiteADonner")
    private boolean pourSuiteADonner;
    
    @JsonProperty("pourVisaPrealable")
    private boolean pourVisaPrealable;
    
    @JsonProperty("pourNecessaire")
    private boolean pourNecessaire;
    
    @JsonProperty("notePourLeDG")
    private boolean notePourLeDG;
    
    @JsonProperty("pourResumerSuccinct")
    private boolean pourResumerSuccinct;
    
    @JsonProperty("noteMinistre")
    private boolean noteMinistre;
    
    @JsonProperty("copieA")
    private boolean copieA;
    
    @JsonProperty("meRepresenter")
    private boolean meRepresenter;
    
    @JsonProperty("pourEtude")
    private boolean pourEtude;
    
    @JsonProperty("lettreDeTransmission")
    private boolean lettreDeTransmission;
    
    @JsonProperty("bordeauEnvoi")
    private boolean bordeauEnvoi;
    
    @JsonProperty("pourInformation")
    private boolean pourInformation;
    
    @JsonProperty("aTouteFinUtile") // ✅ Correction ici
    private boolean aTouteFinUtile;
    
    @JsonProperty("enInstance")
    private boolean enInstance;
    
    @JsonProperty("aClasser") // ✅ Correction ici
    private boolean aClasser;
    
    @JsonProperty("courrierReserve")
    private boolean courrierReserve;
}
