package tg.inseed.gestioncourrier.gestioncourrier.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tg.inseed.gestioncourrier.gestioncourrier.security.JwtAuthenticationFilter;

/**
 * Classe de configuration principale pour la sécurité de l'application.
 * Elle définit les règles d'accès, les filtres de sécurité, la gestion des sessions,
 * le système d'authentification, et la configuration CORS.
 */
@Configuration // Indique à Spring que cette classe contient des beans à injecter dans le contexte
@EnableWebSecurity // Active la sécurité web de Spring Security
@EnableMethodSecurity(prePostEnabled = true) // Permet l'utilisation des annotations @PreAuthorize et @PostAuthorize
public class SecurityConfig {

    /** Filtre personnalisé pour l'authentification JWT */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /** Service chargé de charger les détails d'un utilisateur à partir de la base de données */
    private final UserDetailsService userDetailsService;

    /**
     * Constructeur injectant les dépendances nécessaires à la configuration de sécurité.
     * @param jwtAuthFilter filtre JWT pour l'authentification stateless
     * @param userDetailsService service de récupération des utilisateurs
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Définit la chaîne de filtres de sécurité à appliquer sur les requêtes HTTP.
     * Configure les règles d'accès par rôle, la politique de session, et les filtres personnalisés.
     * @param http l'objet HttpSecurity fourni par Spring
     * @return la chaîne de filtres construite
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactive la protection CSRF car l'application est stateless (JWT)
            .csrf(csrf -> csrf.disable())

            // Active la configuration CORS définie plus bas
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Définition des règles d'autorisation par URL et rôle
            .authorizeHttpRequests(auth -> auth
                // ============================================================
                // 1. ROUTES PUBLIQUES - Accessibles sans authentification
                // ============================================================
                .requestMatchers(
                    "/api/auth/**",           // Login, register, refresh token
                    "/swagger-ui/**",          // Documentation Swagger
                    "/v3/api-docs/**",         // Documentation OpenAPI
                    "/api-docs/**",            // Documentation alternative
                    "/actuator/health",        // Health check pour monitoring
                    "/error",                  // Page d'erreur
                    "/ws-notifications/**"     // WebSocket pour notifications
                ).permitAll()

                // ============================================================
                // 2. ROUTES ACCESSIBLES À TOUS LES UTILISATEURS AUTHENTIFIÉS
                // (Ces routes sont traitées AVANT la règle générale "/api/utilisateurs/**")
                // ============================================================
                
                // Endpoints publics pour la récupération des utilisateurs (affectations, etc.)
                .requestMatchers("/api/utilisateurs/public/**").authenticated()
                .requestMatchers("/api/utilisateurs/list").authenticated()
                .requestMatchers("/api/utilisateurs/role/**").authenticated()
                .requestMatchers("/api/utilisateurs/profil").authenticated()
                .requestMatchers("/api/utilisateurs/changer-mot-de-passe").authenticated()
                
                // ============================================================
                // 3. ROUTES SPÉCIFIQUES PAR RÔLE
                // ============================================================
                
                // Routes ADMIN uniquement
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Routes DG (accessibles aussi à ADMIN)
                .requestMatchers("/api/dg/**").hasAnyRole("DG", "ADMIN")
                .requestMatchers("/api-affectations/**").hasAnyRole("DG", "ADMIN")
                .requestMatchers("/api/courriers/valider/**").hasAnyRole("DG", "ADMIN")
                .requestMatchers("/api/fiches-transmission/dg/**").hasAnyRole("DG", "ADMIN")
                
                // Routes SECRETARIAT (accessibles aussi à ADMIN)
                .requestMatchers("/api/courriers/entrants/**").hasAnyRole("SECRETARIAT", "ADMIN")
                .requestMatchers("/api/courriers/sortants/**").hasAnyRole("SECRETARIAT", "ADMIN")
                .requestMatchers("/api/fiches-transmission/**").hasAnyRole("SECRETARIAT", "ADMIN")
                .requestMatchers("/api/decharges/**").hasAnyRole("SECRETARIAT", "ADMIN")
                
                // Routes DIRECTION, DIVISION, SERVICES
                .requestMatchers("/api/courriers/traiter/**").hasAnyRole("DIRECTION", "ADMIN", "DIVISION")
                .requestMatchers("/api/courriers/mes-affectations").hasAnyRole("DIRECTION", "DIVISION", "SERVICES", "ADMIN", "SECREATARIAT")
                .requestMatchers("/api/courriers/mes-courriers").hasAnyRole("DIRECTION", "DIVISION", "SERVICES", "SECRETARIAT", "ADMIN")
                
                // Routes DIRECTION (accessibles aussi à ADMIN et DG)
                .requestMatchers("/api/directions/**").hasAnyRole("DIRECTION", "ADMIN", "DG", "SECRETARIAT")
                
                // Routes de consultation (accessibles à presque tous)
                .requestMatchers("/api/courriers/consulter/**").hasAnyRole("DIRECTION", "DIVISION", "SERVICES", "SECRETARIAT", "DG", "ADMIN")
                .requestMatchers("/api/tableau-bord/**").hasAnyRole("DIRECTION", "DIVISION", "SERVICES", "SECRETARIAT", "DG", "ADMIN")
                .requestMatchers("/api/statistiques/**").hasAnyRole("DIRECTION", "DIVISION", "SERVICES", "SECRETARIAT", "DG", "ADMIN")
                
                // Routes archives (authentification requise)
                .requestMatchers("/api/archives/**").authenticated()

                // ============================================================
                // 4. ROUTES ADMIN SEULEMENT (EN DERNIER)
                // Tout ce qui commence par /api/utilisateurs/ et n'a pas été 
                // capturé par les règles ci-dessus nécessite le rôle ADMIN
                // ============================================================
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                // ============================================================
                // 5. TOUTE AUTRE REQUÊTE nécessite une authentification
                // ============================================================
                .anyRequest().authenticated()
            )

            // Politique de session : stateless car on utilise JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Fournisseur d'authentification basé sur UserDetailsService et BCrypt
            .authenticationProvider(authenticationProvider())

            // Ajout du filtre JWT avant le filtre standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Construction finale de la chaîne de sécurité
        return http.build();
    }

    /**
     * Définit la configuration CORS pour permettre les appels cross-origin.
     * Utile pour les clients front-end hébergés sur des domaines différents.
     * @return la source de configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autorise toutes les origines (à restreindre en production)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // En-têtes autorisés dans les requêtes
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "Accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));

        // En-têtes exposés dans les réponses
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials"
        ));

        // Autorise l'envoi des cookies dans les requêtes cross-origin
        configuration.setAllowCredentials(true);

        // Durée de mise en cache des réponses CORS (en secondes)
        configuration.setMaxAge(3600L);

        // Application de la configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Définit le fournisseur d'authentification utilisé par Spring Security.
     * Utilise DaoAuthenticationProvider avec BCrypt et UserDetailsService.
     * @return le fournisseur d'authentification configuré
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Service de récupération des utilisateurs
        authProvider.setUserDetailsService(userDetailsService);

        // Encodeur de mot de passe sécurisé (BCrypt)
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Fournit le gestionnaire d'authentification à partir de la configuration globale.
     * Utile pour déclencher manuellement une authentification (ex: login endpoint).
     * @param config configuration d'authentification injectée par Spring
     * @return le gestionnaire d'authentification
     * @throws Exception en cas d'erreur
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Définit l'encodeur de mot de passe utilisé dans l'application.
     * BCrypt est recommandé pour sa robustesse contre les attaques par dictionnaire.
     * @return l'encodeur de mot de passe
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force de hachage : 12 (équilibre entre sécurité et performance)
    }
}