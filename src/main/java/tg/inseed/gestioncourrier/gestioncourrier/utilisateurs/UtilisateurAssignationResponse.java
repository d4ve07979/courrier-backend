package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurAssignationResponse {
    private Long idUtilisateur;
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String emailUtilisateur;
    private String roleUtilisateur;
    private String directionUtilisateur;

    // Getters et setters

}