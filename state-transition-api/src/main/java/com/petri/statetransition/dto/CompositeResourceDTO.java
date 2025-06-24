package com.petri.statetransition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.petri.statetransition.model.enums.CompositeResourceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompositeResourceDTO(
        Long id,

        @NotBlank(message = "Le nom de la ressource composite est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "L'état de la ressource composite est obligatoire")
        CompositeResourceState state,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastUsedAt,

        // Ressources unitaires composant cette ressource composite
        List<Long> componentUnitResourceIds,

        // Métadonnées
        String location,
        Integer totalCapacity,
        Integer minRequiredComponents
) {

    /**
     * Constructeur pour création d'une nouvelle ressource composite
     */
    public static CompositeResourceDTO create(String name, String description, List<Long> componentIds) {
        return new CompositeResourceDTO(
                null, name, description, CompositeResourceState.VIDE,
                null, null, null, componentIds, null, null, null
        );
    }

    public boolean isAvailable() {
        return state != null && state.isAvailableForReservation();
    }
}
