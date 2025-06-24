package com.petri.statetransition.dto;

import com.petri.statetransition.model.enums.TransitionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TriggerTransitionDTO(
        @NotNull(message = "Le type de transition est obligatoire")
        TransitionType type,

        @Size(max = 200, message = "Le nom de la transition ne peut pas dépasser 200 caractères")
        String name,

        @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
        String description,

        @NotNull(message = "Au moins un service doit être spécifié")
        List<Long> serviceIds,

        List<Long> unitResourceIds,
        List<Long> compositeResourceIds,
        Map<String, Object> metadata
) {
}