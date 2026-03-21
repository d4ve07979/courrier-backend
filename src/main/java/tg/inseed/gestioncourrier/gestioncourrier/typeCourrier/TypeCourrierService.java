package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.DuplicateTypeCourrierException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierInUseException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierNotFoundException;
import tg.inseed.gestioncourrier.gestioncourrier.typeCourrier.exceptions.TypeCourrierSystemeException;

/**
 * Service permettant la gestion des opérations CRUD sur les types de courrier.
 * VERSION MIGRÉE - Compatible avec l'ancien code
 * 
 * @author KENKOU Marê Dave Christian
 * @version 2.0
 * @since 10/2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TypeCourrierService {

    private final TypeCourrierRepository typeCourrierRepository;

    // ============================================================
    // ANCIENNES MÉTHODES (compatibilité avec votre ancien code)
    // ============================================================
    
    /**
     * @deprecated Utilisez createTypeCourrier(TypeCourrierDTO) pour la nouvelle version
     */
    @Deprecated
    public TypeCourrier createTypeCourrier(TypeCourrier typeCourrier) {
        log.info("Création d'un type de courrier (ancienne méthode): {}", typeCourrier.getLibelle());
        
        // Vérification des doublons
        if (typeCourrierRepository.existsByLibelleIgnoreCase(typeCourrier.getLibelle())) {
            throw new DuplicateTypeCourrierException(typeCourrier.getLibelle());
        }
        
        return typeCourrierRepository.save(typeCourrier);
    }

    /**
     * @deprecated Utilisez getAllTypeCourriers() qui retourne des DTOs
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<TypeCourrier> getAllTypeCourrierEntities() {
        log.debug("Récupération de tous les types (entités)");
        return typeCourrierRepository.findAll();
    }

    /**
     * @deprecated Utilisez getTypeCourrierById(Long) qui retourne un DTO
     */
    @Deprecated
    @Transactional(readOnly = true)
    public TypeCourrier getTypeCourrierEntityById(Long id) {
        return typeCourrierRepository.findById(id)
            .orElseThrow(() -> new TypeCourrierNotFoundException(id));
    }

    /**
     * @deprecated Utilisez updateTypeCourrier(Long, TypeCourrierDTO)
     */
    @Deprecated
    public TypeCourrier updateTypeCourrier(Long id, TypeCourrier newTypeCourrier) {
        log.info("Mise à jour du type (ancienne méthode): {}", id);
        
        TypeCourrier typeCourrier = getTypeCourrierEntityById(id);
        
        // Vérifier les doublons
        if (!typeCourrier.getLibelle().equalsIgnoreCase(newTypeCourrier.getLibelle()) &&
            typeCourrierRepository.existsByLibelleIgnoreCaseAndIdTypeNot(newTypeCourrier.getLibelle(), id)) {
            throw new DuplicateTypeCourrierException(newTypeCourrier.getLibelle());
        }
        
        typeCourrier.setLibelle(newTypeCourrier.getLibelle());
        typeCourrier.setDescription(newTypeCourrier.getDescription());
        
        return typeCourrierRepository.save(typeCourrier);
    }

    // ============================================================
    // NOUVELLES MÉTHODES (avec DTOs et protections avancées)
    // ============================================================
    
    /**
     * Crée un nouveau type de courrier (nouvelle version avec DTO)
     */
    public TypeCourrierDTO createTypeCourrier(TypeCourrierDTO dto) {
        log.info("Création d'un nouveau type de courrier: {}", dto.libelle());
        
        // Vérification des doublons par libellé
        if (typeCourrierRepository.existsByLibelleIgnoreCase(dto.libelle())) {
            throw new DuplicateTypeCourrierException(dto.libelle());
        }
        
        // Vérification des doublons par code (si fourni)
        if (dto.code() != null && !dto.code().isBlank() && 
            typeCourrierRepository.existsByCode(dto.code())) {
            throw new DuplicateTypeCourrierException("Un type avec le code '" + dto.code() + "' existe déjà");
        }
        
        TypeCourrier typeCourrier = dto.toEntity();
        TypeCourrier saved = typeCourrierRepository.save(typeCourrier);
        
        log.info("Type de courrier créé avec succès: id={}", saved.getIdType());
        return TypeCourrierDTO.fromEntity(saved);
    }

    /**
     * Récupère tous les types de courrier (nouvelle version avec DTOs)
     */
    @Transactional(readOnly = true)
    public List<TypeCourrierDTO> getAllTypeCourriers() {
        log.debug("Récupération de tous les types de courrier");
        return typeCourrierRepository.findAll()
                .stream()
                .map(TypeCourrierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupère uniquement les types actifs
     */
    @Transactional(readOnly = true)
    public List<TypeCourrierDTO> getActiveTypeCourriers() {
        log.debug("Récupération des types de courrier actifs");
        return typeCourrierRepository.findByActifTrue()
                .stream()
                .map(TypeCourrierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un type de courrier par son ID (nouvelle version avec DTO)
     */
    @Transactional(readOnly = true)
    public TypeCourrierDTO getTypeCourrierById(Long id) {
        log.debug("Récupération du type de courrier avec l'id: {}", id);
        TypeCourrier typeCourrier = findTypeCourrierOrThrow(id);
        return TypeCourrierDTO.fromEntity(typeCourrier);
    }

    /**
     * Met à jour un type de courrier existant (nouvelle version avec DTO)
     */
    public TypeCourrierDTO updateTypeCourrier(Long id, TypeCourrierDTO dto) {
        log.info("Mise à jour du type de courrier avec l'id: {}", id);
        
        TypeCourrier typeCourrier = findTypeCourrierOrThrow(id);
        
        // Protection des types système
        if (Boolean.TRUE.equals(typeCourrier.getTypeSysteme()) && 
            Boolean.FALSE.equals(dto.typeSysteme())) {
            throw new TypeCourrierSystemeException("modifier le statut système de");
        }
        
        // Vérification des doublons pour le libellé
        if (!typeCourrier.getLibelle().equalsIgnoreCase(dto.libelle()) &&
            typeCourrierRepository.existsByLibelleIgnoreCaseAndIdTypeNot(dto.libelle(), id)) {
            throw new DuplicateTypeCourrierException(dto.libelle());
        }
        
        // Vérification des doublons pour le code
        if (dto.code() != null && !dto.code().isBlank() &&
            !dto.code().equals(typeCourrier.getCode()) &&
            typeCourrierRepository.existsByCodeAndIdTypeNot(dto.code(), id)) {
            throw new DuplicateTypeCourrierException("Un type avec le code '" + dto.code() + "' existe déjà");
        }
        
        // Mise à jour des champs
        typeCourrier.setLibelle(dto.libelle());
        typeCourrier.setDescription(dto.description());
        typeCourrier.setCode(dto.code());
        
        if (dto.actif() != null) {
            typeCourrier.setActif(dto.actif());
        }
        
        TypeCourrier updated = typeCourrierRepository.save(typeCourrier);
        log.info("Type de courrier mis à jour avec succès: id={}", id);
        
        return TypeCourrierDTO.fromEntity(updated);
    }

    /**
     * Désactive un type de courrier (soft delete)
     */
    public void deactivateTypeCourrier(Long id) {
        log.info("Désactivation du type de courrier avec l'id: {}", id);
        
        TypeCourrier typeCourrier = findTypeCourrierOrThrow(id);
        
        if (Boolean.TRUE.equals(typeCourrier.getTypeSysteme())) {
            throw new TypeCourrierSystemeException("désactiver");
        }
        
        typeCourrier.setActif(false);
        typeCourrierRepository.save(typeCourrier);
        
        log.info("Type de courrier désactivé avec succès: id={}", id);
    }

    /**
     * Supprime définitivement un type de courrier
     */
    public void deleteTypeCourrier(Long id) {
        log.info("Suppression du type de courrier avec l'id: {}", id);
        
        TypeCourrier typeCourrier = typeCourrierRepository.findByIdWithCourriers(id)
                .orElseThrow(() -> new TypeCourrierNotFoundException(id));
        
        // Protection des types système
        if (Boolean.TRUE.equals(typeCourrier.getTypeSysteme())) {
            throw new TypeCourrierSystemeException("supprimer");
        }
        
        // Vérification de l'utilisation
        long nombreCourriers = typeCourrier.getCourriers().size();
        if (nombreCourriers > 0) {
            throw new TypeCourrierInUseException(id, nombreCourriers);
        }
        
        typeCourrierRepository.deleteById(id);
        log.info("Type de courrier supprimé avec succès: id={}", id);
    }

    /**
     * Recherche des types de courrier par mot-clé
     */
    @Transactional(readOnly = true)
    public List<TypeCourrierDTO> searchTypeCourriers(String keyword) {
        log.debug("Recherche de types de courrier avec le mot-clé: {}", keyword);
        return typeCourrierRepository.searchByLibelle(keyword)
                .stream()
                .map(TypeCourrierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques des types de courrier
     */
    @Transactional(readOnly = true)
    public List<TypeCourrierStatsDTO> getStatistiques() {
        log.debug("Récupération des statistiques des types de courrier");
        
        List<TypeCourrierRepository.TypeCourrierStats> stats = typeCourrierRepository.getStatistiques();
        
        // Calcul du total pour les pourcentages
        long total = stats.stream()
                .mapToLong(TypeCourrierRepository.TypeCourrierStats::getNombreCourriers)
                .sum();
        
        return stats.stream()
                .map(s -> new TypeCourrierStatsDTO(
                    s.getIdType(),
                    s.getLibelle(),
                    s.getCode(),
                    s.getNombreCourriers(),
                    total > 0 ? (s.getNombreCourriers() * 100.0) / total : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Méthode utilitaire pour récupérer un type ou lever une exception
     */
    private TypeCourrier findTypeCourrierOrThrow(Long id) {
        return typeCourrierRepository.findById(id)
                .orElseThrow(() -> new TypeCourrierNotFoundException(id));
    }
}