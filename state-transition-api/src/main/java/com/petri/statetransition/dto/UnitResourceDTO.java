package com.petri.statetransition.dto;

import com.petri.statetransition.model.enums.UnitResourceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les ressources unitaires
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnitResourceDTO(
        Long id,

        @NotBlank(message = "Le nom de la ressource est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "L'état de la ressource est obligatoire")
        UnitResourceState state,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastUsedAt,

        // Métadonnées
        String location,
        Integer capacity,
        Integer currentLoad
) {

    /**
     * Constructeur pour création d'une nouvelle ressource unitaire
     */
    public static UnitResourceDTO create(String name, String description) {
        return new UnitResourceDTO(
                null, name, description, UnitResourceState.LIBRE,
                null, null, null, null, null, null
        );
    }

    public boolean isAvailable() {
        return state != null && state.isAvailableForAllocation();
    }
}

/**
 * DTO pour les ressources composites
 */

/**
 * DTO pour la création d'une ressource unitaire
 */


/**
 * DTO pour la création d'une ressource composite
 */
