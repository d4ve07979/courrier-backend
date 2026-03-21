package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour les opérations sur les types de courrier
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 */
public record TypeCourrierDTO(
    @JsonProperty("id_type_courrier")
    Long idType,
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    @JsonProperty("libelle")
    String libelle,
    
    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    @JsonProperty("description")
    String description,
    
    @JsonProperty("code")
    String code,
    
    @JsonProperty("type_systeme")
    Boolean typeSysteme,
    
    @JsonProperty("actif")
    Boolean actif,
    
    @JsonProperty("nombre_courriers")
    Long nombreCourriers,
    
    @JsonProperty("date_creation")
    LocalDateTime dateCreation,
    
    @JsonProperty("date_modification")
    LocalDateTime dateModification
) {
    /**
     * Constructeur simplifié pour la création
     */
    public TypeCourrierDTO(String libelle, String description, String code) {
        this(null, libelle, description, code, false, true, 0L, null, null);
    }

    /**
     * Conversion d'une entité vers un DTO
     */
    public static TypeCourrierDTO fromEntity(TypeCourrier entity) {
        return new TypeCourrierDTO(
            entity.getIdType(),
            entity.getLibelle(),
            entity.getDescription(),
            entity.getCode(),
            entity.getTypeSysteme(),
            entity.getActif(),
            (long) entity.getNombreCourriers(),
            entity.getDateCreation(),
            entity.getDateModification()
        );
    }

    /**
     * Conversion du DTO vers une entité
     */
    public TypeCourrier toEntity() {
        TypeCourrier entity = new TypeCourrier();
        entity.setIdType(this.idType);
        entity.setLibelle(this.libelle);
        entity.setDescription(this.description);
        entity.setCode(this.code);
        entity.setTypeSysteme(this.typeSysteme != null ? this.typeSysteme : false);
        entity.setActif(this.actif != null ? this.actif : true);
        return entity;
    }
}
/**
 * DTO pour les statistiques par type de courrier
 */
record TypeCourrierStatsDTO(
    @JsonProperty("id_type_courrier")
    Long idType,
    
    @JsonProperty("libelle")
    String libelle,
    
    @JsonProperty("code")
    String code,
    
    @JsonProperty("nombre_courriers")
    Long nombreCourriers,
    
    @JsonProperty("pourcentage")
    Double pourcentage
) {}