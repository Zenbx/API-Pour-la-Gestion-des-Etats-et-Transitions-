package com.petri.statetransition.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<String> errors,
        LocalDateTime timestamp
) {

    /**
     * Crée une réponse de succès
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Opération réussie", data, null, LocalDateTime.now());
    }

    /**
     * Crée une réponse de succès avec message personnalisé
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /**
     * Crée une réponse d'erreur
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, LocalDateTime.now());
    }

    /**
     * Crée une réponse d'erreur avec liste d'erreurs
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(false, message, null, errors, LocalDateTime.now());
    }
}