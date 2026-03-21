package tg.inseed.gestioncourrier.gestioncourrier.journalisation;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Classe représentant une entrée de journalisation.
 * Elle permet de tracer toutes les actions effectuées par les utilisateurs
 * dans le système de gestion du courrier (connexion, modification, suppression, etc.).
 *
 * <p>Chaque action est associée à un utilisateur, un type d’action,
 * une entité concernée et des métadonnées comme l’adresse IP et le navigateur.</p>
 *
 * <p>Cette classe est persistée en base via JPA et exposée en JSON
 * pour l’audit et la traçabilité.</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Entity
@Getter
@Setter
@Table(name = "journalisation", indexes = {
    @Index(name = "idx_utilisateur", columnList = "id_utilisateur"),
    @Index(name = "idx_date_action", columnList = "date_action"),
    @Index(name = "idx_type_action", columnList = "type_action")
})
public class Journalisation {

    /**
     * Identifiant unique de l’entrée de journalisation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_journalisation")
    @JsonProperty("id_journalisation")
    private Long idJournalisation;

    /**
     * Utilisateur ayant effectué l’action.
     */
    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    @JsonProperty("utilisateur")
    private Utilisateur utilisateur;

    /**
     * Type d’action réalisée (ex: CREATE, UPDATE, DELETE, LOGIN, LOGOUT).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false, length = 50)
    @JsonProperty("type_action")
    private TypeAction typeAction;

    /**
     * Entité concernée par l’action (ex: UTILISATEUR, COURRIER, DIRECTION).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entite_concernee", length = 50)
    @JsonProperty("entite_concernee")
    private EntiteConcernee entiteConcernee;

    /**
     * Identifiant de l’entité concernée (ex: ID du courrier modifié).
     */
    @Column(name = "id_entite")
    @JsonProperty("id_entite")
    private Long idEntite;

    /**
     * Description détaillée de l’action effectuée.
     */
    @Column(name = "description", nullable = false, length = 500)
    @JsonProperty("description")
    private String description;

    /**
     * Date et heure de l’action.
     */
    @Column(name = "date_action", nullable = false)
    @JsonProperty("date_action")
    private LocalDateTime dateAction;

    /**
     * Adresse IP de l’utilisateur au moment de l’action.
     */
    @Column(name = "adresse_ip", length = 45)
    @JsonProperty("adresse_ip")
    private String adresseIp;

    /**
     * Informations sur le navigateur ou client utilisé (User-Agent).
     */
    @Column(name = "user_agent", length = 255)
    @JsonProperty("user_agent")
    private String userAgent;

    /**
     * Anciennes valeurs (avant modification).
     * Utilisé pour tracer les changements.
     */
    @Column(name = "anciennes_valeurs", columnDefinition = "TEXT")
    @JsonProperty("anciennes_valeurs")
    private String anciennesValeurs;

    /**
     * Nouvelles valeurs (après modification).
     * Utilisé pour tracer les changements.
     */
    @Column(name = "nouvelles_valeurs", columnDefinition = "TEXT")
    @JsonProperty("nouvelles_valeurs")
    private String nouvellesValeurs;

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Journalisation() {}

    // =========================
    // ENUMERATIONS
    // =========================

    /**
     * Enumération des types d’actions possibles.
     */
    public enum TypeAction {
        CREATE("Création"),
        UPDATE("Modification"),
        DELETE("Suppression"),
        LOGIN("Connexion"),
        LOGOUT("Déconnexion"),
        VIEW("Consultation"),
        DOWNLOAD("Téléchargement"),
        UPLOAD("Upload"),
        ASSIGN("Affectation"),
        VALIDATE("Validation"),
        REJECT("Rejet"),
        ARCHIVE("Archivage"),
        RESTORE("Restauration");

        private final String libelle;

        TypeAction(String libelle) {
            this.libelle = libelle;
        }

        /**
         * Retourne le libellé lisible de l’action.
         *
         * @return libellé de l’action
         */
        public String getLibelle() {
            return libelle;
        }
    }

    /**
     * Enumération des entités concernées par une action.
     */
    public enum EntiteConcernee {
        UTILISATEUR("Utilisateur"),
        COURRIER("Courrier"),
        DIRECTION("Direction"),
        AFFECTATION("Affectation"),
        FICHE_TRANSMISSION("Fiche de transmission"),
        FICHIER("Fichier"),
        SESSION("Session"),
        DECHARGE("Décharge"),
        ARCHIVE("Archive");

        private final String libelle;

        EntiteConcernee(String libelle) {
            this.libelle = libelle;
        }

        /**
         * Retourne le libellé lisible de l’entité.
         *
         * @return libellé de l’entité
         */
        public String getLibelle() {
            return libelle;
        }
    }

    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Génère un hash basé sur l’identifiant unique de la journalisation.
     *
     * @return valeur de hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(idJournalisation);
    }

    /**
     * Vérifie l’égalité entre deux objets Journalisation.
     * Deux entrées sont égales si elles possèdent le même identifiant.
     *
     * @param obj objet à comparer
     * @return true si les entrées sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Journalisation other = (Journalisation) obj;
        return Objects.equals(idJournalisation, other.idJournalisation);
    }

    /**
     * Retourne une représentation textuelle de l’entrée de journalisation.
     * Utile pour le débogage et les logs.
     *
     * @return chaîne descriptive de la journalisation
     */
    @Override
    public String toString() {
        return "Journalisation [id=" + idJournalisation + 
               ", typeAction=" + typeAction + 
               ", entite=" + entiteConcernee + 
               ", utilisateur=" + (utilisateur != null ? utilisateur.getEmailUtilisateur() : "null") + 
               ", date=" + dateAction + "]";
    }
}
