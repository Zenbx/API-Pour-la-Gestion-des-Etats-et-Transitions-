package com.petri.statetransition.dto;

import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les services
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceDTO(
        Long id,

        @NotBlank(message = "Le nom du service est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "L'état du service est obligatoire")
        ServiceState state,

        @NotNull(message = "Le type de service est obligatoire")
        ServiceType type,

        @NotNull(message = "La priorité est obligatoire")
        Priority priority,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,

        // IDs des ressources requises
        List<Long> requiredUnitResourceIds,
        List<Long> requiredCompositeResourceIds,

        // Métadonnées additionnelles
        Integer maxExecutionTimeMinutes,
        Boolean autoRetry
) {

    /**
     * Constructeur pour création d'un nouveau service
     */
    public static ServiceDTO create(String name, String description, ServiceType type, Priority priority) {
        return new ServiceDTO(
                null, name, description, ServiceState.PLANIFIE, type, priority,
                null, null, null, null, null, null, null, null
        );
    }

    /**
     * Vérifie si le service est dans un état final
     */
    public boolean isFinalState() {
        return state != null && state.isFinalState();
    }

    /**
     * Vérifie si le service peut être démarré
     */
    public boolean canBeStarted() {
        return state == ServiceState.PRET;
    }
}

/**
 * DTO pour la création d'un service
 */

/**
 * DTO pour la mise à jour d'un service
 */
