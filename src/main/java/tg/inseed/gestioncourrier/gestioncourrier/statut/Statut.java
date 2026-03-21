package tg.inseed.gestioncourrier.gestioncourrier.statut;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;

/**
 * Classe représentant un statut dans le système de gestion du courrier.
 *
 * <p>Un statut définit l’état d’un courrier (exemple : "En attente",
 * "Validé", "Archivé"). Il est utilisé pour suivre le cycle de vie
 * des courriers et leur affichage dans l’interface utilisateur.</p>
 *
 * <p>Chaque statut possède un code unique, un libellé lisible,
 * une couleur et une icône pour l’affichage, ainsi que des
 * propriétés indiquant s’il est actif ou final.</p>
 *
 * @author KENKOU
 * @version 1.0
 * @since 12/2025
 */
@Entity
@Getter
@Setter
@Table(name = "statut")
public class Statut {
    
    /**
     * Identifiant unique du statut.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    @JsonProperty("id_statut")
    private Long idStatut;

    /**
     * Libellé du statut (exemple : "En attente", "Validé").
     * Doit être unique.
     */
    @Column(name = "libelle_statut", nullable = false, length = 50, unique = true)
    @JsonProperty("libelle_statut")
    private String libelleStatut;

    /**
     * Code du statut (utilisé pour l’identification programmatique).
     * Exemple : "EN_ATTENTE", "VALIDE".
     */
    @Column(name = "code_statut", nullable = false, length = 30, unique = true)
    @JsonProperty("code_statut")
    private String codeStatut;

    /**
     * Description détaillée du statut.
     */
    @Column(name = "description", length = 255)
    @JsonProperty("description")
    private String description;

    /**
     * Couleur associée au statut pour l’affichage.
     * Exemple : "#28a745" pour vert.
     */
    @Column(name = "couleur", length = 7)
    @JsonProperty("couleur")
    private String couleur;

    /**
     * Icône associée au statut pour l’affichage.
     * Exemple : "check-circle", "clock".
     */
    @Column(name = "icone", length = 50)
    @JsonProperty("icone")
    private String icone;

    /**
     * Ordre d’affichage du statut dans les listes.
     */
    @Column(name = "ordre")
    @JsonProperty("ordre")
    private Integer ordre;

    /**
     * Indique si le statut est final.
     * Un statut final signifie qu’aucune transition n’est possible après.
     */
    @Column(name = "statut_final")
    @JsonProperty("statut_final")
    private boolean statutFinal = false;

    /**
     * Indique si le statut est actif.
     * Un statut inactif ne peut plus être utilisé.
     */
    @Column(name = "actif")
    @JsonProperty("actif")
    private boolean actif = true;

    /**
     * Liste des courriers associés à ce statut.
     * Relation OneToMany : un statut peut être lié à plusieurs courriers.
     */
    @OneToMany(mappedBy = "statut")
    @JsonIgnore
    private List<Courrier> courriers = new ArrayList<>();

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Statut() {}

    /**
     * Constructeur pratique pour créer un statut avec ses principales propriétés.
     *
     * @param codeStatut code unique du statut
     * @param libelleStatut libellé lisible du statut
     * @param description description du statut
     * @param couleur couleur associée
     * @param ordre ordre d’affichage
     */
    public Statut(String codeStatut, String libelleStatut, String description, String couleur, Integer ordre) {
        this.codeStatut = codeStatut;
        this.libelleStatut = libelleStatut;
        this.description = description;
        this.couleur = couleur;
        this.ordre = ordre;
        this.actif = true;
    }

    /**
     * Génère un hash basé sur l’identifiant unique du statut.
     *
     * @return valeur de hashCode
     */
    @Override
    public int hashCode() {
        return idStatut != null ? idStatut.hashCode() : 0;
    }

    /**
     * Vérifie l’égalité entre deux objets Statut.
     * Deux statuts sont égaux s’ils possèdent le même identifiant.
     *
     * @param obj objet à comparer
     * @return true si les statuts sont identiques, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Statut other = (Statut) obj;
        return idStatut != null && idStatut.equals(other.idStatut);
    }

    /**
     * Retourne une représentation textuelle du statut.
     * Utile pour le débogage et les logs.
     *
     * @return chaîne descriptive du statut
     */
    @Override
    public String toString() {
        return "Statut [idStatut=" + idStatut + 
               ", codeStatut=" + codeStatut + 
               ", libelleStatut=" + libelleStatut + "]";
    }
}
