package tg.inseed.gestioncourrier.gestioncourrier.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Représente les données envoyées par le client lors d'une tentative de connexion.
 * Cette classe est utilisée pour valider les champs requis avant de procéder à l'authentification.
 */
@Data // Génère automatiquement les getters, setters, equals, hashCode et toString
public class LoginRequest {

    /**
     * Adresse email de l'utilisateur.
     * Ce champ est obligatoire et doit respecter le format d'une adresse email valide.
     * Il est utilisé comme identifiant principal pour l'authentification.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Mot de passe associé à l'adresse email.
     * Ce champ est obligatoire et doit être fourni pour valider l'identité de l'utilisateur.
     * La robustesse du mot de passe est vérifiée côté base de données via l'encodeur.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}
