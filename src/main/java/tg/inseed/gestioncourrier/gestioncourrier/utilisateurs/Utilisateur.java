package tg.inseed.gestioncourrier.gestioncourrier.utilisateurs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.affectation.Affectation;
import tg.inseed.gestioncourrier.gestioncourrier.archive.Archive;
import tg.inseed.gestioncourrier.gestioncourrier.decharge.Decharge;
import tg.inseed.gestioncourrier.gestioncourrier.direction.Direction;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation;
import tg.inseed.gestioncourrier.gestioncourrier.session.Session;
/**
 * Entité représentant un utilisateur du système de gestion de courriers.
 * Implémente l'interface {@link UserDetails} pour l'intégration avec Spring Security.
 * Contient les informations personnelles, les rôles, les états de sécurité et les relations métiers.
 * 
 * @author KENKOU
 * @version 1.0
 * @since 10/2025
 */
@Entity
@Getter
@Setter
@Table(name = "utilisateur")
public class Utilisateur implements UserDetails {

    /**
     * Identifiant unique de l'utilisateur
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    @JsonProperty("id_utilisateur")
    private Long idUtilisateur;

    /**
     * Nom de famille de l'utilisateur
     */
    @Column(name = "nom_utilisateur", nullable = false, length = 50)
    @JsonProperty("nom_utilisateur")
    private String nomUtilisateur;

    /**
     * Prénom de l'utilisateur
     */
    @Column(name = "prenom_utilisateur", nullable = false, length = 75)
    @JsonProperty("prenom_utilisateur")
    private String prenomUtilisateur;

    /**
     * Adresse email utilisée comme identifiant de connexion
     */
    @Column(name = "email_utilisateur", nullable = false, length = 100, unique = true)
    @JsonProperty("email_utilisateur")
    private String emailUtilisateur;

    /**
     * Mot de passe encodé de l'utilisateur
     */
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    @JsonProperty("mot_de_passe")
    private String motDePasse;

    /**
     * Rôle métier de l'utilisateur (enum)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_utilisateur", nullable = false, length = 30)
    @JsonProperty("role_utilisateur")
    private RoleUtilisateur role;


    @Enumerated(EnumType.STRING)
    @Column(name = "sexe", length = 1)
    @JsonProperty("sexe")
    private Sexe sexe;

    @Column(name = "telephone", length = 20)
    @JsonProperty("telephone")
    private String telephone;

    // ==================== NOUVEAU CHAMP UTILE ====================

    @Column(name = "bureau", length = 100)
    @JsonProperty("bureau")
    private String bureau; // ex: "Bureau 305 - 2ème étage"

    // =================================================================

    @Column(name = "direction_utilisateur", length = 100)
   private String directionUtilisateur;

     // Constructeurs, UserDetails, méthodes utilitaires et equals/hashCode restent inchangés

    public enum Sexe {
        M, F
    }

    /**
     * Relation avec l'entité Role (optionnelle, pour compatibilité ou extension)
     * @ManyToOne
    @JoinColumn(name = "id_role")
    @JsonIgnore
    private Role roleEntity;
     */
    
    

    /**
     * Indique si le compte est actif (autorisé à se connecter)
     */
    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    /**
     * Indique si le compte est verrouillé (après plusieurs échecs)
     */
    @Column(name = "verrouille", nullable = false)
    private boolean verrouille = false;

    /**
     * Nombre de tentatives d'authentification échouées
     */
    @Column(name = "tentatives_echec", nullable = false)
    private int tentativesEchec = 0;

    /**
     * Nombre maximal de tentatives avant verrouillage
     */
    @Transient
    private final int MAX_TENTATIVES = 3;

    // Relations métiers

    /**
     * Historique des actions de l'utilisateur
     */
    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private Set<Journalisation> journalisation = new HashSet<>();

    /**
     * Sessions actives ou passées de l'utilisateur
     */
    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private Set<Session> session = new HashSet<>();

    /**
     * Décharges associées à l'utilisateur
     */
    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Decharge> decharge = new ArrayList<>();

    /**
     * Affectations de courriers à l'utilisateur
     */
    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Affectation> affectation = new ArrayList<>();

