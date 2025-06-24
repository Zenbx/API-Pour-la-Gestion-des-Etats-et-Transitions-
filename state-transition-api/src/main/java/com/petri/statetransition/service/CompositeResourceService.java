package com.petri.statetransition.service;

import com.petri.statetransition.dto.CompositeResourceDTO;
import com.petri.statetransition.dto.CreateCompositeResourceDTO;
import com.petri.statetransition.exception.InvalidStateTransitionException;
import com.petri.statetransition.exception.ResourceNotFoundException;
import com.petri.statetransition.model.entity.CompositeResource;
import com.petri.statetransition.model.entity.CompositeUnitResource;
import com.petri.statetransition.model.enums.CompositeResourceState;
import com.petri.statetransition.repository.CompositeResourceRepository;
import com.petri.statetransition.repository.CompositeUnitResourceRepository;
import com.petri.statetransition.repository.ServiceCompositeResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service pour la gestion des ressources composites
 */
@Service
@Transactional
public class CompositeResourceService {

    private static final Logger logger = LoggerFactory.getLogger(CompositeResourceService.class);

    private final CompositeResourceRepository compositeResourceRepository;
    private final CompositeUnitResourceRepository compositeUnitResourceRepository;
    private final ServiceCompositeResourceRepository serviceCompositeResourceRepository;
    private final UnitResourceService unitResourceService;

    public CompositeResourceService(
            CompositeResourceRepository compositeResourceRepository,
            CompositeUnitResourceRepository compositeUnitResourceRepository,
            ServiceCompositeResourceRepository serviceCompositeResourceRepository,
            UnitResourceService unitResourceService) {
        this.compositeResourceRepository = compositeResourceRepository;
        this.compositeUnitResourceRepository = compositeUnitResourceRepository;
        this.serviceCompositeResourceRepository = serviceCompositeResourceRepository;
        this.unitResourceService = unitResourceService;
    }

