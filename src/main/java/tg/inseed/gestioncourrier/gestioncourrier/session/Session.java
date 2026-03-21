package tg.inseed.gestioncourrier.gestioncourrier.session;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.utilisateurs.Utilisateur;

/**
 * Classe représentant une session de connexion d’un utilisateur.
 * Elle permet de suivre les informations liées à la connexion et déconnexion,
 * ainsi que des métadonnées comme l’adresse IP, le navigateur utilisé et l’état de la session.
 *
 * <p>Cette classe est persistée en base de données via JPA et exposée en JSON
 * pour les échanges avec le frontend.</p>
 *
 * @author KENKOU
 * @version 2.0
 * @since 12/2025
 */
@Entity
@Getter
@Setter
@Table(name = "session")
public class Session {

    /**
     * Identifiant unique de la session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_session")
    @JsonProperty("id_session")
    private Long idSession;

    /**
     * Utilisateur associé à la session.
     * Relation ManyToOne car un utilisateur peut avoir plusieurs sessions.
     */
    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    @JsonProperty("utilisateur")
    private Utilisateur utilisateur;

    /**
     * Date et heure de connexion.
     * Exemple : 2025-12-28T08:30:00
     */
    @Column(name = "date_connexion", nullable = false)
    @JsonProperty("date_connexion")
    private LocalDateTime dateConnexion;

    /**
     * Date et heure de déconnexion.
     * Exemple : 2025-12-28T17:45:00
     */
    @Column(name = "date_deconnexion")
    @JsonProperty("date_deconnexion")
    private LocalDateTime dateDeconnexion;

    // 🆕 AJOUTS pour un meilleur tracking

    /**
     * Adresse IP de l’utilisateur lors de la connexion.
     * Longueur maximale : 45 caractères (IPv4 ou IPv6).
     */
    @Column(name = "adresse_ip", length = 45)
    @JsonProperty("adresse_ip")
    private String adresseIp;

    /**
     * Informations sur le navigateur ou client utilisé (User-Agent).
     * Exemple : "Mozilla/5.0 (Windows NT 10.0; Win64; x64)".
     */
    @Column(name = "user_agent", length = 255)
    @JsonProperty("user_agent")
    private String userAgent;

    /**
     * Jeton JWT associé à la session.
     * ⚠️ Ne jamais exposer ce champ en JSON pour des raisons de sécurité.
     */
    @Column(name = "token_jwt", length = 500)
    @JsonIgnore
    private String tokenJwt;

    /**
     * Indique si la session est encore active.
     * Par défaut, une session est considérée active jusqu’à déconnexion.
     */
    @Column(name = "active")
    @JsonProperty("active")
    private boolean active = true;

    /**
     * Constructeur sans argument requis par JPA.
     */
    public Session() {}

    /**
     * Calcule la durée de la session en heures et minutes.
     * Si la session est encore active, la durée est calculée jusqu’à l’instant présent.
     *
     * @return durée de la session au format "Xh Ymin", ou "N/A" si la date de connexion est absente.
     */
    @Transient
    @JsonProperty("duree_session")
    public String getDureeSession() {
        if (dateConnexion == null) return "N/A";
        
        LocalDateTime fin = dateDeconnexion != null ? dateDeconnexion : LocalDateTime.now();
        Duration duree = Duration.between(dateConnexion, fin);
        
        long heures = duree.toHours();
        long minutes = duree.toMinutes() % 60;
        
        return String.format("%dh %dmin", heures, minutes);
    }

    /**
     * Vérifie si la session est encore active.
     * Une session est active si le champ {@code active} est vrai
     * et qu’aucune date de déconnexion n’est enregistrée.
     *
     * @return true si la session est active, false sinon.
     */
    @Transient
    @JsonProperty("est_active")
    public boolean estActive() {
        return active && dateDeconnexion == null;
    }

    /**
     * Génère un hash basé sur l’identifiant unique de la session.
     *
     * @return valeur de hashCode.
     */
    @Override
    public int hashCode() {
        return idSession != null ? idSession.hashCode() : 0;
    }

    /**
     * Vérifie l’égalité entre deux objets Session.
     * Deux sessions sont égales si elles possèdent le même identifiant.
     *
     * @param obj objet à comparer
     * @return true si les sessions sont identiques, false sinon.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Session other = (Session) obj;
        return idSession != null && idSession.equals(other.idSession);
    }

    /**
     * Retourne une représentation textuelle de la session.
     * Utile pour le débogage et les logs.
     *
     * @return chaîne descriptive de la session.
     */
    @Override
    public String toString() {
        return "Session [idSession=" + idSession + 
               ", utilisateur=" + (utilisateur != null ? utilisateur.getEmailUtilisateur() : "null") + 
               ", dateConnexion=" + dateConnexion + 
               ", dateDeconnexion=" + dateDeconnexion + 
               ", active=" + active + "]";
    }
}