    /**
     * Archives consultées ou créées par l'utilisateur
     */
    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Archive> archive = new ArrayList<>();

    /**
    * Direction administrative de l'utilisateur
    */
    @ManyToOne
    @JoinColumn(name = "id_direction")
    @JsonProperty("direction")
    private Direction direction;
    /**
     * Constructeur sans argument requis par JPA
     */
    public Utilisateur() {}

    /**
     * Incrémente le compteur d'échecs et verrouille le compte si le seuil est atteint
     */
    public void incrementerTentativesEchec() {
        this.tentativesEchec++;
        if (this.tentativesEchec >= MAX_TENTATIVES) {
            this.verrouille = true;
        }
    }

    /**
     * Réinitialise le compteur d'échecs et déverrouille le compte
     */
    public void reinitialiserTentativesEchec() {
        this.tentativesEchec = 0;
        this.verrouille = false;
    }

    // Implémentation de l'interface UserDetails pour Spring Security

    /**
     * Retourne les autorités de l'utilisateur (rôle Spring Security)
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.role != null
        ? List.of(new SimpleGrantedAuthority(this.role.getAuthority()))
        : List.of();
}


    /**
     * Retourne le mot de passe encodé
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return this.motDePasse;
    }

    /**
     * Retourne l'identifiant de connexion (email)
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return this.emailUtilisateur;
    }

    /**
     * Indique si le compte est expiré (toujours actif ici)
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indique si le compte est verrouillé
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !this.verrouille;
    }

    /**
     * Indique si les identifiants sont expirés (toujours valides ici)
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indique si le compte est activé
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.actif;
    }

    // Méthodes utilitaires

    /**
     * Retourne l'état de verrouillage du compte
     */
    public boolean getVerrouille() {
        return this.verrouille;
    }

    /**
     * Retourne l'état d'activation du compte
     */
    public boolean getActif() {
        return this.actif;
    }

    /**
     * Retourne le rôle métier sous forme de code (ex: "ADMIN")
     */
   public String getRoleString() {
    return this.role != null ? this.role.getCode() : "INCONNU";
}


    /**
     * Génère un hashCode basé sur l'identifiant
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idUtilisateur == null) ? 0 : idUtilisateur.hashCode());
        return result;
    }

    /**
     * Compare deux utilisateurs par leur identifiant
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Utilisateur other = (Utilisateur) obj;
        return Objects.equals(idUtilisateur, other.idUtilisateur);
    }

    @Override
    public String toString() {
        return "Utilisateur [idUtilisateur=" + idUtilisateur + ", nomUtilisateur=" + nomUtilisateur
                + ", prenomUtilisateur=" + prenomUtilisateur + ", emailUtilisateur=" + emailUtilisateur
                + ", motDePasse=" + motDePasse + ", role=" + role + ", actif=" + actif + ", verrouille=" + verrouille
                + ", tentativesEchec=" + tentativesEchec + ", MAX_TENTATIVES=" + MAX_TENTATIVES + ", journalisation="
                + journalisation + ", session=" + session + ", decharge=" + decharge + ", affectation=" + affectation
                + ", archive=" + archive + ", direction=" + direction + "]";
    }

    /**
     * Représentation textuelle de l'utilisateur
     */
    
}




