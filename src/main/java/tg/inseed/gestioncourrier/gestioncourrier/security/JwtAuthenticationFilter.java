package tg.inseed.gestioncourrier.gestioncourrier.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT appliqué à chaque requête HTTP.
 * Vérifie la présence et la validité du token JWT dans l'en-tête Authorization,
 * puis initialise le contexte de sécurité Spring avec l'utilisateur authentifié.
 * 
 * Ce filtre est exécuté une seule fois par requête grâce à {@link OncePerRequestFilter}.
 * 
 * @author KENKOU
 * @version 1.0
 * @since 10/2025
 */
@Component
@RequiredArgsConstructor // Génère un constructeur avec les dépendances injectées
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service de gestion des tokens JWT (extraction, validation, génération)
     */
    private final JwtService jwtService;

    /**
     * Service de récupération des utilisateurs à partir de leur email
     */
    private final UserDetailsService userDetailsService;

    /**
     * Méthode principale du filtre exécutée à chaque requête HTTP.
     * Vérifie si un token JWT est présent et valide, puis authentifie l'utilisateur dans le contexte Spring Security.
     * 
     * @param request requête HTTP entrante
     * @param response réponse HTTP sortante
     * @param filterChain chaîne de filtres à poursuivre
     * @throws ServletException en cas d'erreur liée au servlet
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extraction de l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si aucun token ou mauvais format, on passe au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraction du token JWT (sans le préfixe "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Extraction de l'email contenu dans le token
            userEmail = jwtService.extractUsername(jwt);

            // Si l'utilisateur est identifié et non encore authentifié dans le contexte
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Vérification de la validité du token par rapport à l'utilisateur
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Création du jeton d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    // Ajout des détails de la requête (IP, session, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Enregistrement de l'utilisateur dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log en cas d'erreur d'extraction ou de validation du token
            logger.error("Erreur JWT: " + e.getMessage());
        }

        // Poursuite de la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
