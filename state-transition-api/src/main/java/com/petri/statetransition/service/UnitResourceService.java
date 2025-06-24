package com.petri.statetransition.service;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.entity.UnitResource;
import com.petri.statetransition.model.entity.CompositeResource;
import com.petri.statetransition.model.entity.CompositeUnitResource;
import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import com.petri.statetransition.repository.*;
import com.petri.statetransition.exception.ResourceNotFoundException;
import com.petri.statetransition.exception.InvalidStateTransitionException;
import com.petri.statetransition.exception.BusinessLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service pour la gestion des ressources unitaires
 */
@Service
@Transactional
public class UnitResourceService {

    private static final Logger logger = LoggerFactory.getLogger(UnitResourceService.class);

    private final UnitResourceRepository unitResourceRepository;
    private final CompositeUnitResourceRepository compositeUnitResourceRepository;
    private final ServiceUnitResourceRepository serviceUnitResourceRepository;

    public UnitResourceService(
            UnitResourceRepository unitResourceRepository,
            CompositeUnitResourceRepository compositeUnitResourceRepository,
            ServiceUnitResourceRepository serviceUnitResourceRepository) {
        this.unitResourceRepository = unitResourceRepository;
        this.compositeUnitResourceRepository = compositeUnitResourceRepository;
        this.serviceUnitResourceRepository = serviceUnitResourceRepository;
    }

    /**
     * Crée une nouvelle ressource unitaire
     */
    public Mono<UnitResourceDTO> createUnitResource(CreateUnitResourceDTO createUnitResourceDTO) {
        logger.info("Création d'une nouvelle ressource unitaire: {}", createUnitResourceDTO.name());

        UnitResource resource = new UnitResource(
                createUnitResourceDTO.name(),
                createUnitResourceDTO.description()
        );
        resource.setLocation(createUnitResourceDTO.location());
        resource.setCapacity(createUnitResourceDTO.capacity());

        return unitResourceRepository.save(resource)
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource unitaire créée avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de la création de la ressource unitaire", error));
    }

    /**
     * Met à jour une ressource unitaire existante
     */
    public Mono<UnitResourceDTO> updateUnitResource(Long id, CreateUnitResourceDTO updateDTO) {
        logger.info("Mise à jour de la ressource unitaire ID: {}", id);

        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (updateDTO.name() != null) {
                        resource.setName(updateDTO.name());
                    }
                    if (updateDTO.description() != null) {
                        resource.setDescription(updateDTO.description());
                    }
                    if (updateDTO.location() != null) {
                        resource.setLocation(updateDTO.location());
                    }
                    if (updateDTO.capacity() != null) {
                        resource.setCapacity(updateDTO.capacity());
                    }

                    return unitResourceRepository.save(resource);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource unitaire mise à jour avec succès: ID {}", dto.id()));
    }

    /**
     * Trouve une ressource unitaire par ID
     */
    public Mono<UnitResourceDTO> findById(Long id) {
        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .map(this::convertToDTO);
    }

    /**
     * Trouve toutes les ressources unitaires
     */
    public Flux<UnitResourceDTO> findAll() {
        return unitResourceRepository.findAll()
                .map(this::convertToDTO);
    }

    /**
     * Trouve les ressources unitaires par état
     */
    public Flux<UnitResourceDTO> findByState(UnitResourceState state) {
        return unitResourceRepository.findByState(state)
                .map(this::convertToDTO);
    }

    /**
     * Trouve les ressources unitaires disponibles
     */
    public Flux<UnitResourceDTO> findAvailableResources() {
        return unitResourceRepository.findAvailableResources()
                .map(this::convertToDTO);
    }

    /**
     * Alloue une ressource unitaire (LIBRE -> AFFECTÉ)
     */
    public Mono<UnitResourceDTO> allocateResource(Long id) {
        logger.info("Allocation de la ressource unitaire ID: {}", id);

        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() != UnitResourceState.LIBRE) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource doit être libre pour être allouée. État actuel: %s",
                                        resource.getState())
                        ));
                    }

                    resource.transitionTo(UnitResourceState.AFFECTE);
                    return unitResourceRepository.save(resource);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource unitaire allouée avec succès: ID {}", dto.id()));
    }

    /**
     * Utilise une ressource unitaire (AFFECTÉ -> OCCUPÉ)
     */
    public Mono<UnitResourceDTO> useResource(Long id) {
        logger.info("Utilisation de la ressource unitaire ID: {}", id);

        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() != UnitResourceState.AFFECTE) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource doit être affectée pour être utilisée. État actuel: %s",
                                        resource.getState())
                        ));
                    }

                    resource.transitionTo(UnitResourceState.OCCUPE);
                    return unitResourceRepository.save(resource);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource unitaire utilisée avec succès: ID {}", dto.id()));
    }

    /**
     * Libère une ressource unitaire (OCCUPÉ/AFFECTÉ -> LIBRE)
     */
    public Mono<UnitResourceDTO> releaseResource(Long id) {
        logger.info("Libération de la ressource unitaire ID: {}", id);

        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (!resource.getState().canBeReleased()) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource ne peut pas être libérée dans l'état: %s",
                                        resource.getState())
                        ));
                    }

                    resource.transitionTo(UnitResourceState.LIBRE);
                    return unitResourceRepository.save(resource);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource unitaire libérée avec succès: ID {}", dto.id()));
    }

    /**
     * Supprime une ressource unitaire
     */
    public Mono<Void> deleteUnitResource(Long id) {
        logger.info("Suppression de la ressource unitaire ID: {}", id);

        return unitResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource unitaire non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() == UnitResourceState.OCCUPE) {
                        return Mono.error(new BusinessLogicException(
                                "Impossible de supprimer une ressource en cours d'utilisation"
                        ));
                    }

                    // Supprimer les associations et la ressource
                    return deleteResourceAssociations(id)
                            .then(unitResourceRepository.deleteById(id));
                })
                .doOnSuccess(v -> logger.info("Ressource unitaire supprimée avec succès: ID {}", id));
    }

    private Mono<Void> deleteResourceAssociations(Long resourceId) {
        return serviceUnitResourceRepository.findByUnitResourceId(resourceId)
                .flatMap(serviceUnitResourceRepository::delete)
                .then(compositeUnitResourceRepository.findByUnitResourceId(resourceId)
                        .flatMap(compositeUnitResourceRepository::delete)
                        .then());
    }

    private UnitResourceDTO convertToDTO(UnitResource resource) {
        return new UnitResourceDTO(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getState(),
                resource.getCreatedAt(),
                resource.getUpdatedAt(),
                resource.getLastUsedAt(),
                resource.getLocation(),
                resource.getCapacity(),
                resource.getCurrentLoad()
        );
    }
}

