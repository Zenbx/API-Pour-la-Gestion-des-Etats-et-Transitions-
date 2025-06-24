package com.petri.statetransition.controller;

import com.petri.statetransition.dto.ApiResponse;
import com.petri.statetransition.dto.SystemMetricsDTO;
import com.petri.statetransition.service.MetricsService;
import com.petri.statetransition.service.TransitionService;
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
 * Contrôleur REST pour les opérations administratives
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final TransitionService transitionService;
    private final MetricsService metricsService;

    public AdminController(TransitionService transitionService, MetricsService metricsService) {
        this.transitionService = transitionService;
        this.metricsService = metricsService;
    }

    /**
     * Endpoint de maintenance générale
     */
    @PostMapping("/maintenance")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> performMaintenance() {
        logger.info("Démarrage de la maintenance générale du système");

        return transitionService.cleanupOldTransitions(30)
                .flatMap(cleanupCount ->
                        transitionService.processAutomaticTransitions()
                                .count()
                                .map(processedCount -> {
                                    Map<String, Object> result = Map.of(
                                            "transitionsCleanedUp", cleanupCount,
                                            "automaticTransitionsProcessed", processedCount,
                                            "maintenanceCompletedAt", LocalDateTime.now()
                                    );
                                    return ResponseEntity.ok(ApiResponse.success("Maintenance terminée avec succès", result));
                                })
                )
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors de la maintenance")));
    }

    /**
     * Réinitialise les métriques système
     */
    @PostMapping("/reset-metrics")
    public Mono<ResponseEntity<ApiResponse<String>>> resetMetrics() {
        logger.warn("Demande de réinitialisation des métriques système");

        // Dans un vrai système, ici on réinitialiserait les compteurs, caches, etc.
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Métriques réinitialisées", "OK")));
    }

    /**
     * Export des données pour analyse
     */
    @GetMapping("/export/metrics")
    public Mono<ResponseEntity<ApiResponse<SystemMetricsDTO>>> exportMetrics() {
        logger.info("Export des métriques système");

        return metricsService.getSystemMetrics()
                .map(metrics -> ResponseEntity.ok(ApiResponse.success("Export des métriques réussi", metrics)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors de l'export des métriques")));
    }

    /**
     * Endpoint pour les tests de charge
     */
    @PostMapping("/load-test")
    public Mono<ResponseEntity<ApiResponse<String>>> performLoadTest(
            @RequestParam(defaultValue = "100") Integer numberOfOperations) {
        logger.info("Démarrage d'un test de charge avec {} opérations", numberOfOperations);

        // Simulation d'un test de charge simple
        return Flux.range(1, numberOfOperations)
                .flatMap(i -> transitionService.recordTransition(
                        "Test de charge operation " + i,
                        java.util.List.of(), null, null))
                .count()
                .map(completedOps -> ResponseEntity.ok(ApiResponse.success(
                        String.format("Test de charge terminé: %d opérations", completedOps),
                        "COMPLETED")))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors du test de charge")));
    }
}
