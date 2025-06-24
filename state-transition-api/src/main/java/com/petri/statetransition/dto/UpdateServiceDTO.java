package com.petri.statetransition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import com.petri.statetransition.model.enums.Priority;


import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateServiceDTO(
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        Priority priority,
        List<Long> requiredUnitResourceIds,
        List<Long> requiredCompositeResourceIds,
        Integer maxExecutionTimeMinutes,
        Boolean autoRetry
) {
}