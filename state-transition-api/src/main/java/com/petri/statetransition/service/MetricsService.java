package com.petri.statetransition.service;

/**
 * Service pour la collecte et calcul des métriques du système
 */

import com.petri.statetransition.dto.SystemMetricsDTO;
import com.petri.statetransition.model.enums.TransitionStatus;
import com.petri.statetransition.repository.CompositeResourceRepository;
import com.petri.statetransition.repository.ServiceRepository;
import com.petri.statetransition.repository.TransitionRepository;
import com.petri.statetransition.repository.UnitResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final ServiceRepository serviceRepository;
    private final UnitResourceRepository unitResourceRepository;
    private final CompositeResourceRepository compositeResourceRepository;
    private final TransitionRepository transitionRepository;

    public MetricsService(
            ServiceRepository serviceRepository,
            UnitResourceRepository unitResourceRepository,
            CompositeResourceRepository compositeResourceRepository,
            TransitionRepository transitionRepository) {
        this.serviceRepository = serviceRepository;
        this.unitResourceRepository = unitResourceRepository;
        this.compositeResourceRepository = compositeResourceRepository;
        this.transitionRepository = transitionRepository;
    }

    /**
     * Collecte toutes les métriques système
     */
    public Mono<SystemMetricsDTO> getSystemMetrics() {
        logger.debug("Collecte des métriques système");

        return Mono.zip(
                collectServiceMetrics(),
                collectUnitResourceMetrics(),
                collectCompositeResourceMetrics(),
                collectTransitionMetrics(),
                calculatePerformanceMetrics()
        ).map(tuple -> new SystemMetricsDTO(
                tuple.getT1().get("total"),
                convertToStringMap(tuple.getT1()),
                tuple.getT2().get("total"),
                convertToStringMap(tuple.getT2()),
                tuple.getT3().get("total"),
                convertToStringMap(tuple.getT3()),
                tuple.getT4().get("total"),
                tuple.getT4().get("active"),
                tuple.getT4().get("failed"),
                tuple.getT5().get("avgExecutionTime"),
                tuple.getT5().get("throughput"),
                LocalDateTime.now()
        ));
    }

    private Mono<Map<String, Long>> collectServiceMetrics() {
        return serviceRepository.getServiceCountByState()
                .collectMap(
                        count -> count.getServiceState(),
                        count -> count.getCountServices()
                )
                .flatMap(stateMap ->
                        serviceRepository.count()
                                .map(total -> {
                                    Map<String, Long> metrics = new HashMap<>(stateMap);
                                    metrics.put("total", total);
                                    return metrics;
                                })
                );
    }

    private Mono<Map<String, Long>> collectUnitResourceMetrics() {
        return unitResourceRepository.getResourceCountByState()
                .collectMap(
                        count -> count.getResourceState(),
                        count -> count.getCountResources()
                )
                .flatMap(stateMap ->
                        unitResourceRepository.count()
                                .map(total -> {
                                    Map<String, Long> metrics = new HashMap<>(stateMap);
                                    metrics.put("total", total);
                                    return metrics;
                                })
                );
    }

    private Mono<Map<String, Long>> collectCompositeResourceMetrics() {
        return compositeResourceRepository.getResourceCountByState()
                .collectMap(
                        count -> count.getResourceState(),
                        count -> count.getCountResources()
                )
                .flatMap(stateMap ->
                        compositeResourceRepository.count()
                                .map(total -> {
                                    Map<String, Long> metrics = new HashMap<>(stateMap);
                                    metrics.put("total", total);
                                    return metrics;
                                })
                );
    }

    private Mono<Map<String, Long>> collectTransitionMetrics() {
        return Mono.zip(
                transitionRepository.count(),
                transitionRepository.countByStatus(TransitionStatus.EN_ATTENTE)
                        .mergeWith(transitionRepository.countByStatus(TransitionStatus.EN_COURS))
                        .reduce(Long::sum),
                transitionRepository.countByStatus(TransitionStatus.ECHOUEE)
        ).map(tuple -> {
            Map<String, Long> metrics = new HashMap<>();
            metrics.put("total", tuple.getT1());
            metrics.put("active", tuple.getT2());
            metrics.put("failed", tuple.getT3());
            return metrics;
        });
    }

    private Mono<Map<String, Double>> calculatePerformanceMetrics() {
        return Mono.zip(
                transitionRepository.getAverageExecutionTimeSeconds().defaultIfEmpty(0.0),
                calculateSystemThroughput()
        ).map(tuple -> {
            Map<String, Double> metrics = new HashMap<>();
            metrics.put("avgExecutionTime", tuple.getT1());
            metrics.put("throughput", tuple.getT2());
            return metrics;
        });
    }

    private Mono<Double> calculateSystemThroughput() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return transitionRepository.findByCreatedAtBetween(oneDayAgo, LocalDateTime.now())
                .count()
                .map(count -> count / 24.0); // Transitions par heure
    }

    /**
     * Obtient les métriques de performance sur une période
     */
    public Mono<Map<String, Object>> getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        return transitionRepository.findByCreatedAtBetween(startDate, endDate)
                .collectList()
                .map(transitions -> {
                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("totalTransitions", transitions.size());
                    metrics.put("successfulTransitions",
                            transitions.stream().filter(t -> t.getStatus() == TransitionStatus.TERMINEE).count());
                    metrics.put("failedTransitions",
                            transitions.stream().filter(t -> t.getStatus() == TransitionStatus.ECHOUEE).count());

                    double avgDuration = transitions.stream()
                            .filter(t -> t.getStartedAt() != null && t.getCompletedAt() != null)
                            .mapToLong(t -> java.time.Duration.between(t.getStartedAt(), t.getCompletedAt()).toSeconds())
                            .average()
                            .orElse(0.0);

                    metrics.put("averageDurationSeconds", avgDuration);
                    return metrics;
                });
    }

    private Map<String, Long> convertToStringMap(Map<String, Long> originalMap) {
        return new HashMap<>(originalMap);
    }
}