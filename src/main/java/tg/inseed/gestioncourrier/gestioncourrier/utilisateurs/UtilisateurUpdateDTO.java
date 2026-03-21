package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurUpdateDTO {
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String emailUtilisateur;
    private RoleUtilisateur role;
    private boolean actif;
    private boolean verrouille;
}
