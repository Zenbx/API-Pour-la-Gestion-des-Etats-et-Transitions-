package com.petri.statetransition.service;

import com.petri.statetransition.model.entity.Services;
import com.petri.statetransition.model.entity.UnitResource;
import com.petri.statetransition.model.entity.CompositeResource;
import com.petri.statetransition.model.entity.ServiceUnitResource;
import com.petri.statetransition.model.entity.ServiceCompositeResource;
import com.petri.statetransition.model.entity.CompositeUnitResource;
import com.petri.statetransition.model.enums.*;
import com.petri.statetransition.repository.*;
import com.petri.statetransition.exception.ResourceAllocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ResourceAllocationService
 */
@ExtendWith(MockitoExtension.class)
class ResourceAllocationServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UnitResourceRepository unitResourceRepository;

    @Mock
    private CompositeResourceRepository compositeResourceRepository;

    @Mock
    private ServiceUnitResourceRepository serviceUnitResourceRepository;

    @Mock
    private ServiceCompositeResourceRepository serviceCompositeResourceRepository;

    @Mock
    private CompositeUnitResourceRepository compositeUnitResourceRepository;

    private ResourceAllocationService resourceAllocationService;

    private Services testService;
    private UnitResource unitResource1, unitResource2;
    private CompositeResource compositeResource;
    private ServiceUnitResource serviceUnitResource1, serviceUnitResource2;
    private ServiceCompositeResource serviceCompositeResource;
    private CompositeUnitResource compositeUnitResource1, compositeUnitResource2;

    @BeforeEach
    void setUp() {
        resourceAllocationService = new ResourceAllocationService(
                serviceRepository,
                unitResourceRepository,
                compositeResourceRepository,
                serviceUnitResourceRepository,
                serviceCompositeResourceRepository,
                compositeUnitResourceRepository
        );

        setupTestData();
    }

    private void setupTestData() {
        // Service de test
        testService = new Services("Test Service", "Description", ServiceType.BLOQUANT, Priority.NORMALE);
        testService.setId(1L);
        testService.setState(ServiceState.PRET);

        // Ressources unitaires
        unitResource1 = new UnitResource("CPU-01", "Processeur test");
        unitResource1.setId(1L);
        unitResource1.setState(UnitResourceState.LIBRE);

        unitResource2 = new UnitResource("Memory-01", "Mémoire test");
        unitResource2.setId(2L);
        unitResource2.setState(UnitResourceState.LIBRE);

        // Ressource composite
        compositeResource = new CompositeResource("Server-01", "Serveur test");
        compositeResource.setId(1L);
        compositeResource.setState(CompositeResourceState.VIDE);

        // Relations service-ressources
        serviceUnitResource1 = new ServiceUnitResource(1L, 1L, true);
        serviceUnitResource2 = new ServiceUnitResource(1L, 2L, true);
        serviceCompositeResource = new ServiceCompositeResource(1L, 1L, true);

        // Relations composite-unit
        compositeUnitResource1 = new CompositeUnitResource(1L, 1L, true);
        compositeUnitResource2 = new CompositeUnitResource(1L, 2L, true);
    }

    @Test
    void checkResourceAvailability_ShouldReturnTrue_WhenAllResourcesAvailableForBloquantService() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceUnitResource1, serviceUnitResource2));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceCompositeResource));
        when(unitResourceRepository.findById(1L)).thenReturn(Mono.just(unitResource1));
        when(unitResourceRepository.findById(2L)).thenReturn(Mono.just(unitResource2));
        when(compositeResourceRepository.findById(1L)).thenReturn(Mono.just(compositeResource));
        when(compositeUnitResourceRepository.findByCompositeResourceId(1L))
                .thenReturn(Flux.just(compositeUnitResource1, compositeUnitResource2));

        // When
        Mono<Boolean> result = resourceAllocationService.checkResourceAvailability(1L);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkResourceAvailability_ShouldReturnFalse_WhenUnitResourceNotAvailable() {
        // Given
        unitResource1.setState(UnitResourceState.OCCUPE); // Pas disponible

        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceUnitResource1, serviceUnitResource2));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceCompositeResource));
        when(unitResourceRepository.findById(1L)).thenReturn(Mono.just(unitResource1));
        when(unitResourceRepository.findById(2L)).thenReturn(Mono.just(unitResource2));

        // When
        Mono<Boolean> result = resourceAllocationService.checkResourceAvailability(1L);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkResourceAvailability_ShouldReturnTrue_WhenAtLeastOneResourceAvailableForNonBloquantService() {
        // Given
        testService.setType(ServiceType.NON_BLOQUANT);
        unitResource1.setState(UnitResourceState.OCCUPE); // Pas disponible
        // unitResource2 reste LIBRE

        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceUnitResource1, serviceUnitResource2));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.empty()); // Pas de ressources composites
        when(unitResourceRepository.findById(1L)).thenReturn(Mono.just(unitResource1));
        when(unitResourceRepository.findById(2L)).thenReturn(Mono.just(unitResource2));

        // When
        Mono<Boolean> result = resourceAllocationService.checkResourceAvailability(1L);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void allocateResources_ShouldAllocateAllResources_WhenAvailable() {
        // Given
        when(resourceAllocationService.checkResourceAvailability(1L)).thenReturn(Mono.just(true));
        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceUnitResource1, serviceUnitResource2));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceCompositeResource));
        when(unitResourceRepository.findById(1L)).thenReturn(Mono.just(unitResource1));
        when(unitResourceRepository.findById(2L)).thenReturn(Mono.just(unitResource2));
        when(compositeResourceRepository.findById(1L)).thenReturn(Mono.just(compositeResource));
        when(compositeUnitResourceRepository.findByCompositeResourceId(1L))
                .thenReturn(Flux.just(compositeUnitResource1, compositeUnitResource2));

        // Simulations des sauvegardes
        UnitResource allocatedUnit1 = new UnitResource(unitResource1.getName(), unitResource1.getDescription());
        allocatedUnit1.setId(1L);
        allocatedUnit1.setState(UnitResourceState.AFFECTE);

        UnitResource allocatedUnit2 = new UnitResource(unitResource2.getName(), unitResource2.getDescription());
        allocatedUnit2.setId(2L);
        allocatedUnit2.setState(UnitResourceState.AFFECTE);

        CompositeResource allocatedComposite = new CompositeResource(compositeResource.getName(), compositeResource.getDescription());
        allocatedComposite.setId(1L);
        allocatedComposite.setState(CompositeResourceState.PRET);

        when(unitResourceRepository.save(any(UnitResource.class)))
                .thenReturn(Mono.just(allocatedUnit1))
                .thenReturn(Mono.just(allocatedUnit2));
        when(compositeResourceRepository.save(any(CompositeResource.class)))
                .thenReturn(Mono.just(allocatedComposite));

        // When
        Mono<Void> result = resourceAllocationService.allocateResources(1L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(unitResourceRepository, times(2)).save(any(UnitResource.class));
        verify(compositeResourceRepository, atLeastOnce()).save(any(CompositeResource.class));
    }

    @Test
    void allocateResources_ShouldThrowException_WhenResourcesNotAvailable() {
        // Given
        when(resourceAllocationService.checkResourceAvailability(1L)).thenReturn(Mono.just(false));

        // When
        Mono<Void> result = resourceAllocationService.allocateResources(1L);

        // Then
        StepVerifier.create(result)
                .expectError(ResourceAllocationException.class)
                .verify();
    }

    @Test
    void releaseResources_ShouldReleaseAllResources() {
        // Given
        UnitResource allocatedUnit1 = new UnitResource(unitResource1.getName(), unitResource1.getDescription());
        allocatedUnit1.setId(1L);
        allocatedUnit1.setState(UnitResourceState.AFFECTE);

        UnitResource allocatedUnit2 = new UnitResource(unitResource2.getName(), unitResource2.getDescription());
        allocatedUnit2.setId(2L);
        allocatedUnit2.setState(UnitResourceState.OCCUPE);

        CompositeResource allocatedComposite = new CompositeResource(compositeResource.getName(), compositeResource.getDescription());
        allocatedComposite.setId(1L);
        allocatedComposite.setState(CompositeResourceState.AFFECTE);

        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceUnitResource1, serviceUnitResource2));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.just(serviceCompositeResource));
        when(unitResourceRepository.findById(1L)).thenReturn(Mono.just(allocatedUnit1));
        when(unitResourceRepository.findById(2L)).thenReturn(Mono.just(allocatedUnit2));
        when(compositeResourceRepository.findById(1L)).thenReturn(Mono.just(allocatedComposite));
        when(compositeUnitResourceRepository.findByCompositeResourceId(1L))
                .thenReturn(Flux.just(compositeUnitResource1, compositeUnitResource2));

        // Simulations des libérations
        UnitResource releasedUnit1 = new UnitResource(allocatedUnit1.getName(), allocatedUnit1.getDescription());
        releasedUnit1.setId(1L);
        releasedUnit1.setState(UnitResourceState.LIBRE);

        UnitResource releasedUnit2 = new UnitResource(allocatedUnit2.getName(), allocatedUnit2.getDescription());
        releasedUnit2.setId(2L);
        releasedUnit2.setState(UnitResourceState.LIBRE);

        CompositeResource releasedComposite = new CompositeResource(allocatedComposite.getName(), allocatedComposite.getDescription());
        releasedComposite.setId(1L);
        releasedComposite.setState(CompositeResourceState.VIDE);

        when(unitResourceRepository.save(any(UnitResource.class)))
                .thenReturn(Mono.just(releasedUnit1))
                .thenReturn(Mono.just(releasedUnit2));
        when(compositeResourceRepository.save(any(CompositeResource.class)))
                .thenReturn(Mono.just(releasedComposite));

        // When
        Mono<Void> result = resourceAllocationService.releaseResources(1L);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(unitResourceRepository, times(2)).save(any(UnitResource.class));
        verify(compositeResourceRepository, atLeastOnce()).save(any(CompositeResource.class));
    }

    @Test
    void forceReleaseResources_ShouldCompleteEvenOnError() {
        // Given
        when(serviceUnitResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.error(new RuntimeException("Test error")));
        when(serviceCompositeResourceRepository.findByServiceId(1L))
                .thenReturn(Flux.empty());

        // When
        Mono<Void> result = resourceAllocationService.forceReleaseResources(1L);

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Doit se terminer même avec erreur
    }
}