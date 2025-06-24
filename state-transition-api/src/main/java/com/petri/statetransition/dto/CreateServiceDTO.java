package com.petri.statetransition.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.petri.statetransition.model.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.petri.statetransition.model.enums.Priority;


import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateServiceDTO(
        @NotBlank(message = "Le nom du service est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "Le type de service est obligatoire")
        ServiceType type,

        @NotNull(message = "La priorité est obligatoire")
        Priority priority,

        List<Long> requiredUnitResourceIds,
        List<Long> requiredCompositeResourceIds,
        Integer maxExecutionTimeMinutes,
        Boolean autoRetry
) {
}
