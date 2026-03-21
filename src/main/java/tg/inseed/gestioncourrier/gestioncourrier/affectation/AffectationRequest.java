package tg.inseed.gestioncourrier.gestioncourrier.affectation;



import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AffectationRequest {

    @NotNull(message = "L'identifiant du courrier est obligatoire")
    private Long idCourrier;

    @NotNull(message = "L'identifiant de l'utilisateur à affecter est obligatoire")
    private Long idUtilisateur;

    @NotNull(message = "L'identifiant de la direction est obligatoire")
    private Long idDirection;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String motif;

    // Getters et setters
    public Long getIdCourrier() {
        return idCourrier;
    }

    public void setIdCourrier(Long idCourrier) {
        this.idCourrier = idCourrier;
    }

    public Long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Long getIdDirection() {
        return idDirection;
    }

    public void setIdDirection(Long idDirection) {
        this.idDirection = idDirection;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}

