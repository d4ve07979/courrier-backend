package tg.inseed.gestioncourrier.gestioncourrier.journalisation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.EntiteConcernee;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation.TypeAction;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Service métier pour la gestion des journalisations.
 * 
 * <p>Ce service centralise la logique de création et de récupération
 * des logs d’actions effectuées par les utilisateurs dans le système.</p>
 * 
 * <p>Il permet de tracer les opérations sensibles (création, modification,
 * suppression, connexion, déconnexion, etc.) et de fournir des statistiques
 * pour l’audit et la supervision.</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Service
@Transactional
public class JournalisationService {

    @Autowired
    private JournalisationRepository journalisationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Journaliser une action simple sans anciennes/nouvelles valeurs.
     *
     * @param typeAction type d’action réalisée
     * @param entite entité concernée
     * @param idEntite identifiant de l’entité concernée
     * @param description description de l’action
     * @param utilisateur utilisateur ayant effectué l’action
     * @param request requête HTTP pour extraire IP et User-Agent
     */
    public void journaliser(
            TypeAction typeAction,
            EntiteConcernee entite,
            Long idEntite,
            String description,
            Utilisateur utilisateur,
            HttpServletRequest request) {
        
        journaliser(typeAction, entite, idEntite, description, utilisateur, request, null, null);
    }

