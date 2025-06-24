package com.petri.statetransition.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateCompositeResourceDTO(
        @NotBlank(message = "Le nom de la ressource composite est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description,

        @NotNull(message = "Les composants sont obligatoires")
        List<Long> componentUnitResourceIds,

        String location,
        Integer minRequiredComponents
) {
}