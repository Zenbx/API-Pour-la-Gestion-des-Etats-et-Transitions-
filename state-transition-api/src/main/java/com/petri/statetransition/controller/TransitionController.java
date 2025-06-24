package com.petri.statetransition.controller;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import com.petri.statetransition.service.TransitionService;
import com.petri.statetransition.service.MetricsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des transitions
 */
@RestController
@RequestMapping("/api/v1/transitions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransitionController {

    private static final Logger logger = LoggerFactory.getLogger(TransitionController.class);

    private final TransitionService transitionService;

    public TransitionController(TransitionService transitionService) {
        this.transitionService = transitionService;
    }

    /**
     * Déclenche une nouvelle transition synchrone
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<TransitionDTO>>> triggerTransition(
            @Valid @RequestBody TriggerTransitionDTO triggerTransitionDTO) {
        logger.info("Demande de déclenchement d'une transition: {}", triggerTransitionDTO.name());

        return transitionService.triggerSynchronousTransition(triggerTransitionDTO)
                .map(transitionDTO -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Transition déclenchée avec succès", transitionDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors du déclenchement de la transition")));
    }

    /**
     * Récupère une transition par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<TransitionDTO>>> getTransitionById(@PathVariable Long id) {
        logger.debug("Demande de récupération de la transition ID: {}", id);

        return transitionService.findById(id)
                .map(transitionDTO -> ResponseEntity.ok(ApiResponse.success(transitionDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Transition non trouvée")));
    }

    /**
     * Récupère toutes les transitions
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> getAllTransitions() {
        logger.debug("Demande de récupération de toutes les transitions");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(transitionService.findAll())));
    }

    /**
     * Récupère les transitions par type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> getTransitionsByType(@PathVariable TransitionType type) {
        logger.debug("Demande de récupération des transitions de type: {}", type);

        return Mono.just(ResponseEntity.ok(ApiResponse.success(transitionService.findByType(type))));
    }

    /**
     * Récupère les transitions par statut
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> getTransitionsByStatus(@PathVariable TransitionStatus status) {
        logger.debug("Demande de récupération des transitions avec le statut: {}", status);

        return Mono.just(ResponseEntity.ok(ApiResponse.success(transitionService.findByStatus(status))));
    }

    /**
     * Récupère les transitions actives
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> getActiveTransitions() {
        logger.debug("Demande de récupération des transitions actives");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(transitionService.findActiveTransitions())));
    }

    /**
     * Récupère les transitions qui prennent trop de temps
     */
    @GetMapping("/long-running")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> getLongRunningTransitions(
            @RequestParam(defaultValue = "30") Integer maxMinutes) {
        logger.debug("Demande de récupération des transitions longues (> {} minutes)", maxMinutes);

        return Mono.just(ResponseEntity.ok(ApiResponse.success(
                transitionService.findLongRunningTransitions(maxMinutes))));
    }

    /**
     * Traite les transitions automatiques en attente
     */
    @PostMapping("/process-automatic")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Flux<TransitionDTO>>>> processAutomaticTransitions() {
        logger.info("Demande de traitement des transitions automatiques");

        return Mono.just(ResponseEntity.ok(ApiResponse.success(
                "Traitement des transitions automatiques démarré",
                transitionService.processAutomaticTransitions())));
    }

    /**
     * Annule une transition en cours
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TransitionDTO>>> cancelTransition(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Annulation manuelle") String reason) {
        logger.info("Demande d'annulation de la transition ID: {} pour raison: {}", id, reason);

        return transitionService.cancelTransition(id, reason)
                .map(transitionDTO -> ResponseEntity.ok(ApiResponse.success("Transition annulée avec succès", transitionDTO)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur lors de l'annulation de la transition")));
    }

    /**
     * Nettoie les anciennes transitions
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Integer>>> cleanupOldTransitions(
            @RequestParam(defaultValue = "30") Integer daysOld) {
        logger.info("Demande de nettoyage des transitions antérieures à {} jours", daysOld);

        return transitionService.cleanupOldTransitions(daysOld)
                .map(count -> ResponseEntity.ok(ApiResponse.success(
                        String.format("Nettoyage terminé: %d transitions supprimées", count), count)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors du nettoyage des transitions")));
    }
}

