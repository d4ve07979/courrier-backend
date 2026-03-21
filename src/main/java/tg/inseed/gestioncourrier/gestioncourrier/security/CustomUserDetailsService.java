package tg.inseed.gestioncourrier.gestioncourrier.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.UtilisateurRepository;

/**
 * Service personnalisé pour la récupération des détails d'un utilisateur.
 * Implémente l'interface {@link UserDetailsService} utilisée par Spring Security pour l'authentification.
 * Ce service permet de charger un utilisateur à partir de son adresse email.
 * 
 * Utilisé automatiquement par Spring lors du processus de login.
 * 
 * @author KENKOU
 * @version 1.0
 * @since 10/2025
 */
@Service
@RequiredArgsConstructor // Génère un constructeur avec tous les champs final
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository d'accès aux données des utilisateurs.
     * Utilisé pour rechercher un utilisateur par son adresse email.
     */
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Charge les détails d'un utilisateur à partir de son identifiant (email).
     * Méthode appelée automatiquement par Spring Security lors de l'authentification.
     * 
     * @param username adresse email de l'utilisateur
     * @return l'utilisateur correspondant, sous forme de {@link UserDetails}
     * @throws UsernameNotFoundException si aucun utilisateur n'est trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmailUtilisateur(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
    }
}
