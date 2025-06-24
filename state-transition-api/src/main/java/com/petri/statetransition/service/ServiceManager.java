package com.petri.statetransition.service;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.entity.Services;
import com.petri.statetransition.model.entity.ServiceUnitResource;
import com.petri.statetransition.model.entity.ServiceCompositeResource;
import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.repository.ServiceRepository;
import com.petri.statetransition.repository.ServiceUnitResourceRepository;
import com.petri.statetransition.repository.ServiceCompositeResourceRepository;
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
 * Service pour la gestion des services dans le système de réseaux de Petri
 */
@Service
@Transactional
public class ServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private final ServiceRepository serviceRepository;
    private final ServiceUnitResourceRepository serviceUnitResourceRepository;
    private final ServiceCompositeResourceRepository serviceCompositeResourceRepository;
    private final ResourceAllocationService resourceAllocationService;
    private final TransitionService transitionService;

    public ServiceManager(
            ServiceRepository serviceRepository,
            ServiceUnitResourceRepository serviceUnitResourceRepository,
            ServiceCompositeResourceRepository serviceCompositeResourceRepository,
            @org.springframework.context.annotation.Lazy ResourceAllocationService resourceAllocationService,
            @org.springframework.context.annotation.Lazy TransitionService transitionService) {
        this.serviceRepository = serviceRepository;
        this.serviceUnitResourceRepository = serviceUnitResourceRepository;
        this.serviceCompositeResourceRepository = serviceCompositeResourceRepository;
        this.resourceAllocationService = resourceAllocationService;
        this.transitionService = transitionService;
    }

    /**
     * Crée un nouveau service
     */
    public Mono<ServiceDTO> createService(CreateServiceDTO createServiceDTO) {
        logger.info("Création d'un nouveau service: {}", createServiceDTO.name());

        Services service = new Services(
                createServiceDTO.name(),
                createServiceDTO.description(),
                createServiceDTO.type(),
                createServiceDTO.priority()
        );

        service.setMaxExecutionTimeMinutes(createServiceDTO.maxExecutionTimeMinutes());
        service.setAutoRetry(createServiceDTO.autoRetry() != null ? createServiceDTO.autoRetry() : false);

        return serviceRepository.save(service)
                .flatMap(savedService -> {
                    // Associer les ressources requises
                    return associateResources(savedService.getId(),
                            createServiceDTO.requiredUnitResourceIds(),
                            createServiceDTO.requiredCompositeResourceIds())
                            .then(Mono.just(savedService));
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Service créé avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de la création du service", error));
    }

    /**
     * Met à jour un service existant
     */
    public Mono<ServiceDTO> updateService(Long id, UpdateServiceDTO updateServiceDTO) {
        logger.info("Mise à jour du service ID: {}", id);

        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .flatMap(service -> {
                    // Mise à jour des champs modifiables
                    if (updateServiceDTO.name() != null) {
                        service.setName(updateServiceDTO.name());
                    }
                    if (updateServiceDTO.description() != null) {
                        service.setDescription(updateServiceDTO.description());
                    }
                    if (updateServiceDTO.priority() != null) {
                        service.setPriority(updateServiceDTO.priority());
                    }
                    if (updateServiceDTO.maxExecutionTimeMinutes() != null) {
                        service.setMaxExecutionTimeMinutes(updateServiceDTO.maxExecutionTimeMinutes());
                    }
                    if (updateServiceDTO.autoRetry() != null) {
                        service.setAutoRetry(updateServiceDTO.autoRetry());
                    }

                    return serviceRepository.save(service);
                })
                .flatMap(savedService -> {
                    // Mise à jour des associations de ressources si nécessaire
                    if (updateServiceDTO.requiredUnitResourceIds() != null ||
                            updateServiceDTO.requiredCompositeResourceIds() != null) {
                        return updateResourceAssociations(savedService.getId(),
                                updateServiceDTO.requiredUnitResourceIds(),
                                updateServiceDTO.requiredCompositeResourceIds())
                                .then(Mono.just(savedService));
                    }
                    return Mono.just(savedService);
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Service mis à jour avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de la mise à jour du service ID: {}", id, error));
    }

    /**
     * Trouve un service par ID
     */
    public Mono<ServiceDTO> findById(Long id) {
        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .map(this::convertToDTO);
    }

    /**
     * Trouve tous les services
     */
    public Flux<ServiceDTO> findAll() {
        return serviceRepository.findAll()
                .map(this::convertToDTO);
    }

    /**
     * Trouve les services par état
     */
    public Flux<ServiceDTO> findByState(ServiceState state) {
        return serviceRepository.findByState(state)
                .map(this::convertToDTO);
    }

    /**
     * Recherche avec critères
     */
    public Flux<ServiceDTO> searchServices(SearchCriteriaDTO criteria) {
        return serviceRepository.findWithFilters(
                criteria.name(),
                criteria.states() != null && !criteria.states().isEmpty() ?
                        ServiceState.valueOf(criteria.states().get(0)) : null,
                criteria.type() != null ?
                        com.petri.statetransition.model.enums.ServiceType.valueOf(criteria.type()) : null,
                criteria.priority() != null ?
                        com.petri.statetransition.model.enums.Priority.valueOf(criteria.priority()) : null,
                criteria.createdAfter(),
                criteria.createdBefore()
        ).map(this::convertToDTO);
    }

    /**
     * Démarre un service (transition PRÊT -> EN_COURS)
     */
    public Mono<ServiceDTO> startService(Long id) {
        logger.info("Démarrage du service ID: {}", id);

        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .flatMap(service -> {
                    if (service.getState() != ServiceState.PRET) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("Le service doit être en état PRÊT pour être démarré. État actuel: %s",
                                        service.getState())
                        ));
                    }

                    // Vérifier la disponibilité des ressources
                    return resourceAllocationService.checkResourceAvailability(service.getId())
                            .flatMap(available -> {
                                if (!available) {
                                    return transitionToBlocked(service);
                                } else {
                                    // Allouer les ressources et démarrer
                                    return resourceAllocationService.allocateResources(service.getId())
                                            .then(transitionToInProgress(service));
                                }
                            });
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Service démarré avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors du démarrage du service ID: {}", id, error));
    }

    /**
     * Termine un service (transition EN_COURS -> TERMINÉ)
     */
    public Mono<ServiceDTO> completeService(Long id) {
        logger.info("Finalisation du service ID: {}", id);

        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .flatMap(service -> {
                    if (service.getState() != ServiceState.EN_COURS) {
                        return Mono.error(new InvalidStateTransitionException(
                                String.format("Le service doit être en cours pour être terminé. État actuel: %s",
                                        service.getState())
                        ));
                    }

                    // Libérer les ressources et terminer
                    return resourceAllocationService.releaseResources(service.getId())
                            .then(transitionToCompleted(service));
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Service terminé avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de la finalisation du service ID: {}", id, error));
    }

    /**
     * Annule un service
     */
    public Mono<ServiceDTO> cancelService(Long id) {
        logger.info("Annulation du service ID: {}", id);

        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .flatMap(service -> {
                    if (service.isFinalState()) {
                        return Mono.error(new InvalidStateTransitionException(
                                "Impossible d'annuler un service dans un état final"
                        ));
                    }

                    // Libérer les ressources si allouées et annuler
                    return resourceAllocationService.releaseResources(service.getId())
                            .then(transitionToCancelled(service));
                })
                .map(this::convertToDTO)
                .doOnSuccess(dto -> logger.info("Service annulé avec succès: ID {}", dto.id()))
                .doOnError(error -> logger.error("Erreur lors de l'annulation du service ID: {}", id, error));
    }

    /**
     * Supprime un service
     */
    public Mono<Void> deleteService(Long id) {
        logger.info("Suppression du service ID: {}", id);

        return serviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Service non trouvé avec l'ID: " + id)))
                .flatMap(service -> {
                    if (service.getState() == ServiceState.EN_COURS) {
                        return Mono.error(new BusinessLogicException(
                                "Impossible de supprimer un service en cours d'exécution"
                        ));
                    }

                    // Supprimer les associations et le service
                    return deleteResourceAssociations(service.getId())
                            .then(serviceRepository.deleteById(id));
                })
                .doOnSuccess(v -> logger.info("Service supprimé avec succès: ID {}", id))
                .doOnError(error -> logger.error("Erreur lors de la suppression du service ID: {}", id, error));
    }

    // Méthodes privées pour les transitions d'état

    private Mono<Services> transitionToInProgress(Services service) {
        service.transitionTo(ServiceState.EN_COURS);
        return serviceRepository.save(service)
                .flatMap(savedService ->
                        transitionService.recordTransition("Service démarré", List.of(savedService.getId()), null, null)
                                .then(Mono.just(savedService))
                );
    }

    private Mono<Services> transitionToCompleted(Services service) {
        service.transitionTo(ServiceState.TERMINE);
        return serviceRepository.save(service)
                .flatMap(savedService ->
                        transitionService.recordTransition("Service terminé", List.of(savedService.getId()), null, null)
                                .then(Mono.just(savedService))
                );
    }

    private Mono<Services> transitionToBlocked(Services service) {
        service.transitionTo(ServiceState.BLOQUE);
        return serviceRepository.save(service)
                .flatMap(savedService ->
                        transitionService.recordTransition("Service bloqué", List.of(savedService.getId()), null, null)
                                .then(Mono.just(savedService))
                );
    }

    private Mono<Services> transitionToCancelled(Services service) {
        service.transitionTo(ServiceState.ANNULE);
        return serviceRepository.save(service)
                .flatMap(savedService ->
                        transitionService.recordTransition("Service annulé", List.of(savedService.getId()), null, null)
                                .then(Mono.just(savedService))
                );
    }

    // Méthodes utilitaires
    private Mono<Void> associateResources(Long serviceId, List<Long> unitResourceIds, List<Long> compositeResourceIds) {
        Flux<Void> unitAssociations = Flux.fromIterable(unitResourceIds != null ? unitResourceIds : List.of())
                .map(resourceId -> new ServiceUnitResource(serviceId, resourceId, true))
                .flatMap(serviceUnitResourceRepository::save)
                .then().flux();

        Flux<Void> compositeAssociations = Flux.fromIterable(compositeResourceIds != null ? compositeResourceIds : List.of())
                .map(resourceId -> new ServiceCompositeResource(serviceId, resourceId, true))
                .flatMap(serviceCompositeResourceRepository::save)
                .then().flux();

        return Flux.merge(unitAssociations, compositeAssociations).then();
    }

    private Mono<Void> updateResourceAssociations(Long serviceId, List<Long> unitResourceIds, List<Long> compositeResourceIds) {
        return deleteResourceAssociations(serviceId)
                .then(associateResources(serviceId, unitResourceIds, compositeResourceIds));
    }

    private Mono<Void> deleteResourceAssociations(Long serviceId) {
        return serviceUnitResourceRepository.findByServiceId(serviceId)
                .flatMap(serviceUnitResourceRepository::delete)
                .then(serviceCompositeResourceRepository.findByServiceId(serviceId)
                        .flatMap(serviceCompositeResourceRepository::delete)
                        .then());
    }

    private ServiceDTO convertToDTO(Services service) {
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getState(),
                service.getType(),
                service.getPriority(),
                service.getCreatedAt(),
                service.getUpdatedAt(),
                service.getStartedAt(),
                service.getCompletedAt(),
                null, // Les IDs des ressources requises seraient récupérés par une autre requête si nécessaire
                null,
                service.getMaxExecutionTimeMinutes(),
                service.getAutoRetry()
        );
    }
}