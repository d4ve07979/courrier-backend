package tg.inseed.gestioncourrier.gestioncourrier.courriers;

import java.sql.Date;
import java.util.List;

public class CourrierDetailsResponse {

    private Long idCourrier;
    private String objet;
    private Date dateReception;

    private String expediteurNom;
    private String expediteurEmail;

    private String destinataireNom;
    private String destinataireEmail;

    private String typeCourrier;
    private String statut;
    private String ficheReference;

    private List<String> fichiersTelechargeables;

    // Getters et setters

    public Long getIdCourrier() {
        return idCourrier;
    }

    public void setIdCourrier(Long idCourrier) {
        this.idCourrier = idCourrier;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

    public String getExpediteurNom() {
        return expediteurNom;
    }

    public void setExpediteurNom(String expediteurNom) {
        this.expediteurNom = expediteurNom;
    }

    public String getExpediteurEmail() {
        return expediteurEmail;
    }

    public void setExpediteurEmail(String expediteurEmail) {
        this.expediteurEmail = expediteurEmail;
    }

    public String getDestinataireNom() {
        return destinataireNom;
    }

    public void setDestinataireNom(String destinataireNom) {
        this.destinataireNom = destinataireNom;
    }

    public String getDestinataireEmail() {
        return destinataireEmail;
    }

    public void setDestinataireEmail(String destinataireEmail) {
        this.destinataireEmail = destinataireEmail;
    }

    public String getTypeCourrier() {
        return typeCourrier;
    }

    public void setTypeCourrier(String typeCourrier) {
        this.typeCourrier = typeCourrier;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getFicheReference() {
        return ficheReference;
    }

    public void setFicheReference(String ficheReference) {
        this.ficheReference = ficheReference;
    }

    public List<String> getFichiersTelechargeables() {
        return fichiersTelechargeables;
    }

    public void setFichiersTelechargeables(List<String> fichiersTelechargeables) {
        this.fichiersTelechargeables = fichiersTelechargeables;
    }
}
