package tg.inseed.gestioncourrier.gestioncourrier.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import tg.inseed.gestioncourrier.gestioncourrier.statut.Statut;
import tg.inseed.gestioncourrier.gestioncourrier.statut.StatutRepository;

/**
 * Classe permettant d’initialiser les statuts par défaut au démarrage de l’application.
 *
 * <p>Ce composant Spring Boot est exécuté automatiquement grâce à l’implémentation
 * de {@link CommandLineRunner}. Il vérifie si la table des statuts est vide et,
 * dans ce cas, insère les statuts prédéfinis nécessaires au bon fonctionnement
 * du système de gestion du courrier.</p>
 *
 * <p>Les statuts définissent le cycle de vie des courriers (exemple : "En attente",
 * "En cours", "Traité", "Classé").</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Component
public class StatutInitializer implements CommandLineRunner {

    @Autowired
    private StatutRepository statutRepository;

    /**
     * Méthode exécutée automatiquement au démarrage de l’application.
     * 
     * <p>Si aucun statut n’existe en base, elle initialise une liste
     * de statuts prédéfinis avec leurs propriétés (code, libellé,
     * description, couleur, icône, ordre, statut final).</p>
     *
     * @param args arguments passés à l’application (non utilisés ici)
     * @throws Exception en cas d’erreur lors de l’initialisation
     */
    @Override
    public void run(String... args) throws Exception {
        if (statutRepository.count() == 0) {
            System.out.println("📋 Initialisation des statuts...");
            
            // Statuts de courrier
            creerStatut("EN_ATTENTE", "En attente", "Courrier reçu, en attente de traitement", "#ffc107", "clock", 1, false);
            creerStatut("EN_COURS", "En cours de traitement", "Courrier en cours de traitement", "#17a2b8", "refresh-cw", 2, false);
            creerStatut("AFFECTE", "Affecté", "Courrier affecté à un service", "#007bff", "user-check", 3, false);
            creerStatut("TRAITE", "Traité", "Courrier traité", "#28a745", "check-circle", 4, false);
            creerStatut("URGENT", "Urgent", "Courrier urgent nécessitant un traitement immédiat", "#dc3545", "alert-circle", 5, false);
            creerStatut("CLASSE", "Classé", "Courrier classé et archivé", "#6c757d", "archive", 6, true);
            creerStatut("REJETE", "Rejeté", "Courrier rejeté", "#dc3545", "x-circle", 7, true);
            creerStatut("EN_INSTANCE", "En instance", "Courrier mis en attente temporaire", "#6c757d", "pause-circle", 8, false);
            
            System.out.println("✅ Statuts initialisés avec succès !");
        }
    }

    /**
     * Crée et enregistre un statut en base de données si celui-ci n’existe pas déjà.
     *
     * @param code code unique du statut (exemple : "EN_ATTENTE")
     * @param libelle libellé lisible du statut (exemple : "En attente")
     * @param description description détaillée du statut
     * @param couleur couleur associée au statut (exemple : "#28a745")
     * @param icone icône associée au statut (exemple : "check-circle")
     * @param ordre ordre d’affichage du statut
     * @param statutFinal indique si le statut est final (aucune transition possible après)
     */
    private void creerStatut(String code, String libelle, String description, String couleur, String icone, int ordre, boolean statutFinal) {
        if (!statutRepository.existsByCodeStatut(code)) {
            Statut statut = new Statut(code, libelle, description, couleur, ordre);
            statut.setIcone(icone);
            statut.setStatutFinal(statutFinal);
            statutRepository.save(statut);
        }
    }
}
