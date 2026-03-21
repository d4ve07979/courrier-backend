package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        return new UtilisateurDTO(
            utilisateur.getIdUtilisateur(),
            utilisateur.getNomUtilisateur(),
            utilisateur.getPrenomUtilisateur(),
            utilisateur.getEmailUtilisateur(),
            utilisateur.getRoleString(),
            utilisateur.getActif(),
            utilisateur.getVerrouille(),
            utilisateur.getTentativesEchec()
        );
    }

    public Utilisateur fromCreateDTO(UtilisateurCreateDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNomUtilisateur(dto.getNomUtilisateur());
        utilisateur.setPrenomUtilisateur(dto.getPrenomUtilisateur());
        utilisateur.setEmailUtilisateur(dto.getEmailUtilisateur());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setRole(dto.getRole());
        utilisateur.setActif(true);
        utilisateur.setVerrouille(false);
        return utilisateur;
    }

    public Utilisateur fromUpdateDTO(UtilisateurUpdateDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNomUtilisateur(dto.getNomUtilisateur());
        utilisateur.setPrenomUtilisateur(dto.getPrenomUtilisateur());
        utilisateur.setEmailUtilisateur(dto.getEmailUtilisateur());
        utilisateur.setRole(dto.getRole());
        utilisateur.setActif(dto.isActif());
        utilisateur.setVerrouille(dto.isVerrouille());
        return utilisateur;
    }
}

