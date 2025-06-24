package com.petri.statetransition.config;

import com.petri.statetransition.dto.ApiResponse;
import com.petri.statetransition.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestionnaire global d'exceptions pour l'API
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gestion des erreurs de ressource non trouvée
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Ressource non trouvée: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage())));
    }

    /**
     * Gestion des erreurs de transition d'état invalide
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleInvalidStateTransitionException(InvalidStateTransitionException ex) {
        logger.warn("Transition d'état invalide: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Transition d'état invalide: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs d'allocation de ressources
     */
    @ExceptionHandler(ResourceAllocationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResourceAllocationException(ResourceAllocationException ex) {
        logger.warn("Erreur d'allocation de ressources: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Allocation impossible: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs de transitions
     */
    @ExceptionHandler(TransitionException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleTransitionException(TransitionException ex) {
        logger.warn("Erreur de transition: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Erreur de transition: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs de logique métier
     */
    @ExceptionHandler(BusinessLogicException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBusinessLogicException(BusinessLogicException ex) {
        logger.warn("Erreur de logique métier: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Règle métier violée: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationException(ValidationException ex) {
        logger.warn("Erreur de validation: {}", ex.getMessage());
        List<String> errors = ex.getValidationErrors() != null ?
                ex.getValidationErrors() : List.of(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Erreurs de validation", errors)));
    }

    /**
     * Gestion des erreurs de validation des requêtes WebFlux
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        logger.warn("Erreur de validation de requête: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Erreurs de validation", errors)));
    }

    /**
     * Gestion des erreurs de concurrence
     */
    @ExceptionHandler(ConcurrencyException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleConcurrencyException(ConcurrencyException ex) {
        logger.warn("Erreur de concurrence: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Conflit de concurrence: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs de configuration
     */
    @ExceptionHandler(ConfigurationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleConfigurationException(ConfigurationException ex) {
        logger.error("Erreur de configuration: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur de configuration système")));
    }

    /**
     * Gestion des erreurs d'accès
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Accès refusé: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès refusé: permissions insuffisantes")));
    }

    /**
     * Gestion des erreurs d'arguments illégaux
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Argument illégal: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Paramètre invalide: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs d'état illégal
     */
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("État illégal: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("État système invalide: " + ex.getMessage())));
    }

    /**
     * Gestion des erreurs de base State Transition
     */
    @ExceptionHandler(StateTransitionException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleStateTransitionException(StateTransitionException ex) {
        logger.warn("Erreur State Transition: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Erreur système: " + ex.getMessage())));
    }

    /**
     * Gestion de toutes les autres exceptions non gérées
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(Exception ex) {
        logger.error("Erreur inattendue", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur interne s'est produite. Veuillez contacter l'administrateur.")));
    }

    /**
     * Gestion des erreurs liées à la base de données
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleDataAccessException(org.springframework.dao.DataAccessException ex) {
        logger.error("Erreur d'accès aux données", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur d'accès aux données")));
    }

    /**
     * Gestion des timeouts
     */
    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleTimeoutException(java.util.concurrent.TimeoutException ex) {
        logger.warn("Timeout: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(ApiResponse.error("Opération expirée: " + ex.getMessage())));
    }
}