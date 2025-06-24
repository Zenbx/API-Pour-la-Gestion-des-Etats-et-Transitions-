package com.petri.statetransition.service;

import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import com.petri.statetransition.repository.*;
import com.petri.statetransition.exception.ResourceAllocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

/**
 * Service pour la gestion de l'allocation des ressources selon le modèle de réseaux de Petri
 */
@Service
@Transactional
public class ResourceAllocationService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceAllocationService.class);

    private final ServiceRepository serviceRepository;
    private final UnitResourceRepository unitResourceRepository;
    private final CompositeResourceRepository compositeResourceRepository;
    private final ServiceUnitResourceRepository serviceUnitResourceRepository;
    private final ServiceCompositeResourceRepository serviceCompositeResourceRepository;
    private final CompositeUnitResourceRepository compositeUnitResourceRepository;

    public ResourceAllocationService(
            ServiceRepository serviceRepository,
            UnitResourceRepository unitResourceRepository,
            CompositeResourceRepository compositeResourceRepository,
            ServiceUnitResourceRepository serviceUnitResourceRepository,
            ServiceCompositeResourceRepository serviceCompositeResourceRepository,
            CompositeUnitResourceRepository compositeUnitResourceRepository) {
        this.serviceRepository = serviceRepository;
        this.unitResourceRepository = unitResourceRepository;
        this.compositeResourceRepository = compositeResourceRepository;
        this.serviceUnitResourceRepository = serviceUnitResourceRepository;
        this.serviceCompositeResourceRepository = serviceCompositeResourceRepository;
        this.compositeUnitResourceRepository = compositeUnitResourceRepository;
    }

    /**
     * Vérifie la disponibilité des ressources pour un service selon son type
     */
    public Mono<Boolean> checkResourceAvailability(Long serviceId) {
        logger.debug("Vérification de la disponibilité des ressources pour le service ID: {}", serviceId);

        return serviceRepository.findById(serviceId)
                .flatMap(service -> {
                    if (service.getType() == ServiceType.BLOQUANT) {
                        return checkAllResourcesAvailable(serviceId);
                    } else {
                        return checkAtLeastOneResourceAvailable(serviceId);
                    }
                })
                .defaultIfEmpty(false);
    }

    /**
     * Alloue toutes les ressources requises pour un service
     */
    public Mono<Void> allocateResources(Long serviceId) {
        logger.info("Allocation des ressources pour le service ID: {}", serviceId);

        return checkResourceAvailability(serviceId)
                .flatMap(available -> {
                    if (!available) {
                        return Mono.error(new ResourceAllocationException(
                                "Ressources insuffisantes pour allouer au service ID: " + serviceId
                        ));
                    }

                    return allocateUnitResources(serviceId)
                            .then(allocateCompositeResources(serviceId));
                })
                .doOnSuccess(v -> logger.info("Ressources allouées avec succès pour le service ID: {}", serviceId))
                .doOnError(error -> logger.error("Erreur lors de l'allocation des ressources pour le service ID: {}", serviceId, error));
    }

    /**
     * Libère toutes les ressources allouées à un service
     */
    public Mono<Void> releaseResources(Long serviceId) {
        logger.info("Libération des ressources pour le service ID: {}", serviceId);

        return releaseUnitResources(serviceId)
                .then(releaseCompositeResources(serviceId))
                .doOnSuccess(v -> logger.info("Ressources libérées avec succès pour le service ID: {}", serviceId))
                .doOnError(error -> logger.error("Erreur lors de la libération des ressources pour le service ID: {}", serviceId, error));
    }

    /**
     * Vérifie si toutes les ressources requises sont disponibles (pour services BLOQUANT)
     */
    private Mono<Boolean> checkAllResourcesAvailable(Long serviceId) {
        return Mono.zip(
                checkAllUnitResourcesAvailable(serviceId),
                checkAllCompositeResourcesAvailable(serviceId)
        ).map(tuple -> tuple.getT1() && tuple.getT2());
    }

    /**
     * Vérifie si au moins une ressource est disponible (pour services NON_BLOQUANT)
     */
    private Mono<Boolean> checkAtLeastOneResourceAvailable(Long serviceId) {
        return Mono.zip(
                checkAnyUnitResourceAvailable(serviceId),
                checkAnyCompositeResourceAvailable(serviceId)
        ).map(tuple -> tuple.getT1() || tuple.getT2());
    }

    /**
     * Vérifie si toutes les ressources unitaires requises sont disponibles
     */
    private Mono<Boolean> checkAllUnitResourcesAvailable(Long serviceId) {
        return serviceUnitResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .map(resource -> resource.getState() == UnitResourceState.LIBRE)
                )
                .all(available -> available)
                .defaultIfEmpty(true); // Si aucune ressource unitaire requise, considérer comme disponible
    }

    /**
     * Vérifie si toutes les ressources composites requises sont disponibles
     */
    private Mono<Boolean> checkAllCompositeResourcesAvailable(Long serviceId) {
        return serviceCompositeResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        compositeResourceRepository.findById(association.getCompositeResourceId())
                                .flatMap(this::checkCompositeResourceAvailable)
                )
                .all(available -> available)
                .defaultIfEmpty(true); // Si aucune ressource composite requise, considérer comme disponible
    }

    /**
     * Vérifie si au moins une ressource unitaire est disponible
     */
    private Mono<Boolean> checkAnyUnitResourceAvailable(Long serviceId) {
        return serviceUnitResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .map(resource -> resource.getState() == UnitResourceState.LIBRE)
                )
                .any(available -> available)
                .defaultIfEmpty(false);
    }

    /**
     * Vérifie si au moins une ressource composite est disponible
     */
    private Mono<Boolean> checkAnyCompositeResourceAvailable(Long serviceId) {
        return serviceCompositeResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        compositeResourceRepository.findById(association.getCompositeResourceId())
                                .flatMap(this::checkCompositeResourceAvailable)
                )
                .any(available -> available)
                .defaultIfEmpty(false);
    }

    /**
     * Vérifie si une ressource composite est disponible (elle et tous ses composants)
     */
    private Mono<Boolean> checkCompositeResourceAvailable(com.petri.statetransition.model.entity.CompositeResource compositeResource) {
        if (compositeResource.getState() != CompositeResourceState.VIDE) {
            return Mono.just(false);
        }

        return compositeUnitResourceRepository.findByCompositeResourceId(compositeResource.getId())
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .map(resource -> resource.getState() == UnitResourceState.LIBRE)
                )
                .all(available -> available)
                .defaultIfEmpty(true);
    }

    /**
     * Alloue toutes les ressources unitaires requises par un service
     */
    private Mono<Void> allocateUnitResources(Long serviceId) {
        return serviceUnitResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .flatMap(resource -> {
                                    if (resource.getState() != UnitResourceState.LIBRE) {
                                        return Mono.error(new ResourceAllocationException(
                                                "Ressource unitaire ID " + resource.getId() + " n'est pas disponible"
                                        ));
                                    }

                                    resource.transitionTo(UnitResourceState.AFFECTE);
                                    return unitResourceRepository.save(resource);
                                })
                )
                .then();
    }

    /**
     * Alloue toutes les ressources composites requises par un service
     */
    private Mono<Void> allocateCompositeResources(Long serviceId) {
        return serviceCompositeResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        compositeResourceRepository.findById(association.getCompositeResourceId())
                                .flatMap(this::allocateCompositeResource)
                )
                .then();
    }

    /**
     * Alloue une ressource composite (réservation en cours puis prêt)
     */
    private Mono<com.petri.statetransition.model.entity.CompositeResource> allocateCompositeResource(
            com.petri.statetransition.model.entity.CompositeResource compositeResource) {

        if (compositeResource.getState() != CompositeResourceState.VIDE) {
            return Mono.error(new ResourceAllocationException(
                    "Ressource composite ID " + compositeResource.getId() + " n'est pas disponible"
            ));
        }

        // Transition VIDE -> EN_COURS_RESERVATION
        compositeResource.transitionTo(CompositeResourceState.EN_COURS_RESERVATION);
        return compositeResourceRepository.save(compositeResource)
                .flatMap(savedResource ->
                        // Allouer tous les composants unitaires
                        compositeUnitResourceRepository.findByCompositeResourceId(savedResource.getId())
                                .flatMap(association ->
                                        unitResourceRepository.findById(association.getUnitResourceId())
                                                .flatMap(unitResource -> {
                                                    if (unitResource.getState() != UnitResourceState.LIBRE) {
                                                        return Mono.error(new ResourceAllocationException(
                                                                "Composant unitaire ID " + unitResource.getId() + " n'est pas disponible"
                                                        ));
                                                    }

                                                    unitResource.transitionTo(UnitResourceState.AFFECTE);
                                                    return unitResourceRepository.save(unitResource);
                                                })
                                )
                                .then(Mono.defer(() -> {
                                    // Transition EN_COURS_RESERVATION -> PRET
                                    savedResource.transitionTo(CompositeResourceState.PRET);
                                    return compositeResourceRepository.save(savedResource);
                                }))
                );
    }

    /**
     * Libère toutes les ressources unitaires allouées à un service
     */
    private Mono<Void> releaseUnitResources(Long serviceId) {
        return serviceUnitResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .flatMap(resource -> {
                                    if (resource.getState().canBeReleased()) {
                                        resource.transitionTo(UnitResourceState.LIBRE);
                                        return unitResourceRepository.save(resource);
                                    }
                                    return Mono.just(resource);
                                })
                )
                .then();
    }

    /**
     * Libère toutes les ressources composites allouées à un service
     */
    private Mono<Void> releaseCompositeResources(Long serviceId) {
        return serviceCompositeResourceRepository.findByServiceId(serviceId)
                .flatMap(association ->
                        compositeResourceRepository.findById(association.getCompositeResourceId())
                                .flatMap(this::releaseCompositeResource)
                )
                .then();
    }

    /**
     * Libère une ressource composite et tous ses composants
     */
    private Mono<com.petri.statetransition.model.entity.CompositeResource> releaseCompositeResource(
            com.petri.statetransition.model.entity.CompositeResource compositeResource) {

        // Libérer tous les composants unitaires d'abord
        return compositeUnitResourceRepository.findByCompositeResourceId(compositeResource.getId())
                .flatMap(association ->
                        unitResourceRepository.findById(association.getUnitResourceId())
                                .flatMap(unitResource -> {
                                    if (unitResource.getState().canBeReleased()) {
                                        unitResource.transitionTo(UnitResourceState.LIBRE);
                                        return unitResourceRepository.save(unitResource);
                                    }
                                    return Mono.just(unitResource);
                                })
                )
                .then(Mono.defer(() -> {
                    // Puis libérer la ressource composite
                    if (compositeResource.getState() == CompositeResourceState.PRET ||
                            compositeResource.getState() == CompositeResourceState.AFFECTE) {
                        compositeResource.transitionTo(CompositeResourceState.VIDE);
                        return compositeResourceRepository.save(compositeResource);
                    }
                    return Mono.just(compositeResource);
                }));
    }

    /**
     * Force la libération de toutes les ressources d'un service (pour nettoyage)
     */
    public Mono<Void> forceReleaseResources(Long serviceId) {
        logger.warn("Libération forcée des ressources pour le service ID: {}", serviceId);

        return releaseResources(serviceId)
                .onErrorResume(error -> {
                    logger.error("Erreur lors de la libération forcée, continuons quand même", error);
                    return Mono.empty();
                });
    }

    /**
     * Obtient un rapport sur l'utilisation des ressources
     */
    public Mono<ResourceUtilizationReport> getResourceUtilizationReport() {
        return Mono.zip(
                unitResourceRepository.getResourceCountByState().collectList(),
                compositeResourceRepository.getResourceCountByState().collectList()
        ).map(tuple -> new ResourceUtilizationReport(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Classe pour le rapport d'utilisation des ressources
     */
    public static class ResourceUtilizationReport {
        private final java.util.List<UnitResourceRepository.UnitResourceStateCount> unitResourceCounts;
        private final java.util.List<CompositeResourceRepository.CompositeResourceStateCount> compositeResourceCounts;

        public ResourceUtilizationReport(
                java.util.List<UnitResourceRepository.UnitResourceStateCount> unitResourceCounts,
                java.util.List<CompositeResourceRepository.CompositeResourceStateCount> compositeResourceCounts) {
            this.unitResourceCounts = unitResourceCounts;
            this.compositeResourceCounts = compositeResourceCounts;
        }

        public java.util.List<UnitResourceRepository.UnitResourceStateCount> getUnitResourceCounts() {
            return unitResourceCounts;
        }

        public java.util.List<CompositeResourceRepository.CompositeResourceStateCount> getCompositeResourceCounts() {
            return compositeResourceCounts;
        }
    }
}