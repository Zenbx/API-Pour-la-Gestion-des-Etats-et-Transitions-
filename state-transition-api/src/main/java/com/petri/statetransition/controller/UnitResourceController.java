package com.petri.statetransition.controller;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import com.petri.statetransition.service.UnitResourceService;
import com.petri.statetransition.service.CompositeResourceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Contrôleur REST pour la gestion des ressources unitaires
 */
@RestController
@RequestMapping("/api/v1/unit-resources")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UnitResourceController {

    private static final Logger logger = LoggerFactory.getLogger(UnitResourceController.class);

    private final UnitResourceService unitResourceService;

    public UnitResourceController(UnitResourceService unitResourceService) {
        this.unitResourceService = unitResourceService;
    }

    /**
     * Crée une nouvelle ressource unitaire
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> createUnitResource(
            @Valid @RequestBody CreateUnitResourceDTO createUnitResourceDTO) {
        logger.info("Demande de création d'une ressource unitaire: {}", createUnitResourceDTO.name());

        return unitResourceService.createUnitResource(createUnitResourceDTO)
                .map(resourceDTO -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Ressource unitaire créée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la création de la ressource unitaire")));
    }

    /**
     * Met à jour une ressource unitaire existante
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> updateUnitResource(
            @PathVariable Long id,
            @Valid @RequestBody CreateUnitResourceDTO updateDTO) {
        logger.info("Demande de mise à jour de la ressource unitaire ID: {}", id);

        return unitResourceService.updateUnitResource(id, updateDTO)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource unitaire mise à jour avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la mise à jour de la ressource unitaire")));
    }

    /**
     * Récupère une ressource unitaire par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> getUnitResourceById(@PathVariable Long id) {
        logger.debug("Demande de récupération de la ressource unitaire ID: {}", id);

        return unitResourceService.findById(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success(resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Ressource unitaire non trouvée")));
    }

    /**
     * Récupère toutes les ressources unitaires
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<UnitResourceDTO>>>> getAllUnitResources() {
        logger.debug("Demande de récupération de toutes les ressources unitaires");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(unitResourceService.findAll())));
    }

    /**
     * Récupère les ressources unitaires par état
     */
    @GetMapping("/state/{state}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<UnitResourceDTO>>>> getUnitResourcesByState(@PathVariable UnitResourceState state) {
        logger.debug("Demande de récupération des ressources unitaires dans l'état: {}", state);

        return Mono.just(ResponseEntity.ok(ApiResponse.success(unitResourceService.findByState(state))));
    }

    /**
     * Récupère les ressources unitaires disponibles
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<UnitResourceDTO>>>> getAvailableUnitResources() {
        logger.debug("Demande de récupération des ressources unitaires disponibles");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(unitResourceService.findAvailableResources())));
    }

    /**
     * Alloue une ressource unitaire (LIBRE -> AFFECTÉ)
     */
    @PostMapping("/{id}/allocate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> allocateUnitResource(@PathVariable Long id) {
        logger.info("Demande d'allocation de la ressource unitaire ID: {}", id);

        return unitResourceService.allocateResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource unitaire allouée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de l'allocation de la ressource unitaire")));
    }

    /**
     * Utilise une ressource unitaire (AFFECTÉ -> OCCUPÉ)
     */
    @PostMapping("/{id}/use")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> useUnitResource(@PathVariable Long id) {
        logger.info("Demande d'utilisation de la ressource unitaire ID: {}", id);

        return unitResourceService.useResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource unitaire utilisée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de l'utilisation de la ressource unitaire")));
    }

    /**
     * Libère une ressource unitaire (OCCUPÉ/AFFECTÉ -> LIBRE)
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<UnitResourceDTO>>> releaseUnitResource(@PathVariable Long id) {
        logger.info("Demande de libération de la ressource unitaire ID: {}", id);

        return unitResourceService.releaseResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource unitaire libérée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la libération de la ressource unitaire")));
    }

    /**
     * Supprime une ressource unitaire
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUnitResource(@PathVariable Long id) {
        logger.info("Demande de suppression de la ressource unitaire ID: {}", id);

        return unitResourceService.deleteUnitResource(id)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Ressource unitaire supprimée avec succès", null))))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la suppression de la ressource unitaire")));
    }
}

