package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe d'initialisation des types de courrier par défaut.
 * S'exécute au démarrage de l'application pour créer les types système essentiels.
 * 
 * @author KENKOU Marê Dave Christian
 * @version 1.0
 */
@Component
@Order(1) // S'exécute en premier
@RequiredArgsConstructor
@Slf4j
public class TypeCourrierInitializer implements CommandLineRunner {
    
    private final TypeCourrierRepository typeCourrierRepository;

    @Override
    public void run(String... args) {
        log.info("Vérification et initialisation des types de courrier...");
        
        if (typeCourrierRepository.count() == 0) {
            log.info("Aucun type de courrier trouvé. Création des types par défaut...");
            initializeDefaultTypes();
        } else {
            log.info("Types de courrier déjà existants: {}", typeCourrierRepository.count());
            // Vérifier que les types système essentiels existent
            ensureSystemTypes();
        }
    }

    /**
     * Initialise tous les types de courrier par défaut
     */
    private void initializeDefaultTypes() {
        List<TypeCourrier> defaultTypes = Arrays.asList(
            // Types système (non supprimables)
            createType(
                "Courrier entrant",
                "Courrier reçu de l'extérieur (administrations, partenaires, particuliers)",
                "ENT",
                true
            ),
            createType(
                "Courrier sortant",
                "Courrier envoyé vers l'extérieur",
                "SOR",
                true
            ),
            
            // Types standards (modifiables/supprimables)
            createType(
                "Note interne",
                "Communication interne entre services ou directions",
                "NI",
                false
            ),
            createType(
                "Note de service",
                "Note administrative ou organisationnelle",
                "NS",
                false
            ),
            createType(
                "Circulaire",
                "Document d'information ou d'instruction général",
                "CIR",
                false
            ),
            createType(
                "Lettre officielle",
                "Correspondance formelle officielle",
                "LO",
                false
            ),
            createType(
                "Rapport",
                "Document de synthèse ou d'analyse",
                "RAP",
                false
            ),
            createType(
                "Demande",
                "Demande administrative (congé, matériel, etc.)",
                "DEM",
                false
            ),
            createType(
                "Convocation",
                "Convocation à une réunion ou événement",
                "CONV",
                false
            ),
            createType(
                "Compte rendu",
                "Compte rendu de réunion ou mission",
                "CR",
                false
            )
        );

        List<TypeCourrier> saved = typeCourrierRepository.saveAll(defaultTypes);
        log.info("✅ {} types de courrier créés avec succès", saved.size());
        
        saved.forEach(type -> 
            log.debug("  - {} ({}) [Système: {}]", 
                type.getLibelle(), 
                type.getCode(), 
                type.getTypeSysteme())
        );
    }

    /**
     * S'assure que les types système essentiels existent
     */
    private void ensureSystemTypes() {
        ensureTypeExists("Courrier entrant", "ENT", 
            "Courrier reçu de l'extérieur", true);
        ensureTypeExists("Courrier sortant", "SOR", 
            "Courrier envoyé vers l'extérieur", true);
    }

    /**
     * Vérifie qu'un type existe, sinon le crée
     */
    private void ensureTypeExists(String libelle, String code, String description, boolean systeme) {
        if (!typeCourrierRepository.existsByCode(code)) {
            TypeCourrier type = createType(libelle, description, code, systeme);
            typeCourrierRepository.save(type);
            log.info("✅ Type système créé: {} ({})", libelle, code);
        }
    }

    /**
     * Crée une instance de TypeCourrier
     */
    private TypeCourrier createType(String libelle, String description, 
                                    String code, boolean systeme) {
        TypeCourrier type = new TypeCourrier();
        type.setLibelle(libelle);
        type.setDescription(description);
        type.setCode(code);
        type.setTypeSysteme(systeme);
        type.setActif(true);
        return type;
    }
}