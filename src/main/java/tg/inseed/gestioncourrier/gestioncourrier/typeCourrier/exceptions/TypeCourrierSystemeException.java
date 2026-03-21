package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions;

/**
 * Exception levée lors d'une tentative d'opération interdite sur un type système
 */
public class TypeCourrierSystemeException extends RuntimeException {
    public TypeCourrierSystemeException(String operation) {
        super("Impossible de " + operation + " un type de courrier système");
    }
}