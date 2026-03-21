package tg.inseed.gestioncourrier.gestioncourrier.affectation;

import java.time.LocalDate;

public class AffectationDTO {
    private Long id;
    private Long courrierId;
    private String objetCourrier;
    private LocalDate dateReceptionCourrier;
    private String statutCourrier;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String directionNom;
    private LocalDate dateAffectation;
    private String motif;

    // Getters et setters (ou utilisez Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourrierId() { return courrierId; }
    public void setCourrierId(Long courrierId) { this.courrierId = courrierId; }

    public String getObjetCourrier() { return objetCourrier; }
    public void setObjetCourrier(String objetCourrier) { this.objetCourrier = objetCourrier; }

    public LocalDate getDateReceptionCourrier() { return dateReceptionCourrier; }
    public void setDateReceptionCourrier(LocalDate dateReceptionCourrier) { this.dateReceptionCourrier = dateReceptionCourrier; }

    public String getStatutCourrier() { return statutCourrier; }
    public void setStatutCourrier(String statutCourrier) { this.statutCourrier = statutCourrier; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public String getUtilisateurPrenom() { return utilisateurPrenom; }
    public void setUtilisateurPrenom(String utilisateurPrenom) { this.utilisateurPrenom = utilisateurPrenom; }

    public String getDirectionNom() { return directionNom; }
    public void setDirectionNom(String directionNom) { this.directionNom = directionNom; }

    public LocalDate getDateAffectation() { return dateAffectation; }
    public void setDateAffectation(LocalDate dateAffectation) { this.dateAffectation = dateAffectation; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
}