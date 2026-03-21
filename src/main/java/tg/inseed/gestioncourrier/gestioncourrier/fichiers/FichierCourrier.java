package tg.inseed.gestioncourrier.gestioncourrier.fichiers;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import tg.inseed.gestioncourrier.gestioncourrier.courriers.Courrier;

/**
 * Entité représentant un fichier associé à un courrier.
 * Chaque fichier est lié à un courrier via une relation ManyToOne.
 * Les fichiers sont stockés physiquement dans le dossier /uploads/courriers/{id}
 * et référencés en base via leur chemin absolu.
 *
 * Exemple : document.pdf lié au courrier #42
 *
 * @author KENKOU
 * @version 1.0
 * @since 10/2025
 */
@Entity
@Getter
@Setter
@Table(name = "fichier_courrier")
public class FichierCourrier {

    /**
     * Identifiant unique du fichier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom original du fichier uploadé
     * Exemple : "document.pdf"
     */
    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    /**
     * Chemin absolu du fichier sur le serveur
     * Exemple : "/uploads/courriers/42/document.pdf"
     */
    @Column(name = "chemin_fichier", nullable = false)
    private String cheminFichier;

    /**
     * Référence au courrier auquel ce fichier est lié
     */
    @ManyToOne
    @JoinColumn(name = "id_courrier", nullable = false)
    @JsonBackReference("courrier-fichier")
    private Courrier courrier;

    /**
     * Constructeur sans argument requis par JPA
     */
    public FichierCourrier() {}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        FichierCourrier other = (FichierCourrier) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FichierCourrier [id=" + id + ", nomFichier=" + nomFichier + ", cheminFichier=" + cheminFichier
                + ", courrier=" + courrier + "]";
    }

    
}