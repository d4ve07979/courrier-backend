package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO représentant un utilisateur pour les échanges via l'API.
 * Ne contient pas les informations sensibles ni les relations complexes.
 * 
 * @author KENKOU
 * @version 1.0
 * @since 10/2025
 */
public class UtilisateurDTO {

    private Long idUtilisateur;
    private String nomUtilisateur;
    private String prenomUtilisateur;
    private String emailUtilisateur;
    private String role; // ou RoleUtilisateur si tu veux exposer l'enum
    private boolean actif;
    private boolean verrouille;
    private int tentativesEchec;

    // Constructeurs
    public UtilisateurDTO() {}

    public UtilisateurDTO(Long idUtilisateur, String nomUtilisateur, String prenomUtilisateur,
                          String emailUtilisateur, String role, boolean actif,
                          boolean verrouille, int tentativesEchec) {
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
        this.prenomUtilisateur = prenomUtilisateur;
        this.emailUtilisateur = emailUtilisateur;
        this.role = role;
        this.actif = actif;
        this.verrouille = verrouille;
        this.tentativesEchec = tentativesEchec;
    }

    // Getters et Setters
    // (générés automatiquement avec Lombok si tu veux : @Getter @Setter)
}
