package tg.inseed.gestioncourrier.gestioncourrier.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Représente la requête envoyée par le client pour obtenir un nouveau token d'accès.
 * Cette classe est utilisée dans le mécanisme de rafraîchissement JWT, permettant à l'utilisateur
 * de rester connecté sans devoir se réauthentifier avec ses identifiants.
 */
@Data // Génère automatiquement les getters, setters, equals, hashCode et toString
public class RefreshTokenRequest {

    /**
     * Token de rafraîchissement JWT précédemment délivré au client.
     * Ce champ est obligatoire et doit être transmis pour valider la session et générer un nouveau token d'accès.
     * La validité du token est vérifiée côté serveur avant toute régénération.
     */
    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}