    /**
     * Journaliser une action avec traçage des anciennes et nouvelles valeurs.
     *
     * @param typeAction type d’action réalisée
     * @param entite entité concernée
     * @param idEntite identifiant de l’entité concernée
     * @param description description de l’action
     * @param utilisateur utilisateur ayant effectué l’action
     * @param request requête HTTP pour extraire IP et User-Agent
     * @param anciennesValeurs état avant modification
     * @param nouvellesValeurs état après modification
     */
    public void journaliser(
            TypeAction typeAction,
            EntiteConcernee entite,
            Long idEntite,
            String description,
            Utilisateur utilisateur,
            HttpServletRequest request,
            Object anciennesValeurs,
            Object nouvellesValeurs) {
        
        try {
            Journalisation log = new Journalisation();
            log.setTypeAction(typeAction);
            log.setEntiteConcernee(entite);
            log.setIdEntite(idEntite);
            log.setDescription(description);
            log.setUtilisateur(utilisateur);
            log.setDateAction(LocalDateTime.now());
            
            if (request != null) {
                log.setAdresseIp(getClientIp(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
            
            if (anciennesValeurs != null) {
                log.setAnciennesValeurs(objectMapper.writeValueAsString(anciennesValeurs));
            }
            
            if (nouvellesValeurs != null) {
                log.setNouvellesValeurs(objectMapper.writeValueAsString(nouvellesValeurs));
            }
            
            journalisationRepository.save(log);
            
            System.out.println("📝 LOG: " + typeAction + " - " + description + " par " + utilisateur.getEmailUtilisateur());
            
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur lors de la sérialisation des valeurs : " + e.getMessage());
        }
    }

    // =========================
    // MÉTHODES SIMPLIFIÉES
    // =========================

    /** Journaliser une création */
    public void logCreation(EntiteConcernee entite, Long idEntite, String description, 
                           Utilisateur utilisateur, HttpServletRequest request) {
        journaliser(TypeAction.CREATE, entite, idEntite, description, utilisateur, request);
    }

    /** Journaliser une modification avec avant/après */
    public void logModification(EntiteConcernee entite, Long idEntite, String description, 
                               Utilisateur utilisateur, HttpServletRequest request,
                               Object avant, Object apres) {
        journaliser(TypeAction.UPDATE, entite, idEntite, description, utilisateur, request, avant, apres);
    }

    /** Journaliser une suppression */
    public void logSuppression(EntiteConcernee entite, Long idEntite, String description, 
                              Utilisateur utilisateur, HttpServletRequest request) {
        journaliser(TypeAction.DELETE, entite, idEntite, description, utilisateur, request);
    }

    /** Journaliser une consultation */
    public void logConsultation(EntiteConcernee entite, Long idEntite, String description, 
                               Utilisateur utilisateur, HttpServletRequest request) {
        journaliser(TypeAction.VIEW, entite, idEntite, description, utilisateur, request);
    }

    /** Journaliser une connexion */
    public void logConnexion(Utilisateur utilisateur, HttpServletRequest request) {
        journaliser(TypeAction.LOGIN, EntiteConcernee.SESSION, null, 
                   "Connexion réussie", utilisateur, request);
    }

    /** Journaliser une déconnexion */
    public void logDeconnexion(Utilisateur utilisateur, HttpServletRequest request) {
        journaliser(TypeAction.LOGOUT, EntiteConcernee.SESSION, null, 
                   "Déconnexion", utilisateur, request);
    }

    // =========================
    // MÉTHODES DE RÉCUPÉRATION
    // =========================

    /** Récupérer tous les logs triés par date décroissante */
    public List<Journalisation> getAllLogs() {
        return journalisationRepository.findAll(Sort.by(Sort.Direction.DESC, "dateAction"));
    }

    /** Récupérer les logs avec pagination */
    public Page<Journalisation> getLogsPagines(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return journalisationRepository.findAll(pageable);
    }

    /** Récupérer les logs d’un utilisateur */
    public List<Journalisation> getLogsUtilisateur(Utilisateur utilisateur) {
        return journalisationRepository.findByUtilisateur(utilisateur);
    }

    /** Récupérer les logs d’une entité spécifique */
    public List<Journalisation> getLogsEntite(EntiteConcernee entite, Long idEntite) {
        return journalisationRepository.findByEntiteConcerneeAndIdEntite(entite, idEntite);
    }

    /** Récupérer les logs avec filtres multiples */
    public Page<Journalisation> getLogsAvecFiltres(
            Utilisateur utilisateur,
            TypeAction typeAction,
            EntiteConcernee entite,
            LocalDateTime debut,
            LocalDateTime fin,
            int page,
            int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateAction"));
        return journalisationRepository.findWithFilters(utilisateur, typeAction, entite, debut, fin, pageable);
    }

    // =========================
    // STATISTIQUES
    // =========================

    /**
     * Génère des statistiques sur les logs :
     * <ul>
     *   <li>Total des logs</li>
     *   <li>Répartition par type d’action</li>
     *   <li>Les 10 derniers logs</li>
     * </ul>
     *
     * @return map contenant les statistiques
     */
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", journalisationRepository.count());
        
        Map<String, Long> parType = new HashMap<>();
        for (TypeAction type : TypeAction.values()) {
            parType.put(type.name(), journalisationRepository.countByTypeAction(type));
        }
        stats.put("par_type", parType);
        
        stats.put("derniers_logs", journalisationRepository.findTop10ByOrderByDateActionDesc());
        
        return stats;
    }

    // =========================
    // UTILITAIRES
    // =========================

        /**
     * Extrait l’adresse IP du client à partir des en-têtes HTTP.
     * 
     * <p>Cette méthode vérifie plusieurs en-têtes standards utilisés
     * par les proxys et load balancers (X-Forwarded-For, Proxy-Client-IP,
     * WL-Proxy-Client-IP). Si aucune information n’est trouvée, elle
     * retourne l’adresse IP directe fournie par {@code request.getRemoteAddr()}.</p>
     *
     * @param request requête HTTP
     * @return adresse IP du client
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    // =========================
    // MÉTHODES CRUD
    // =========================

    /**
     * Récupère une entrée de journalisation par son identifiant.
     *
     * @param id identifiant de la journalisation
     * @return l’entrée correspondante
     * @throws RuntimeException si aucune entrée n’est trouvée
     */
    public Journalisation getJournalisationById(Long id) {
        return journalisationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("❌ Journalisation introuvable avec l'id: " + id));
    }

    /**
     * Supprime une entrée de journalisation par son identifiant.
     *
     * @param id identifiant de la journalisation
     * @throws RuntimeException si l’entrée n’existe pas
     */
    public void deleteJournalisation(Long id) {
        if (!journalisationRepository.existsById(id)) {
            throw new RuntimeException("❌ La journalisation avec l'id " + id + " n'existe pas");
        }
        journalisationRepository.deleteById(id);
    }
}
