package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UtilisateurCreateDTO {
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String emailUtilisateur;
    private String motDePasse;
    private RoleUtilisateur role;
    
    @Override
    public String toString() {
        return "UtilisateurCreateDTO [nomUtilisateur=" + nomUtilisateur + ", prenomUtilisateur=" + prenomUtilisateur
                + ", emailUtilisateur=" + emailUtilisateur + ", motDePasse=" + motDePasse + ", role=" + role + "]";
    }

  


}