    /**
     * Crée une nouvelle ressource composite
     */
    public Mono<CompositeResourceDTO> createCompositeResource(CreateCompositeResourceDTO createDTO) {
        logger.info("Création d'une nouvelle ressource composite: {}", createDTO.name());

        CompositeResource resource = new CompositeResource(
                createDTO.name(),
                createDTO.description()
        );
        resource.setLocation(createDTO.location());
        resource.setMinRequiredComponents(createDTO.minRequiredComponents());

        return compositeResourceRepository.save(resource)
                .flatMap(savedResource -> {
                    // Associer les composants unitaires
                    return associateUnitResources(savedResource.getId(), createDTO.componentUnitResourceIds())
                            .then(Mono.just(savedResource));
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource composite créée avec succès: ID {}", dto.id()));
    }

    /**
     * Trouve une ressource composite par ID
     */
    public Mono<CompositeResourceDTO> findById(Long id) {
        return compositeResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource composite non trouvée avec l'ID: " + id)))
                .flatMap(this::convertToDTOWithComponents);
    }

    /**
     * Trouve toutes les ressources composites
     */
    public Flux<CompositeResourceDTO> findAll() {
        return compositeResourceRepository.findAll()
                .flatMap(this::convertToDTOWithComponents);
    }

    /**
     * Réserve une ressource composite (VIDE -> EN_COURS_RÉSERVATION -> PRÊT)
     */
    public Mono<CompositeResourceDTO> reserveResource(Long id) {
        logger.info("Réservation de la ressource composite ID: {}", id);

        return compositeResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource composite non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() != CompositeResourceState.VIDE) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource doit être vide pour être réservée. État actuel: %s",
                                        resource.getState())
                        ));
                    }

                    // Démarrer la réservation
                    resource.transitionTo(CompositeResourceState.EN_COURS_RESERVATION);
                    return compositeResourceRepository.save(resource)
                            .flatMap(this::allocateComponents);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource composite réservée avec succès: ID {}", dto.id()));
    }

    /**
     * Utilise une ressource composite (PRÊT -> AFFECTÉ)
     */
    public Mono<CompositeResourceDTO> useResource(Long id) {
        logger.info("Utilisation de la ressource composite ID: {}", id);

        return compositeResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource composite non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() != CompositeResourceState.PRET) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource doit être prête pour être utilisée. État actuel: %s",
                                        resource.getState())
                        ));
                    }

                    resource.transitionTo(CompositeResourceState.AFFECTE);
                    return compositeResourceRepository.save(resource)
                            .flatMap(this::useComponents);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource composite utilisée avec succès: ID {}", dto.id()));
    }

    /**
     * Libère une ressource composite (AFFECTÉ -> VIDE)
     */
    public Mono<CompositeResourceDTO> releaseResource(Long id) {
        logger.info("Libération de la ressource composite ID: {}", id);

        return compositeResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Ressource composite non trouvée avec l'ID: " + id)))
                .flatMap(resource -> {
                    if (resource.getState() != CompositeResourceState.AFFECTE) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("La ressource doit être affectée pour être libérée. État actuel: %s",
                                        resource.getState())
                        ));
                    }

                    return releaseComponents(resource)
                            .then(Mono.defer(() -> {
                                resource.transitionTo(CompositeResourceState.VIDE);
                                return compositeResourceRepository.save(resource);
                            }));
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Ressource composite libérée avec succès: ID {}", dto.id()));
    }

    private Mono<CompositeResource> allocateComponents(CompositeResource resource) {
        return compositeUnitResourceRepository.findByCompositeResourceId(resource.getId())
                .flatMap(association -> unitResourceService.allocateResource(association.getUnitResourceId()))
                .then(Mono.defer(() -> {
                    resource.transitionTo(CompositeResourceState.PRET);
                    return compositeResourceRepository.save(resource);
                }));
    }

    private Mono<CompositeResource> useComponents(CompositeResource resource) {
        return compositeUnitResourceRepository.findByCompositeResourceId(resource.getId())
                .flatMap(association -> unitResourceService.useResource(association.getUnitResourceId()))
                .then(Mono.just(resource));
    }

    private Mono<Void> releaseComponents(CompositeResource resource) {
        return compositeUnitResourceRepository.findByCompositeResourceId(resource.getId())
                .flatMap(association -> unitResourceService.releaseResource(association.getUnitResourceId()))
                .then();
    }

    private Mono<Void> associateUnitResources(Long compositeId, List<Long> unitResourceIds) {
        return Flux.fromIterable(unitResourceIds != null ? unitResourceIds : List.of())
                .map(unitId -> new CompositeUnitResource(compositeId, unitId, true))
                .flatMap(compositeUnitResourceRepository::save)
                .then();
    }

    private Mono<CompositeResourceDTO> convertToDTOWithComponents(CompositeResource resource) {
        return compositeUnitResourceRepository.findByCompositeResourceId(resource.getId())
                .map(CompositeUnitResource::getUnitResourceId)
                .collectList()
                .map(componentIds -> new CompositeResourceDTO(
                        resource.getId(),
                        resource.getName(),
                        resource.getDescription(),
                        resource.getState(),
                        resource.getCreatedAt(),
                        resource.getUpdatedAt(),
                        resource.getLastUsedAt(),
                        componentIds,
                        resource.getLocation(),
                        resource.getTotalCapacity(),
                        resource.getMinRequiredComponents()
                ));
    }

    private CompositeResourceDTO convertToDTO(CompositeResource resource) {
        return new CompositeResourceDTO(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getState(),
                resource.getCreatedAt(),
                resource.getUpdatedAt(),
                resource.getLastUsedAt(),
                null, // Les composants seraient récupérés par une autre requête si nécessaire
                resource.getLocation(),
                resource.getTotalCapacity(),
                resource.getMinRequiredComponents()
        );
    }
}