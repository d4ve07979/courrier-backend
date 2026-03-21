package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions;

/**
 * Exception levée lors d'une tentative de suppression d'un type utilisé
 */
public class TypeCourrierInUseException extends RuntimeException {
    public TypeCourrierInUseException(Long id, long nombreCourriers) {
        super("Impossible de supprimer le type de courrier (id: " + id + 
              "). Il est utilisé par " + nombreCourriers + " courrier(s)");
    }
}
