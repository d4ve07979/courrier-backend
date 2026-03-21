package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;

/**
 * Classe représentant un type de courrier dans le système.
 * Permet de catégoriser les courriers selon leur nature ou leur usage.
 * 
 * Exemples : Courrier entrant, Courrier sortant, Note interne, Note de service
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 * @since 10/2025
 */
@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "type_courrier")
public class TypeCourrier {

    /**
     * Identifiant unique du type de courrier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_courrier")
    @JsonProperty("id_type_courrier")
    private Long idType;

    /**
     * Libellé du type de courrier
     * Exemple : Courrier entrant
     */
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    @Column(name = "libelle", nullable = false, length = 100, unique = true)
    @JsonProperty("libelle")
    private String libelle;

    /**
     * Description du type de courrier
     */
    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    @Column(name = "description", length = 255)
    @JsonProperty("description")
    private String description;

    /**
     * Code unique pour identifier le type (ex: ENT, SOR, NI, NS)
     */
    @Column(name = "code", length = 10, unique = true)
    @JsonProperty("code")
    private String code;

    /**
     * Indique si le type est un type système (non supprimable)
     * Les types "Courrier entrant" et "Courrier sortant" sont des types système
     */
    @Column(name = "type_systeme", nullable = false)
    @JsonProperty("type_systeme")
    private Boolean typeSysteme = false;

    /**
     * Indique si le type est actif
     */
    @Column(name = "actif", nullable = false)
    @JsonProperty("actif")
    private Boolean actif = true;

    /**
     * Date de création du type de courrier
     */
    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    @JsonProperty("date_creation")
    private LocalDateTime dateCreation;

    /**
     * Date de dernière modification
     */
    @LastModifiedDate
    @Column(name = "date_modification")
    @JsonProperty("date_modification")
    private LocalDateTime dateModification;

    /**
     * Liste des courriers associés à ce type
     */
    @OneToMany(mappedBy = "typeCourrier")
    @JsonIgnore
    private List<Courrier> courriers = new ArrayList<>();

    /**
     * Constructeur sans argument requis par JPA
     */
    public TypeCourrier() {}

    /**
     * Constructeur avec paramètres principaux
     */
    public TypeCourrier(String libelle, String description, String code, Boolean typeSysteme) {
        this.libelle = libelle;
        this.description = description;
        this.code = code;
        this.typeSysteme = typeSysteme;
        this.actif = true;
    }

    /**
     * Retourne le nombre de courriers associés à ce type
     */
    @JsonProperty("nombre_courriers")
    public int getNombreCourriers() {
        return courriers != null ? courriers.size() : 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idType == null) ? 0 : idType.hashCode());
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
        TypeCourrier other = (TypeCourrier) obj;
        if (idType == null) {
            if (other.idType != null)
                return false;
        } else if (!idType.equals(other.idType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TypeCourrier [idType=" + idType + ", libelle=" + libelle + 
               ", code=" + code + ", description=" + description + 
               ", actif=" + actif + ", typeSysteme=" + typeSysteme + "]";
    }
}