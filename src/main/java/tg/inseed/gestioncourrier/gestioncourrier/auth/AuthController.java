package tg.inseed.gestioncourrier.gestioncourrier.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // ✅ AJOUT
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tg.inseed.gestioncourrier.gestioncourrier.security.JwtService;
import tg.inseed.gestioncourrier.gestioncourrier.session.SessionService;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder; // ✅ INJECTION DIRECTE

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final ConcurrentHashMap<String, VerificationCode> codes = new ConcurrentHashMap<>();

/**
 * 🔐 CONNEXION STANDARD
 */
@PostMapping("/login")
public ResponseEntity<?> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest) { // 🆕 AJOUT
    try {
        Utilisateur utilisateur = utilisateurRepository
            .findByEmailUtilisateur(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (utilisateur.getVerrouille()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                "success", false,
                "message", "🔒 Compte verrouillé. Contactez le service informatique."
            ));
        }

        if (!utilisateur.getActif()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "❌ Compte inactif. Contactez le service informatique."
            ));
        }

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        utilisateur.reinitialiserTentativesEchec();
        utilisateurRepository.save(utilisateur);

        // ✅ Génération des tokens
        String accessToken = jwtService.generateToken(utilisateur);
        String refreshToken = jwtService.generateRefreshToken(utilisateur);

        // 🆕 Création automatique de la session
        String adresseIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        sessionService.creerSession(utilisateur, accessToken, adresseIp, userAgent);

        // ✅ Construction de la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("access_token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("token_type", "Bearer");
        response.put("utilisateur", Map.of(
            "id", utilisateur.getIdUtilisateur(),
            "nom", utilisateur.getNomUtilisateur(),
            "prenom", utilisateur.getPrenomUtilisateur(),
            "email", utilisateur.getEmailUtilisateur(),
            "role", utilisateur.getRoleString(),
            "direction", utilisateur.getDirection() != null 
                ? utilisateur.getDirection().getNomDirection() 
                : "Aucune direction"
        ));

        System.out.println("✅ Connexion réussie : " + utilisateur.getEmailUtilisateur());

        return ResponseEntity.ok(response);

    } catch (BadCredentialsException e) {
        utilisateurRepository.findByEmailUtilisateur(request.getEmail())
            .ifPresent(user -> {
                user.incrementerTentativesEchec();
                utilisateurRepository.save(user);
                System.out.println("⚠️ Tentative échouée : " + request.getEmail());
            });

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "message", "❌ Email ou mot de passe incorrect"
        ));
    }
}


    /**
     * 🔄 Rafraîchissement du token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.getRefreshToken());
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (jwtService.isTokenValid(request.getRefreshToken(), utilisateur)) {
                String accessToken = jwtService.generateToken(utilisateur);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "access_token", accessToken,
                    "token_type", "Bearer"
                ));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Token de rafraîchissement invalide"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "Erreur lors du rafraîchissement"
            ));
        }
    }

    /**
     * 🔑 Changement de mot de passe
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, 
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // ✅ UTILISATION DIRECTE DE passwordEncoder
            if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "L'ancien mot de passe est incorrect"
                ));
            }

            if (!request.getNouveauMotDePasse().equals(request.getConfirmationMotDePasse())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Les mots de passe ne correspondent pas"
                ));
            }

            // ✅ UTILISATION DIRECTE DE passwordEncoder
            utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
            utilisateurRepository.save(utilisateur);

            System.out.println("✅ Mot de passe changé : " + email);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ Mot de passe modifié avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "❌ Erreur lors du changement de mot de passe"
            ));
        }
    }

    /**
     * 🚪 Déconnexion
     */
    @PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) { // 🆕 AJOUT
    try {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 🆕 FERMER LA SESSION
            sessionService.fermerSession(token);
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Déconnexion réussie"
        ));
    } catch (Exception e) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Déconnexion réussie"
        )); // On retourne toujours succès même en cas d'erreur
    }
}

    /**
     * 📧 Connexion par code (Étape 1)
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        var utilisateurOpt = utilisateurRepository.findByEmailUtilisateur(email);
        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "❌ Aucun compte associé à cet email"
            ));
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        if (!utilisateur.getActif()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", "❌ Compte inactif"
            ));
        }

        if (utilisateur.getVerrouille()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                "success", false,
                "message", "🔒 Compte verrouillé"
            ));
        }

        String code = String.format("%06d", (int) (Math.random() * 999999));
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        codes.put(email, new VerificationCode(code, expiration));

        sendVerificationEmail(email, code, utilisateur.getPrenomUtilisateur());

        System.out.println("📧 Code envoyé à : " + email);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "✅ Code envoyé à " + email
        ));
    }

    /**
     * 📧 Connexion par code (Étape 2)
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        VerificationCode storedCode = codes.get(email);
        if (storedCode == null || storedCode.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Code expiré ou inexistant"
            ));
        }

        if (!storedCode.getCode().equals(code)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "❌ Code incorrect"
            ));
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmailUtilisateur(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        codes.remove(email);

        String accessToken = jwtService.generateToken(utilisateur);
        String refreshToken = jwtService.generateRefreshToken(utilisateur);

        System.out.println("✅ Connexion par code : " + email);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "access_token", accessToken,
            "refresh_token", refreshToken,
            "token_type", "Bearer",
            "utilisateur", Map.of(
                "id", utilisateur.getIdUtilisateur(),
                "nom", utilisateur.getNomUtilisateur(),
                "prenom", utilisateur.getPrenomUtilisateur(),
                "email", utilisateur.getEmailUtilisateur(),
                "role", utilisateur.getRoleString(),
                "direction", utilisateur.getDirection() != null 
                    ? utilisateur.getDirection().getNomDirection() 
                    : "Aucune direction"
            )
        ));
    }

    // ---------- MÉTHODES UTILITAIRES ----------

    private void sendVerificationEmail(String email, String code, String prenom) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Code de vérification - INSEED");
            message.setText(
                "Bonjour " + prenom + ",\n\n" +
                "Votre code de vérification est : " + code + "\n\n" +
                "Ce code expire dans 5 minutes.\n\n" +
                "Cordialement,\nService Informatique - INSEED"
            );

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email : " + e.getMessage());
        }
    }

    static class VerificationCode {
        private final String code;
        private final LocalDateTime expiration;

        public VerificationCode(String code, LocalDateTime expiration) {
            this.code = code;
            this.expiration = expiration;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiration);
        }
    }
}