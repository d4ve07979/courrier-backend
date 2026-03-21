package tg.inseed.gestioncourrier.gestioncourrier.statut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier pour la gestion des statuts.
 *
 * <p>Ce service centralise la logique de création, mise à jour,
 * suppression et consultation des statuts. Il fournit également
 * des méthodes pour obtenir des statistiques sur les courriers
 * associés à chaque statut.</p>
 *
 * <p>Les statuts définissent le cycle de vie des courriers
 * (exemple : "En attente", "En cours", "Traité", "Classé").</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Service
@Transactional
public class StatutService {

    @Autowired
    private StatutRepository statutRepository;

    /**
     * Crée un nouveau statut.
     *
     * @param statut objet Statut à créer
     * @return le statut créé et sauvegardé
     * @throws RuntimeException si un statut avec le même code existe déjà
     */
    public Statut createStatut(Statut statut) {
        if (statutRepository.existsByCodeStatut(statut.getCodeStatut())) {
            throw new RuntimeException("❌ Un statut avec le code " + statut.getCodeStatut() + " existe déjà");
        }
        return statutRepository.save(statut);
    }

    /**
     * Récupère tous les statuts existants.
     *
     * @return liste de tous les statuts
     */
    public List<Statut> getAllStatuts() {
        return statutRepository.findAll();
    }

    /**
     * Récupère uniquement les statuts actifs, triés par ordre d’affichage.
     *
     * @return liste des statuts actifs triés
     */
    public List<Statut> getStatutsActifs() {
        return statutRepository.findByActifTrueOrderByOrdreAsc();
    }

    /**
     * Récupère un statut par son code unique.
     *
     * @param code code du statut
     * @return le statut correspondant
     * @throws RuntimeException si aucun statut n’est trouvé
     */
    public Statut getStatutByCode(String code) {
        return statutRepository.findByCodeStatut(code)
            .orElseThrow(() -> new RuntimeException("❌ Statut introuvable avec le code: " + code));
    }

    /**
     * Récupère un statut par son identifiant.
     *
     * @param id identifiant du statut
     * @return le statut correspondant
     * @throws RuntimeException si aucun statut n’est trouvé
     */
    public Statut getStatutById(Long id) {
        return statutRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("❌ Statut introuvable avec l'id: " + id));
    }

    /**
     * Met à jour un statut existant.
     *
     * @param id identifiant du statut à mettre à jour
     * @param newStatut objet contenant les nouvelles valeurs
     * @return le statut mis à jour
     */
    public Statut updateStatut(Long id, Statut newStatut) {
        Statut statut = getStatutById(id);
        statut.setLibelleStatut(newStatut.getLibelleStatut());
        statut.setDescription(newStatut.getDescription());
        statut.setCouleur(newStatut.getCouleur());
        statut.setIcone(newStatut.getIcone());
        statut.setOrdre(newStatut.getOrdre());
        statut.setStatutFinal(newStatut.isStatutFinal());
        statut.setActif(newStatut.isActif());
        return statutRepository.save(statut);
    }

    /**
     * Supprime un statut.
     *
     * <p>Un statut ne peut pas être supprimé s’il est encore utilisé
     * par un ou plusieurs courriers.</p>
     *
     * @param id identifiant du statut à supprimer
     * @throws RuntimeException si le statut est utilisé par des courriers
     */
    public void deleteStatut(Long id) {
        Statut statut = getStatutById(id);
        
        if (!statut.getCourriers().isEmpty()) {
            throw new RuntimeException("❌ Impossible de supprimer ce statut : " + 
                                     statut.getCourriers().size() + " courrier(s) l'utilisent encore");
        }
        
        statutRepository.deleteById(id);
    }

    /**
     * Génère des statistiques sur les courriers par statut.
     *
     * @return map contenant :
     *         - total_courriers : nombre total de courriers
     *         - par_statut : répartition par libellé de statut
     *         - nombre_statuts : nombre total de statuts
     */
    public Map<String, Object> getStatistiques() {
        List<Object[]> results = statutRepository.countCourriersParStatut();
        
        Map<String, Long> parStatut = new HashMap<>();
        long total = 0;
        
        for (Object[] result : results) {
            String libelle = (String) result[0];
            Long count = (Long) result[1];
            parStatut.put(libelle, count);
            total += count;
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_courriers", total);
        stats.put("par_statut", parStatut);
        stats.put("nombre_statuts", statutRepository.count());
        
        return stats;
    }

    /**
     * Récupère les statuts actifs avec le nombre de courriers associés.
     *
     * @return liste de maps contenant les informations du statut et le nombre de courriers
     */
    public List<Map<String, Object>> getStatutsAvecCount() {
        List<Statut> statuts = getStatutsActifs();
        
        return statuts.stream().map(statut -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_statut", statut.getIdStatut());
            map.put("code_statut", statut.getCodeStatut());
            map.put("libelle_statut", statut.getLibelleStatut());
            map.put("description", statut.getDescription());
            map.put("couleur", statut.getCouleur());
            map.put("icone", statut.getIcone());
            map.put("ordre", statut.getOrdre());
            map.put("statut_final", statut.isStatutFinal());
            map.put("nombre_courriers", statut.getCourriers().size());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Vérifie si un changement de statut est autorisé selon les règles métier.
     *
     * <p>Exemples de règles :
     * <ul>
     *   <li>Un statut final ne peut plus être modifié.</li>
     *   <li>Impossible de revenir à "En attente" depuis "Traité".</li>
     * </ul>
     * </p>
     *
     * @param statutActuel statut actuel du courrier
     * @param nouveauStatut statut cible
     * @return true si le changement est autorisé, false sinon
     */
    public boolean peutChangerStatut(Statut statutActuel, Statut nouveauStatut) {
        if (statutActuel.isStatutFinal()) {
            return false;
        }
        
        if (statutActuel.getCodeStatut().equals("TRAITE") && 
            nouveauStatut.getCodeStatut().equals("EN_ATTENTE")) {
            return false;
        }
        
        // Ajoutez d'autres règles métier ici
        return true;
    }
}
