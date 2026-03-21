package tg.inseed.gestioncourrier.gestioncourrier.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Représente les données nécessaires à l'enregistrement d'un nouvel utilisateur via l'inscription publique.
 * Cette classe est utilisée pour valider les informations envoyées par le client lors de la création de compte publique.
 * Le rôle est affecté automatiquement par le système.
 */
@Data // Génère automatiquement les getters, setters, equals, hashCode et toString
public class PublicRegisterRequest {

    /**
     * Nom de famille de l'utilisateur.
     * Ce champ est obligatoire et doit contenir entre 2 et 50 caractères pour éviter les saisies trop courtes ou trop longues.
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    /**
     * Prénom de l'utilisateur.
     * Ce champ est obligatoire et doit contenir entre 2 et 75 caractères pour garantir une saisie complète et lisible.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 75, message = "Le prénom doit contenir entre 2 et 75 caractères")
    private String prenom;

    /**
     * Adresse email de l'utilisateur.
     * Ce champ est obligatoire et doit respecter le format standard d'une adresse email.
     * Il est utilisé comme identifiant principal pour l'authentification.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Mot de passe choisi par l'utilisateur.
     * Ce champ est obligatoire et doit respecter les critères de robustesse suivants :
     * - Minimum 8 caractères
     * - Au moins une lettre majuscule
     * - Au moins une lettre minuscule
     * - Au moins un chiffre
     * - Au moins un caractère spécial (ex: @, #, $, %, etc.)
     * Ces règles visent à renforcer la sécurité contre les attaques par dictionnaire ou force brute.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;
}