/* 
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.affectation.Affectation;
import tg.inseed.gestioncourrier.gestioncourrier.archive.Archive;
import tg.inseed.gestioncourrier.gestioncourrier.decharge.Decharge;
import tg.inseed.gestioncourrier.gestioncourrier.direction.Direction;
import tg.inseed.gestioncourrier.gestioncourrier.journalisation.Journalisation;

import tg.inseed.gestioncourrier.gestioncourrier.session.Session;



/**
 * Classe représentant un utilisateur du système de gestion des courriers
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 * @since 10/2025

@Entity
@Getter
@Setter
@Table(name = "utilisateur")
public class Utilisateur implements UserDetails{

    /**
     * Identifiant unique de l'utilisateur
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    @JsonProperty("id_utilisateur")
    private Long idUtilisateur;

    /**
     * Nom de l'utilisateur
     * Exemple : KENKOU
   
    @Column(name = "nom_utilisateur", nullable = false, length = 50)
    @JsonProperty("nom_utilisateur")
    private String nomUtilisateur;

    /**
     * Prénom de l'utilisateur
     * Exemple : Marê Dave
    
    @Column(name = "prenom_utilisateur", nullable = false, length = 75)
    @JsonProperty("prenom_utilisateur")
    private String prenomUtilisateur;

    /**
     * Adresse email de l'utilisateur ou encore son identifiant de connexion
     * Exemple : mare.christian@inseed.tg
   
    @Column(name = "email_utilisateur", nullable = false, length = 100, unique = true)
    @JsonProperty("email_utilisateur")
    private String emailUtilisateur;

    /**
     * Mot de passe de l'identifiant de connexion de l'utilisateur
     * Exemple : mdp1234
   
    
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    @JsonProperty("mot_de_passe" )
    private String motDePasse;

    /**
     * Rôle de l'utilisateur dans le système
     * Exemple : DG, Agent, Secrétaire
    

         // Changement important : utilisation de l'enum pour le rôle
    @Enumerated(EnumType.STRING)
    @Column(name = "role_utilisateur", nullable = false, length = 30)
    @JsonProperty("role_utilisateur")
    private tg.inseed.gestioncourrier.gestioncourrier.role.RoleUtilisateur role;

     // Relation avec l'entité Role (si vous voulez garder les deux)
    @ManyToOne
    @JoinColumn(name = "id_role")
    @JsonIgnore
    private tg.inseed.gestioncourrier.gestioncourrier.role.Role roleEntity;

     // Champs pour la sécurité
    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "verrouille", nullable = false)
    private boolean verrouille = false;

    @Column(name = "tentatives_echec", nullable = false)
    private int tentativesEchec = 0;

    @Transient
    private final int MAX_TENTATIVES = 3;

     /**
     * Liste des journalisations associées à cet utilisateur
     * Relation OneToMany vers Journalisation

   @OneToMany(mappedBy = "utilisateur")
   @JsonIgnore
   private Set<Journalisation> journalisation = new HashSet<>();

   @OneToMany(mappedBy = "utilisateur")
   @JsonIgnore
   private Set<Session> session = new HashSet<>();
   

   @OneToMany(mappedBy = "utilisateur")
   @JsonIgnore
   private List <Decharge> decharge = new ArrayList<>();

   @OneToMany(mappedBy = "utilisateur")
   @JsonIgnore
   private List <Affectation> affectation = new ArrayList<>();

   @OneToMany(mappedBy = "utilisateur")
   @JsonIgnore
   private List <Archive> archive = new ArrayList<>();

   @OneToMany(mappedBy="utilisateur")
   @JsonIgnore
   private List <Direction> direction = new ArrayList<>();
       
   public Utilisateur() {
    }
    // Méthodes pour la gestion de la sécurité
    public void incrementerTentativesEchec() {
        this.tentativesEchec++;
        if (this.tentativesEchec >= MAX_TENTATIVES) {
            this.verrouille = true;
        }
    }

    public void reinitialiserTentativesEchec() {
        this.tentativesEchec = 0;
        this.verrouille = false;
    }

    // Implémentation des méthodes de UserDetails
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.getAuthority()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.motDePasse;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.emailUtilisateur;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !this.verrouille;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.actif;
    }

    // Getters supplémentaires pour compatibilité
    public boolean getVerrouille() {
        return this.verrouille;
    }

    public boolean getActif() {
        return this.actif;
    }

    // Getter pour le rôle en string (compatibilité)
    public String getRoleString() {
        return this.role.getCode();
    }

    /**
     * Constructeur sans argument requis par JPA
    
    

   

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idUtilisateur == null) ? 0 : idUtilisateur.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Utilisateur other = (Utilisateur) obj;
        if (idUtilisateur == null) {
            if (other.idUtilisateur != null)
                return false;
        } else if (!idUtilisateur.equals(other.idUtilisateur))
            return false;
        return true;
    }

@Override
public String toString() {
    return "Utilisateur [idUtilisateur=" + idUtilisateur + ", nomUtilisateur=" + nomUtilisateur
            + ", prenomUtilisateur=" + prenomUtilisateur + ", emailUtilisateur=" + emailUtilisateur
            + ", motDePasse=****" + ", role=" + role + "]";
}

    
}
*/
