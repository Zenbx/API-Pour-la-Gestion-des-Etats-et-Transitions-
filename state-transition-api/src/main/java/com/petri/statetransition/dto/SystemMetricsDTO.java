package com.petri.statetransition.dto;

import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SystemMetricsDTO(
        // Statistiques des services
        Long totalServices,
        Map<String, Long> servicesByState,

        // Statistiques des ressources unitaires
        Long totalUnitResources,
        Map<String, Long> unitResourcesByState,

        // Statistiques des ressources composites
        Long totalCompositeResources,
        Map<String, Long> compositeResourcesByState,

        // Statistiques des transitions
        Long totalTransitions,
        Long activeTransitions,
        Long failedTransitions,

        // Métriques de performance
        Double averageServiceExecutionTime,
        Double systemThroughput,
        LocalDateTime lastUpdated
) {
}