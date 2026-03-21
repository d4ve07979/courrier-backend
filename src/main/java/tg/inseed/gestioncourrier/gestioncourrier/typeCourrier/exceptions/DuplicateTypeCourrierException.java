package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions;

/**
 * Exception levée lors d'une tentative de création d'un type déjà existant
 */
public class DuplicateTypeCourrierException extends RuntimeException {
    public DuplicateTypeCourrierException(String libelle) {
        super("Un type de courrier avec le libellé '" + libelle + "' existe déjà");
    }
}