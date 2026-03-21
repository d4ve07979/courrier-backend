package tg.inseed.gestioncourrier.gestioncourrier.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.DuplicateTypeCourrierException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierInUseException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierNotFoundException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierSystemeException;

/**
 * Gestionnaire global des exceptions pour l'application.
 * Capture et formate toutes les exceptions levées par les contrôleurs.
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Structure de réponse d'erreur standardisée
     */
    record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
    ) {
        public ErrorResponse(HttpStatus status, String message, String path) {
            this(LocalDateTime.now(), status.value(), status.getReasonPhrase(), 
                 message, path, null);
        }

        public ErrorResponse(HttpStatus status, String message, String path, 
                           Map<String, String> details) {
            this(LocalDateTime.now(), status.value(), status.getReasonPhrase(), 
                 message, path, details);
        }
    }

    /**
     * Gère les exceptions de type TypeCourrierNotFoundException
     */
    @ExceptionHandler(TypeCourrierNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTypeCourrierNotFound(
            TypeCourrierNotFoundException ex, WebRequest request) {
        log.error("Type de courrier non trouvé: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Gère les exceptions de duplication
     */
    @ExceptionHandler(DuplicateTypeCourrierException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTypeCourrier(
            DuplicateTypeCourrierException ex, WebRequest request) {
        log.error("Tentative de création d'un doublon: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gère les exceptions liées aux types système
     */
    @ExceptionHandler(TypeCourrierSystemeException.class)
    public ResponseEntity<ErrorResponse> handleTypeCourrierSysteme(
            TypeCourrierSystemeException ex, WebRequest request) {
        log.error("Opération interdite sur type système: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Gère les exceptions quand un type est utilisé
     */
    @ExceptionHandler(TypeCourrierInUseException.class)
    public ResponseEntity<ErrorResponse> handleTypeCourrierInUse(
            TypeCourrierInUseException ex, WebRequest request) {
        log.error("Tentative de suppression d'un type en cours d'utilisation: {}", 
                  ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gère les erreurs de validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Erreur de validation: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Erreur de validation des données",
            request.getDescription(false).replace("uri=", ""),
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gère toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Erreur inattendue: ", ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur interne est survenue: " + ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}