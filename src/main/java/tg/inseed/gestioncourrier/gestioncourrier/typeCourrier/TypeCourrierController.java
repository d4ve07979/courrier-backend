package tg.inseed.gestioncourrier.gestioncourrier.typeCourrier;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les types de courrier.
 * VERSION MIGRÉE - Compatible avec anciens ET nouveaux endpoints
 * 
 * @author KENKOU Marê Dave Christian
 * @version 2.0
 * @since 10/2025
 */
@RestController
@RequestMapping("/api/typecourriers")
@RequiredArgsConstructor
@Slf4j
public class TypeCourrierController {

    private final TypeCourrierService typeCourrierService;

    // ============================================================
    // ANCIENS ENDPOINTS (compatibilité backward)
    // ============================================================
    
    /**
     * POST /api/typecourriers/ajouter
     * @deprecated Utilisez POST /api/types-courriers
     */
    @PostMapping("/ajouter")
    public ResponseEntity<TypeCourrier> createTypeCourrierOld(@RequestBody TypeCourrier typeCourrier) {
        log.info("API (ancien): Création d'un type de courrier - {}", typeCourrier.getLibelle());
        TypeCourrier created = typeCourrierService.createTypeCourrier(typeCourrier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/typecourriers/list
     * @deprecated Utilisez GET /api/types-courriers
     */
    @GetMapping("/list")
    public ResponseEntity<List<TypeCourrier>> getAllTypeCourrierOld() {
        log.debug("API (ancien): Récupération de tous les types");
        List<TypeCourrier> types = typeCourrierService.getAllTypeCourrierEntities();
        return ResponseEntity.ok(types);
    }

    /**
     * PUT /api/typecourriers/update/{id}
     * @deprecated Utilisez PUT /api/types-courriers/{id}
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<TypeCourrier> updateTypeCourrierOld(
            @PathVariable Long id, 
            @RequestBody TypeCourrier typeCourrier) {
        log.info("API (ancien): Mise à jour du type - {}", id);
        TypeCourrier updated = typeCourrierService.updateTypeCourrier(id, typeCourrier);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/typecourriers/delete/{id}
     * @deprecated Utilisez DELETE /api/types-courriers/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTypeCourrierOld(@PathVariable Long id) {
        log.info("API (ancien): Suppression du type - {}", id);
        typeCourrierService.deleteTypeCourrier(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // NOUVEAUX ENDPOINTS (versions améliorées avec DTOs)
    // ============================================================
    
    /**
     * POST /api/typecourriers (nouveau)
     * Crée un nouveau type de courrier
     */
    @PostMapping
    public ResponseEntity<TypeCourrierDTO> createTypeCourrier(
            @Valid @RequestBody TypeCourrierDTO dto) {
        log.info("API: Création d'un type de courrier - {}", dto.libelle());
        TypeCourrierDTO created = typeCourrierService.createTypeCourrier(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/typecourriers (nouveau)
     * Récupère tous les types de courrier
     */
    @GetMapping
    public ResponseEntity<List<TypeCourrierDTO>> getAllTypeCourriers(
            @RequestParam(required = false, defaultValue = "false") boolean actifsOnly) {
        log.debug("API: Récupération de tous les types (actifs only: {})", actifsOnly);
        
        List<TypeCourrierDTO> types = actifsOnly 
            ? typeCourrierService.getActiveTypeCourriers()
            : typeCourrierService.getAllTypeCourriers();
            
        return ResponseEntity.ok(types);
    }

    /**
     * GET /api/typecourriers/{id}
     * Compatible avec ancien ET nouveau
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeCourrierDTO> getTypeCourrierById(@PathVariable Long id) {
        log.debug("API: Récupération du type - {}", id);
        TypeCourrierDTO type = typeCourrierService.getTypeCourrierById(id);
        return ResponseEntity.ok(type);
    }

    /**
     * PUT /api/typecourriers/{id} (nouveau)
     * Met à jour un type de courrier
     */
    @PutMapping("/{id}")
    public ResponseEntity<TypeCourrierDTO> updateTypeCourrier(
            @PathVariable Long id,
            @Valid @RequestBody TypeCourrierDTO dto) {
        log.info("API: Mise à jour du type - {}", id);
        TypeCourrierDTO updated = typeCourrierService.updateTypeCourrier(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/typecourriers/{id}/desactiver (nouveau)
     * Désactive un type de courrier
     */
    @PatchMapping("/{id}/desactiver")
    public ResponseEntity<Void> deactivateTypeCourrier(@PathVariable Long id) {
        log.info("API: Désactivation du type - {}", id);
        typeCourrierService.deactivateTypeCourrier(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/typecourriers/{id} (nouveau)
     * Supprime définitivement un type
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypeCourrier(@PathVariable Long id) {
        log.info("API: Suppression du type - {}", id);
        typeCourrierService.deleteTypeCourrier(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/typecourriers/recherche (nouveau)
     * Recherche par mot-clé
     */
    @GetMapping("/recherche")
    public ResponseEntity<List<TypeCourrierDTO>> searchTypeCourriers(
            @RequestParam(name = "q") String keyword) {
        log.debug("API: Recherche - {}", keyword);
        List<TypeCourrierDTO> results = typeCourrierService.searchTypeCourriers(keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/typecourriers/statistiques (nouveau)
     * Statistiques des types
     */
    @GetMapping("/statistiques")
    public ResponseEntity<List<TypeCourrierStatsDTO>> getStatistiques() {
        log.debug("API: Récupération des statistiques");
        List<TypeCourrierStatsDTO> stats = typeCourrierService.getStatistiques();
        return ResponseEntity.ok(stats);
    }
}