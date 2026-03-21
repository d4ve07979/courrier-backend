package tg.inseed.gestioncourrier.gestioncourrier.session;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

@Service
@Transactional
public class SessionService {

    @Autowired
    private SessionDataRepository sessionDataRepository;

    /**
     * 🆕 Créer une session lors de la connexion
     */
    public Session creerSession(Utilisateur utilisateur, String tokenJwt, String adresseIp, String userAgent) {
        Session session = new Session();
        session.setUtilisateur(utilisateur);
        session.setDateConnexion(LocalDateTime.now());
        session.setTokenJwt(tokenJwt);
        session.setAdresseIp(adresseIp);
        session.setUserAgent(userAgent);
        session.setActive(true);
        
        Session saved = sessionDataRepository.save(session);
        
        System.out.println("📱 Session créée : ID=" + saved.getIdSession() + 
                         " pour " + utilisateur.getEmailUtilisateur());
        
        return saved;
    }

    /**
     * 🆕 Fermer une session lors de la déconnexion
     */
    public void fermerSession(String tokenJwt) {
        sessionDataRepository.findByTokenJwt(tokenJwt).ifPresent(session -> {
            session.setDateDeconnexion(LocalDateTime.now());
            session.setActive(false);
            sessionDataRepository.save(session);
            
            System.out.println("🔚 Session fermée : ID=" + session.getIdSession());
        });
    }

    /**
     * 🆕 Fermer toutes les sessions actives d'un utilisateur
     */
    public void fermerToutesSessionsUtilisateur(Utilisateur utilisateur) {
        List<Session> sessionsActives = sessionDataRepository.findByUtilisateurAndActiveTrue(utilisateur);
        
        sessionsActives.forEach(session -> {
            session.setDateDeconnexion(LocalDateTime.now());
            session.setActive(false);
        });
        
        sessionDataRepository.saveAll(sessionsActives);
        
        System.out.println("🔚 " + sessionsActives.size() + " session(s) fermée(s) pour " + 
                         utilisateur.getEmailUtilisateur());
    }

    /**
     * Récupérer toutes les sessions
     */
    public List<Session> getAllSessions() {
        return sessionDataRepository.findAll();
    }

    /**
     * 🆕 Récupérer les sessions actives
     */
    public List<Session> getSessionsActives() {
        return sessionDataRepository.findByActiveTrue();
    }

    /**
     * 🆕 Récupérer les sessions d'un utilisateur
     */
    public List<Session> getSessionsUtilisateur(Utilisateur utilisateur) {
        return sessionDataRepository.findByUtilisateur(utilisateur);
    }

    /**
     * 🆕 Récupérer les sessions dans une période
     */
    public List<Session> getSessionsPeriode(LocalDateTime debut, LocalDateTime fin) {
        return sessionDataRepository.findSessionsBetween(debut, fin);
    }

    /**
     * Récupérer une session par ID
     */
    public Session getSessionById(Long id) {
        return sessionDataRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("❌ Session introuvable avec l'id: " + id));
    }

    /**
     * 🆕 Statistiques des sessions
     */
    public SessionStatistiques getStatistiques() {
        SessionStatistiques stats = new SessionStatistiques();
        stats.setTotalSessions(sessionDataRepository.count());
        stats.setSessionsActives(sessionDataRepository.countByActiveTrue());
        stats.setSessionsInactives(stats.getTotalSessions() - stats.getSessionsActives());
        
        return stats;
    }

    /**
     * Supprimer une session
     */
    public void deleteSession(Long id) {
        if (!sessionDataRepository.existsById(id)) {
            throw new RuntimeException("❌ La session avec l'id " + id + " n'existe pas");
        }
        sessionDataRepository.deleteById(id);
    }

    // Classe interne pour les statistiques
    public static class SessionStatistiques {
        private long totalSessions;
        private long sessionsActives;
        private long sessionsInactives;

        public long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
        
        public long getSessionsActives() { return sessionsActives; }
        public void setSessionsActives(long sessionsActives) { this.sessionsActives = sessionsActives; }
        
        public long getSessionsInactives() { return sessionsInactives; }
        public void setSessionsInactives(long sessionsInactives) { this.sessionsInactives = sessionsInactives; }
    }
}