package com.petri.statetransition.service;

/**
 * Service pour la gestion des transitions dans le système de réseaux de Petri
 */

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.entity.Transition;
import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import com.petri.statetransition.repository.*;
import com.petri.statetransition.exception.ResourceNotFoundException;
import com.petri.statetransition.exception.TransitionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@Service
@Transactional
public class TransitionService {

    private static final Logger logger = LoggerFactory.getLogger(TransitionService.class);

    private final TransitionRepository transitionRepository;
    private final ObjectMapper objectMapper;

    public TransitionService(TransitionRepository transitionRepository, ObjectMapper objectMapper) {
        this.transitionRepository = transitionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Enregistre une nouvelle transition
     */
    public Mono<TransitionDTO> recordTransition(String description, List<Long> serviceIds,
                                                List<Long> unitResourceIds, List<Long> compositeResourceIds) {
        return recordTransition(TransitionType.NORMALE, description, serviceIds, unitResourceIds, compositeResourceIds, null);
    }

    /**
     * Enregistre une nouvelle transition avec type et métadonnées
     */
    public Mono<TransitionDTO> recordTransition(TransitionType type, String description, List<Long> serviceIds,
                                                List<Long> unitResourceIds, List<Long> compositeResourceIds,
                                                Map<String, Object> metadata) {
        logger.debug("Enregistrement d'une transition: {}", description);

        Transition transition = new Transition(type, generateTransitionName(type), description);

        // Sérialiser les métadonnées en JSON
        if (metadata != null) {
            try {
                String metadataJson = objectMapper.writeValueAsString(metadata);
                transition.setMetadataJson(metadataJson);
            } catch (JsonProcessingException e) {
                logger.warn("Erreur lors de la sérialisation des métadonnées", e);
            }
        }

        return transitionRepository.save(transition)
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.debug("Transition enregistrée avec succès: ID {}", dto.id()));
    }

    /**
     * Déclenche une transition synchrone
     */
    public Mono<TransitionDTO> triggerSynchronousTransition(TriggerTransitionDTO triggerDTO) {
        logger.info("Déclenchement d'une transition synchrone: {}", triggerDTO.name());

        Transition transition = new Transition(
                triggerDTO.type(),
                triggerDTO.name(),
                triggerDTO.description()
        );

        if (triggerDTO.metadata() != null) {
            try {
                String metadataJson = objectMapper.writeValueAsString(triggerDTO.metadata());
                transition.setMetadataJson(metadataJson);
            } catch (JsonProcessingException e) {
                logger.warn("Erreur lors de la sérialisation des métadonnées", e);
            }
        }

        return transitionRepository.save(transition)
                .flatMap(savedTransition -> {
                    // Démarrer la transition
                    savedTransition.start();
                    return transitionRepository.save(savedTransition);
                })
                .flatMap(this::executeTransition)
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Transition synchrone exécutée avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de l'exécution de la transition synchrone", error));
    }

    /**
     * Trouve une transition par ID
     */
    public Mono<TransitionDTO> findById(Long id) {
        return transitionRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Transition non trouvée avec l'ID: " + id)))
                .map(this::convertToDTO);
    }

    /**
     * Trouve toutes les transitions
     */
    public Flux<TransitionDTO> findAll() {
        return transitionRepository.findAll()
                .map(this::convertToDTO);
    }

    /**
     * Trouve les transitions par type
     */
    public Flux<TransitionDTO> findByType(TransitionType type) {
        return transitionRepository.findByType(type)
                .map(this::convertToDTO);
    }

    /**
     * Trouve les transitions par statut
     */
    public Flux<TransitionDTO> findByStatus(TransitionStatus status) {
        return transitionRepository.findByStatus(status)
                .map(this::convertToDTO);
    }

    /**
     * Trouve les transitions actives
     */
    public Flux<TransitionDTO> findActiveTransitions() {
        return transitionRepository.findActiveTransitions()
                .map(this::convertToDTO);
    }

    /**
     * Trouve les transitions qui prennent trop de temps
     */
    public Flux<TransitionDTO> findLongRunningTransitions(Integer maxMinutes) {
        return transitionRepository.findLongRunningTransitions(maxMinutes)
                .map(this::convertToDTO);
    }

    /**
     * Traite les transitions automatiques en attente
     */
    public Flux<TransitionDTO> processAutomaticTransitions() {
        logger.info("Traitement des transitions automatiques en attente");

        return transitionRepository.findPendingAutomaticTransitions()
                .flatMap(this::processAutomaticTransition)
                .map(this::convertToDTO)
                .doOnComplete(() -> logger.info("Traitement des transitions automatiques terminé"));
    }

    /**
     * Annule une transition en cours
     */
    public Mono<TransitionDTO> cancelTransition(Long id, String reason) {
        logger.info("Annulation de la transition ID: {} pour raison: {}", id, reason);

        return transitionRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Transition non trouvée avec l'ID: " + id)))
                .flatMap(transition -> {
                    if (transition.getStatus() != TransitionStatus.EN_COURS) {
                        return Mono.error(new TransitionException(
                                "Seules les transitions en cours peuvent être annulées"
                        ));
                    }

                    transition.fail("Annulée: " + reason);
                    return transitionRepository.save(transition);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Transition annulée avec succès: ID {}", dto.id()));
    }

    /**
     * Nettoie les anciennes transitions terminées
     */
    public Mono<Integer> cleanupOldTransitions(Integer daysOld) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysOld);
        logger.info("Nettoyage des transitions antérieures à: {}", beforeDate);

        return transitionRepository.deleteOldCompletedTransitions(beforeDate)
                .doOnSuccess(count -> logger.info("Nettoyage terminé: {} transitions supprimées", count));
    }

    // Méthodes privées

    private Mono<Transition> executeTransition(Transition transition) {
        // Simulation de l'exécution d'une transition
        // Dans un vrai système, ici on exécuterait la logique métier spécifique
        return Mono.delay(java.time.Duration.ofMillis(100)) // Simulation d'un traitement
                .then(Mono.defer(() -> {
                    if (Math.random() > 0.9) { // 10% de chance d'échec pour simulation
                        transition.fail("Échec simulé de la transition");
                    } else {
                        transition.complete();
                    }
                    return transitionRepository.save(transition);
                }));
    }

    private Mono<Transition> processAutomaticTransition(Transition transition) {
        logger.debug("Traitement de la transition automatique ID: {}", transition.getId());

        transition.start();
        return transitionRepository.save(transition)
                .flatMap(this::executeTransition);
    }

    private String generateTransitionName(TransitionType type) {
        return type.getCode() + "_" + System.currentTimeMillis();
    }

    private TransitionDTO convertToDTO(Transition transition) {
        Map<String, Object> metadata = null;
        if (transition.getMetadataJson() != null) {
            try {
                metadata = objectMapper.readValue(transition.getMetadataJson(), Map.class);
            } catch (JsonProcessingException e) {
                logger.warn("Erreur lors de la désérialisation des métadonnées", e);
                metadata = new HashMap<>();
            }
        }

        return new TransitionDTO(
                transition.getId(),
                transition.getType(),
                transition.getStatus(),
                transition.getName(),
                transition.getDescription(),
                transition.getCreatedAt(),
                transition.getStartedAt(),
                transition.getCompletedAt(),
                null, // Les IDs seraient récupérés par des requêtes séparées si nécessaire
                null,
                null,
                metadata,
                transition.getErrorMessage()
        );
    }
}


