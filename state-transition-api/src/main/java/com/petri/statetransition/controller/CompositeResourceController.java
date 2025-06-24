package com.petri.statetransition.controller;

import com.petri.statetransition.dto.ApiResponse;
import com.petri.statetransition.dto.CompositeResourceDTO;
import com.petri.statetransition.dto.CreateCompositeResourceDTO;
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
 * Contrôleur REST pour la gestion des ressources composites
 */
@RestController
@RequestMapping("/api/v1/composite-resources")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CompositeResourceController {

    private static final Logger logger = LoggerFactory.getLogger(CompositeResourceController.class);

    private final CompositeResourceService compositeResourceService;

    public CompositeResourceController(CompositeResourceService compositeResourceService) {
        this.compositeResourceService = compositeResourceService;
    }

    /**
     * Crée une nouvelle ressource composite
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<CompositeResourceDTO>>> createCompositeResource(
            @Valid @RequestBody CreateCompositeResourceDTO createCompositeResourceDTO) {
        logger.info("Demande de création d'une ressource composite: {}", createCompositeResourceDTO.name());

        return compositeResourceService.createCompositeResource(createCompositeResourceDTO)
                .map(resourceDTO -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Ressource composite créée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la création de la ressource composite")));
    }

    /**
     * Récupère une ressource composite par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<CompositeResourceDTO>>> getCompositeResourceById(@PathVariable Long id) {
        logger.debug("Demande de récupération de la ressource composite ID: {}", id);

        return compositeResourceService.findById(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success(resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Ressource composite non trouvée")));
    }

    /**
     * Récupère toutes les ressources composites
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<CompositeResourceDTO>>>> getAllCompositeResources() {
        logger.debug("Demande de récupération de toutes les ressources composites");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(compositeResourceService.findAll())));
    }

    /**
     * Réserve une ressource composite (VIDE -> EN_COURS_RÉSERVATION -> PRÊT)
     */
    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<CompositeResourceDTO>>> reserveCompositeResource(@PathVariable Long id) {
        logger.info("Demande de réservation de la ressource composite ID: {}", id);

        return compositeResourceService.reserveResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource composite réservée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la réservation de la ressource composite")));
    }

    /**
     * Utilise une ressource composite (PRÊT -> AFFECTÉ)
     */
    @PostMapping("/{id}/use")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<CompositeResourceDTO>>> useCompositeResource(@PathVariable Long id) {
        logger.info("Demande d'utilisation de la ressource composite ID: {}", id);

        return compositeResourceService.useResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource composite utilisée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de l'utilisation de la ressource composite")));
    }

    /**
     * Libère une ressource composite (AFFECTÉ -> VIDE)
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<CompositeResourceDTO>>> releaseCompositeResource(@PathVariable Long id) {
        logger.info("Demande de libération de la ressource composite ID: {}", id);

        return compositeResourceService.releaseResource(id)
                .map(resourceDTO -> ResponseEntity.ok(ApiResponse.success("Ressource composite libérée avec succès", resourceDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de la libération de la ressource composite")));
    }
}
