// tg/inseed/gestioncourrier/gestioncourrier/courriers/CourrierUpdateRequest.java
package tg.inseed.gestioncourrier.gestioncourrier.courriers;

public class CourrierUpdateRequest {
    private String objet;
    private String dateReception;  // Reçu comme String, converti en Date
    private Long idExpediteur;
    private Long idDestinataire;
    private Long idTypeCourrier;
    private Long idStatut;
    private Long idFiche;

    // Constructeurs
    public CourrierUpdateRequest() {}

    // Getters et Setters
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }

    public String getDateReception() { return dateReception; }
    public void setDateReception(String dateReception) { this.dateReception = dateReception; }

    public Long getIdExpediteur() { return idExpediteur; }
    public void setIdExpediteur(Long idExpediteur) { this.idExpediteur = idExpediteur; }

    public Long getIdDestinataire() { return idDestinataire; }
    public void setIdDestinataire(Long idDestinataire) { this.idDestinataire = idDestinataire; }

    public Long getIdTypeCourrier() { return idTypeCourrier; }
    public void setIdTypeCourrier(Long idTypeCourrier) { this.idTypeCourrier = idTypeCourrier; }

    public Long getIdStatut() { return idStatut; }
    public void setIdStatut(Long idStatut) { this.idStatut = idStatut; }

    public Long getIdFiche() { return idFiche; }
    public void setIdFiche(Long idFiche) { this.idFiche = idFiche; }
}