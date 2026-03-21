package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions;

/**
 * Exception levée lorsqu'un type de courrier n'est pas trouvé
 */
public class TypeCourrierNotFoundException extends RuntimeException {
    public TypeCourrierNotFoundException(Long id) {
        super("Type de courrier introuvable avec l'id: " + id);
    }
    
    public TypeCourrierNotFoundException(String message) {
        super(message);
    }
}
