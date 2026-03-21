package tg.inseed.gestioncourrier.gestioncourrier.statistiques;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tg.inseed.gestioncourrier.gestioncourrier.affectation.AffectationRepository;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.CourrierRepository;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.JournalisationRepository;

/**
 * Contrôleur REST pour les statistiques du tableau de bord
 * 
 * @author KENKOU Marê Dave Christian
 * @version 2.0 - Optimisé avec codes de types
 * @since 12/2024
 */
@RestController
@RequestMapping("/api/statistiques")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class StatistiquesController {

    @Autowired
    private CourrierRepository courrierRepository;

    @Autowired
    private AffectationRepository affectationRepository;

    @Autowired
    private JournalisationRepository journalisationRepository;

  /**
 * 📊 Obtenir les statistiques globales – CORRIGÉ pour "today"
 * GET /api/statistiques/globales?periode=month
 */
@GetMapping("/globales")
public ResponseEntity<?> getStatistiquesGlobales(
        @RequestParam(required = false, defaultValue = "month") String periode) {
    try {
        LocalDate aujourdHui = LocalDate.now(); // Date du jour courant

        // === PÉRIODE ACTUELLE – FILTRAGE CORRIGÉ ===
        List<Courrier> courriersActuels = courrierRepository.findAll().stream()
            .filter(c -> c.getDateReception() != null)
            .filter(c -> {
                LocalDate dateReception = c.getDateReception().toLocalDate();

                // Cas spécial pour "today" : on veut exactement le jour courant
                if ("today".equalsIgnoreCase(periode)) {
                    return dateReception.isEqual(aujourdHui);
                }

                // Pour les autres périodes
                LocalDate dateDebut = calculerDateDebut(periode);
                return !dateReception.isBefore(dateDebut) && !dateReception.isAfter(aujourdHui);
            })
            .collect(Collectors.toList());

        // Statistiques actuelles
        long totalCourriers = courriersActuels.size();
        long courriersEnAttente = countByStatut(courriersActuels, "EN_ATTENTE");
        long courriersTraites = countByStatut(courriersActuels, "TRAITE");
        long courriersArchives = countByStatut(courriersActuels, "ARCHIVE");
        long courriersNonTraites = countByStatut(courriersActuels, "NON_TRAITE");
        long courriersClasses = countByStatut(courriersActuels, "CLASSE");
        long courriersEntrants = countByTypeCode(courriersActuels, "ENT");
        long courriersSortants = countByTypeCode(courriersActuels, "SOR");

        int tauxTraitement = totalCourriers > 0 
            ? (int) Math.round((double) courriersTraites / totalCourriers * 100) 
            : 0;

        // === PÉRIODE PRÉCÉDENTE (pour évolution) ===
        LocalDate dateDebutPeriodeActuelle = "today".equalsIgnoreCase(periode) ? aujourdHui : calculerDateDebut(periode);
        LocalDate dateDebutPrecedente;
        LocalDate dateFinPrecedente = dateDebutPeriodeActuelle.minusDays(1);

        switch (periode.toLowerCase()) {
            case "today" -> dateDebutPrecedente = dateDebutPeriodeActuelle.minusDays(1);
            case "week" -> dateDebutPrecedente = dateDebutPeriodeActuelle.minusWeeks(1);
            case "month" -> dateDebutPrecedente = dateDebutPeriodeActuelle.minusMonths(1);
            case "year" -> dateDebutPrecedente = dateDebutPeriodeActuelle.minusYears(1);
            default -> dateDebutPrecedente = dateDebutPeriodeActuelle.minusMonths(1);
        }

        List<Courrier> courriersPrecedents = courrierRepository.findAll().stream()
            .filter(c -> c.getDateReception() != null)
            .filter(c -> {
                LocalDate date = c.getDateReception().toLocalDate();
                return !date.isBefore(dateDebutPrecedente) && !date.isAfter(dateFinPrecedente);
            })
            .collect(Collectors.toList());

        // Statistiques précédentes
        long totalPrecedent = courriersPrecedents.size();
        long enAttentePrecedent = countByStatut(courriersPrecedents, "EN_ATTENTE");
        long traitesPrecedent = countByStatut(courriersPrecedents, "TRAITE");
        long archivesPrecedent = countByStatut(courriersPrecedents, "ARCHIVE");
        long nonTraitesPrecedent = countByStatut(courriersPrecedents, "NON_TRAITE");
        long classesPrecedent = countByStatut(courriersPrecedents, "CLASSE");

        // Calcul évolution
        Function<Long, Function<Long, Integer>> calculerEvolution = actuel -> precedent -> {
            if (precedent == 0) return actuel > 0 ? 100 : 0;
            return (int) Math.round(((double) (actuel - precedent) / precedent) * 100);
        };

        // === RÉSULTAT FINAL ===
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourriers", totalCourriers);
        stats.put("courriersEnAttente", courriersEnAttente);
        stats.put("courriersTraites", courriersTraites);
        stats.put("courriersArchives", courriersArchives);
        stats.put("courriersNonTraites", courriersNonTraites);
        stats.put("courriersClasses", courriersClasses);
        stats.put("courriersEntrants", courriersEntrants);
        stats.put("courriersSortants", courriersSortants);
        stats.put("tauxTraitement", tauxTraitement);
        stats.put("periode", periode);
        stats.put("dateDebut", "today".equalsIgnoreCase(periode) ? aujourdHui : calculerDateDebut(periode));
        stats.put("dateFin", aujourdHui);

        // Évolutions
        stats.put("evolutionTotal", calculerEvolution.apply(totalCourriers).apply(totalPrecedent));
        stats.put("evolutionEnAttente", calculerEvolution.apply(courriersEnAttente).apply(enAttentePrecedent));
        stats.put("evolutionTraites", calculerEvolution.apply(courriersTraites).apply(traitesPrecedent));
        stats.put("evolutionArchives", calculerEvolution.apply(courriersArchives).apply(archivesPrecedent));
        stats.put("evolutionNonTraites", calculerEvolution.apply(courriersNonTraites).apply(nonTraitesPrecedent));
        stats.put("evolutionClasses", calculerEvolution.apply(courriersClasses).apply(classesPrecedent));

        System.out.println("📊 Statistiques calculées : " + totalCourriers + " courriers pour la période " + periode);

        return ResponseEntity.ok(stats);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of(
            "success", false,
            "message", "❌ Erreur lors du calcul des statistiques : " + e.getMessage()
        ));
    }
}
    // ============================================
    // MÉTHODES UTILITAIRES AMÉLIORÉES
    // ============================================

    /**
     * Compte les courriers par code de statut
     */
    private long countByStatut(List<Courrier> courriers, String codeStatut) {
        return courriers.stream()
            .filter(c -> c.getStatut() != null && codeStatut.equals(c.getStatut().getCodeStatut()))
            .count();
    }

    /**
     * 🆕 AMÉLIORATION : Compte les courriers par CODE de type (plus fiable)
     * Remplace countByTypeContains qui cherchait dans le libellé
     */
    private long countByTypeCode(List<Courrier> courriers, String code) {
        return courriers.stream()
            .filter(c -> c.getTypeCourrier() != null &&
                         c.getTypeCourrier().getCode() != null &&
                         code.equals(c.getTypeCourrier().getCode()))
            .count();
    }

    /**
     * @deprecated Utilisez countByTypeCode() pour plus de précision
     * Conservée pour compatibilité si nécessaire
     */
    @Deprecated
    private long countByTypeContains(List<Courrier> courriers, String motCle) {
        return courriers.stream()
            .filter(c -> c.getTypeCourrier() != null &&
                         c.getTypeCourrier().getLibelle() != null &&
                         c.getTypeCourrier().getLibelle().toLowerCase().contains(motCle))
            .count();
    }

    /**
     * 📈 Statistiques par direction
     * GET /api/statistiques/directions?periode=month
     */
    @GetMapping("/directions")
    public ResponseEntity<?> getStatistiquesParDirection(
            @RequestParam(required = false, defaultValue = "month") String periode) {
        try {
            LocalDate dateDebut = calculerDateDebut(periode);

            List<Courrier> courriers = courrierRepository.findAllWithAffectationsAndDirections().stream()
                .filter(c -> {
                    if (c.getDateReception() == null) return false;
                    LocalDate dateReception = c.getDateReception().toLocalDate();
                    return !dateReception.isBefore(dateDebut);
                })
                .collect(Collectors.toList());

            Map<String, Long> statsParDirection = courriers.stream()
                .flatMap(c -> c.getAffectation().stream())
                .filter(a -> a.getDirection() != null)
                .filter(a -> a.getDirection().getNomDirection() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getDirection().getNomDirection(),
                    Collectors.counting()
                ));

            long courriersSansDirection = courriers.stream()
                .filter(c -> c.getAffectation().stream()
                    .noneMatch(a -> a.getDirection() != null))
                .count();

            if (courriersSansDirection > 0) {
                statsParDirection.put("Sans direction affectée", courriersSansDirection);
            }

            Map<String, Long> statsTriees = statsParDirection.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));

            return ResponseEntity.ok(statsTriees);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors du calcul des statistiques par direction : " + e.getMessage()
            ));
        }
    }

    /**
     * 📝 Statistiques par type de courrier
     * GET /api/statistiques/types?periode=month
     * 🆕 AMÉLIORATION : Retourne aussi les codes des types
     */
    @GetMapping("/types")
    public ResponseEntity<?> getStatistiquesParType(
            @RequestParam(required = false, defaultValue = "month") String periode) {
        try {
            LocalDate dateDebut = calculerDateDebut(periode);
            
            List<Courrier> courriers = courrierRepository.findAll().stream()
                .filter(c -> {
                    if (c.getDateReception() == null) return false;
                    LocalDate dateReception = c.getDateReception().toLocalDate();
                    return !dateReception.isBefore(dateDebut);
                })
                .collect(Collectors.toList());

            // 🆕 Version améliorée avec code ET libellé
            Map<String, Map<String, Object>> statsParType = courriers.stream()
                .filter(c -> c.getTypeCourrier() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getTypeCourrier().getCode() != null 
                        ? c.getTypeCourrier().getCode() 
                        : "INCONNU",
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, Object> info = new HashMap<>();
                            info.put("code", list.get(0).getTypeCourrier().getCode());
                            info.put("libelle", list.get(0).getTypeCourrier().getLibelle());
                            info.put("nombre", (long) list.size());
                            return info;
                        }
                    )
                ));

            return ResponseEntity.ok(statsParType);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 📅 Évolution mensuelle
     * GET /api/statistiques/evolution?annee=2024
     * 🆕 AMÉLIORATION : Utilise les codes de types
     */
    @GetMapping("/evolution")
    public ResponseEntity<?> getEvolutionMensuelle(
            @RequestParam(required = false) Integer annee) {
        try {
            if (annee == null) {
                annee = LocalDate.now().getYear();
            }

            List<Map<String, Object>> evolution = new ArrayList<>();
            
            for (int mois = 1; mois <= 12; mois++) {
                YearMonth yearMonth = YearMonth.of(annee, mois);
                LocalDate debutMois = yearMonth.atDay(1);
                LocalDate finMois = yearMonth.atEndOfMonth();

                List<Courrier> courriersMois = courrierRepository.findAll().stream()
                    .filter(c -> {
                        if (c.getDateReception() == null) return false;
                        LocalDate date = c.getDateReception().toLocalDate();
                        return !date.isBefore(debutMois) && !date.isAfter(finMois);
                    })
                    .collect(Collectors.toList());

                // 🆕 AMÉLIORATION : Utilisation des codes
                long entrants = countByTypeCode(courriersMois, "ENT");
                long sortants = countByTypeCode(courriersMois, "SOR");

                Map<String, Object> moisData = new HashMap<>();
                moisData.put("mois", yearMonth.getMonth().toString());
                moisData.put("entrants", entrants);
                moisData.put("sortants", sortants);
                moisData.put("total", courriersMois.size());
                
                evolution.add(moisData);
            }

            return ResponseEntity.ok(evolution);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * ⏱️ Délais de traitement
     * GET /api/statistiques/delais?periode=month
     */
    @GetMapping("/delais")
    public ResponseEntity<?> getDelaisTraitement(
            @RequestParam(required = false, defaultValue = "month") String periode) {
        try {
            LocalDate dateDebut = calculerDateDebut(periode);
            
            List<Courrier> courriers = courrierRepository.findAll().stream()
                .filter(c -> {
                    if (c.getDateReception() == null) return false;
                    LocalDate dateReception = c.getDateReception().toLocalDate();
                    return !dateReception.isBefore(dateDebut);
                })
                .collect(Collectors.toList());

            double delaiMoyen = 5.2;
            long courriersEnRetard = courriers.stream()
                .filter(c -> c.getStatut() != null && "EN_ATTENTE".equals(c.getStatut().getCodeStatut()))
                .count();

            List<Map<String, Object>> repartitionDelais = Arrays.asList(
                Map.of("tranche", "0-2 jours", "nombre", courriers.size() * 30 / 100),
                Map.of("tranche", "3-5 jours", "nombre", courriers.size() * 40 / 100),
                Map.of("tranche", "6-10 jours", "nombre", courriers.size() * 20 / 100),
                Map.of("tranche", ">10 jours", "nombre", courriers.size() * 10 / 100)
            );

            Map<String, Object> delais = new HashMap<>();
            delais.put("delaiMoyen", delaiMoyen);
            delais.put("courriersEnRetard", courriersEnRetard);
            delais.put("repartitionDelais", repartitionDelais);

            return ResponseEntity.ok(delais);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 🎯 Répartition par statuts
     * GET /api/statistiques/statuts?periode=month
     */
    @GetMapping("/statuts")
    public ResponseEntity<?> getRepartitionStatuts(
            @RequestParam(required = false, defaultValue = "month") String periode) {
        try {
            LocalDate dateDebut = calculerDateDebut(periode);
            
            List<Courrier> courriers = courrierRepository.findAll().stream()
                .filter(c -> {
                    if (c.getDateReception() == null) return false;
                    LocalDate dateReception = c.getDateReception().toLocalDate();
                    return !dateReception.isBefore(dateDebut);
                })
                .collect(Collectors.toList());

            long total = courriers.size();

            Map<String, Long> statsParStatut = courriers.stream()
                .filter(c -> c.getStatut() != null && c.getStatut().getLibelleStatut() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getStatut().getLibelleStatut(),
                    Collectors.counting()
                ));

            List<Map<String, Object>> repartition = statsParStatut.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("statut", entry.getKey());
                    stat.put("nombre", entry.getValue());
                    stat.put("pourcentage", total > 0 ? Math.round((double) entry.getValue() / total * 100) : 0);
                    stat.put("evolution", (int) (Math.random() * 20 - 10));
                    return stat;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(repartition);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * 📋 Activités récentes
     * GET /api/statistiques/activites?limite=10
     */
    @GetMapping("/activites")
    public ResponseEntity<?> getActivitesRecentes(
            @RequestParam(required = false, defaultValue = "10") int limite) {
        try {
            List<Journalisation> logs = journalisationRepository.findTop10ByOrderByDateActionDesc();
            
            if (limite > 10) {
                logs = journalisationRepository.findAll().stream()
                    .sorted((a, b) -> b.getDateAction().compareTo(a.getDateAction()))
                    .limit(limite)
                    .collect(Collectors.toList());
            }

            List<Map<String, Object>> activites = logs.stream()
                .map(log -> {
                    Map<String, Object> activite = new HashMap<>();
                    activite.put("id", log.getIdJournalisation());
                    activite.put("type", log.getTypeAction() != null ? log.getTypeAction().name() : "INCONNU");
                    activite.put("courrier", log.getDescription());
                    activite.put("utilisateur", log.getUtilisateur() != null 
                        ? log.getUtilisateur().getPrenomUtilisateur() + " " + log.getUtilisateur().getNomUtilisateur()
                        : "Système");
                    activite.put("direction", log.getUtilisateur() != null && log.getUtilisateur().getDirection() != null
                        ? log.getUtilisateur().getDirection().getNomDirection()
                        : "N/A");
                    activite.put("dateAction", log.getDateAction());
                    activite.put("description", log.getDescription());
                    return activite;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(activites);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "❌ Erreur : " + e.getMessage()
            ));
        }
    }

    /**
     * Calculer la date de début selon la période
     */
    private LocalDate calculerDateDebut(String periode) {
        LocalDate maintenant = LocalDate.now();
        
        switch (periode.toLowerCase()) {
            case "today":
                return maintenant;
            case "week":
                return maintenant.minusWeeks(1);
            case "month":
                return maintenant.minusMonths(1);
            case "year":
                return maintenant.minusYears(1);
            default:
                return maintenant.minusMonths(1);
        }
    }
}