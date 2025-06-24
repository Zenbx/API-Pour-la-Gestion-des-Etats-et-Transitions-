package com.petri.statetransition.dto;

import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO pour les transitions
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransitionDTO(
        Long id,

        @NotNull(message = "Le type de transition est obligatoire")
        TransitionType type,

        @NotNull(message = "Le statut de transition est obligatoire")
        TransitionStatus status,

        @Size(max = 200, message = "Le nom de la transition ne peut pas dépasser 200 caractères")
        String name,

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        String description,

        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,

        // Entités impliquées dans la transition
        List<Long> serviceIds,
        List<Long> unitResourceIds,
        List<Long> compositeResourceIds,

        // Métadonnées de la transition
        Map<String, Object> metadata,
        String errorMessage
) {
}