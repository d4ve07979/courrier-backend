package tg.inseed.gestioncourrier.gestioncourrier.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Représente la requête de changement de mot de passe d’un utilisateur.
 * Cette classe est utilisée pour valider les données envoyées par le client lors du changement de mot de passe.
 * Elle applique des contraintes de sécurité strictes pour garantir la robustesse du nouveau mot de passe.
 */
@Data // Génère automatiquement les getters, setters, equals, hashCode et toString
public class ChangePasswordRequest {

    /**
     * Ancien mot de passe de l'utilisateur.
     * Ce champ est obligatoire pour vérifier que l'utilisateur connaît son mot de passe actuel.
     */
    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;

    /**
     * Nouveau mot de passe souhaité par l'utilisateur.
     * Ce champ est soumis à plusieurs contraintes :
     * - Doit contenir au moins 8 caractères pour éviter les mots de passe trop courts.
     * - Doit inclure au moins :
     *   - une lettre majuscule,
     *   - une lettre minuscule,
     *   - un chiffre,
     *   - un caractère spécial (ex: @, #, $, %, etc.)
     * Ces règles visent à renforcer la sécurité contre les attaques par dictionnaire ou force brute.
     */
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String nouveauMotDePasse;

    /**
     * Confirmation du nouveau mot de passe.
     * Ce champ permet de s'assurer que l'utilisateur n’a pas fait d’erreur de saisie.
     * La correspondance entre ce champ et {@code nouveauMotDePasse} est vérifiée dans le contrôleur.
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmationMotDePasse;
}
