package com.petri.statetransition.controller;

import com.petri.statetransition.dto.ApiResponse;
import com.petri.statetransition.dto.SystemMetricsDTO;
import com.petri.statetransition.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contrôleur REST pour les métriques et statistiques du système
 */
@RestController
@RequestMapping("/api/v1/metrics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Récupère toutes les métriques système
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<SystemMetricsDTO>>> getSystemMetrics() {
        logger.debug("Demande de récupération des métriques système");

        return metricsService.getSystemMetrics()
                .map(metrics -> ResponseEntity.ok(ApiResponse.success("Métriques système récupérées", metrics)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors de la récupération des métriques système")));
    }

    /**
     * Récupère les métriques de performance sur une période
     */
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VIEWER')")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getPerformanceMetrics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        // Valeurs par défaut si non spécifiées
        LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusDays(7);
        LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();

        logger.debug("Demande de métriques de performance du {} au {}", start, end);

        return metricsService.getPerformanceMetrics(start, end)
                .map(metrics -> ResponseEntity.ok(ApiResponse.success("Métriques de performance récupérées", metrics)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Erreur lors de la récupération des métriques de performance")));
    }

    /**
     * Endpoint de santé personnalisé pour le système
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> getSystemHealth() {
        logger.debug("Vérification de la santé du système");

        return metricsService.getSystemMetrics()
                .map(metrics -> {
                    Map<String, Object> health = Map.of(
                            "status", "UP",
                            "timestamp", LocalDateTime.now(),
                            "totalServices", metrics.totalServices(),
                            "totalUnitResources", metrics.totalUnitResources(),
                            "totalCompositeResources", metrics.totalCompositeResources(),
                            "activeTransitions", metrics.activeTransitions(),
                            "systemThroughput", metrics.systemThroughput()
                    );
                    return ResponseEntity.ok(ApiResponse.success("Système en bonne santé", health));
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("Problème de santé du système")));
    }
}

