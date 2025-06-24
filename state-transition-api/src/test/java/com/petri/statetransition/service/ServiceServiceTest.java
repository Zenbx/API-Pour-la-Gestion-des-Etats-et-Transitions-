package com.petri.statetransition.service;

import com.petri.statetransition.dto.CreateServiceDTO;
import com.petri.statetransition.dto.ServiceDTO;
import com.petri.statetransition.model.entity.Services;
import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.Priority;
import com.petri.statetransition.repository.ServiceRepository;
import com.petri.statetransition.repository.ServiceUnitResourceRepository;
import com.petri.statetransition.repository.ServiceCompositeResourceRepository;
import com.petri.statetransition.exception.ResourceNotFoundException;
import com.petri.statetransition.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ServiceService
 */
@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceUnitResourceRepository serviceUnitResourceRepository;

    @Mock
    private ServiceCompositeResourceRepository serviceCompositeResourceRepository;

    @Mock
    private ResourceAllocationService resourceAllocationService;

    @Mock
    private TransitionService transitionService;

    private ServiceManager serviceService;

    private Services testService;
    private CreateServiceDTO createServiceDTO;

    @BeforeEach
    void setUp() {
        serviceService = new ServiceManager(
                serviceRepository,
                serviceUnitResourceRepository,
                serviceCompositeResourceRepository,
                resourceAllocationService,
                transitionService
        );

        // Service de test
        testService = new Services("Test Service", "Description de test", ServiceType.NON_BLOQUANT, Priority.NORMALE);
        testService.setId(1L);
        testService.setCreatedAt(LocalDateTime.now());
        testService.setUpdatedAt(LocalDateTime.now());

        // DTO de création de test
        createServiceDTO = new CreateServiceDTO(
                "Test Service",
                "Description de test",
                ServiceType.NON_BLOQUANT,
                Priority.NORMALE,
                List.of(1L, 2L), // Unit resources
                List.of(1L),     // Composite resources
                30,              // Max execution time
                false            // Auto retry
        );
    }

    @Test
    void createService_ShouldReturnServiceDTO_WhenValidInput() {
        // Given
        when(serviceRepository.save(any(Services.class))).thenReturn(Mono.just(testService));
        when(serviceUnitResourceRepository.save(any())).thenReturn(Mono.just(new com.petri.statetransition.model.entity.ServiceUnitResource()));
        when(serviceCompositeResourceRepository.save(any())).thenReturn(Mono.just(new com.petri.statetransition.model.entity.ServiceCompositeResource()));

        // When
        Mono<ServiceDTO> result = serviceService.createService(createServiceDTO);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.name().equals("Test Service") &&
                                dto.state() == ServiceState.PLANIFIE &&
                                dto.type() == ServiceType.NON_BLOQUANT &&
                                dto.priority() == Priority.NORMALE
                )
                .verifyComplete();

        verify(serviceRepository).save(any(Services.class));
    }

    @Test
    void findById_ShouldReturnServiceDTO_WhenServiceExists() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));

        // When
        Mono<ServiceDTO> result = serviceService.findById(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.id().equals(1L) &&
                                dto.name().equals("Test Service")
                )
                .verifyComplete();
    }

    @Test
    void findById_ShouldThrowException_WhenServiceNotExists() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Mono.empty());

        // When
        Mono<ServiceDTO> result = serviceService.findById(999L);

        // Then
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void startService_ShouldReturnServiceDTO_WhenServiceIsReady() {
        // Given
        testService.setState(ServiceState.PRET);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(resourceAllocationService.checkResourceAvailability(1L)).thenReturn(Mono.just(true));
        when(resourceAllocationService.allocateResources(1L)).thenReturn(Mono.empty());
        when(transitionService.recordTransition(any(), any(), any(), any())).thenReturn(Mono.just(createMockTransitionDTO()));

        Services startedService = new Services(testService.getName(), testService.getDescription(), testService.getType(), testService.getPriority());
        startedService.setId(1L);
        startedService.setState(ServiceState.EN_COURS);
        startedService.setStartedAt(LocalDateTime.now());

        when(serviceRepository.save(any(Services.class))).thenReturn(Mono.just(startedService));

        // When
        Mono<ServiceDTO> result = serviceService.startService(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.state() == ServiceState.EN_COURS)
                .verifyComplete();

        verify(resourceAllocationService).allocateResources(1L);
    }

    @Test
    void startService_ShouldTransitionToBlocked_WhenResourcesNotAvailable() {
        // Given
        testService.setState(ServiceState.PRET);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(resourceAllocationService.checkResourceAvailability(1L)).thenReturn(Mono.just(false));
        when(transitionService.recordTransition(any(), any(), any(), any())).thenReturn(Mono.just(createMockTransitionDTO()));

        Services blockedService = new Services(testService.getName(), testService.getDescription(), testService.getType(), testService.getPriority());
        blockedService.setId(1L);
        blockedService.setState(ServiceState.BLOQUE);

        when(serviceRepository.save(any(Services.class))).thenReturn(Mono.just(blockedService));

        // When
        Mono<ServiceDTO> result = serviceService.startService(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.state() == ServiceState.BLOQUE)
                .verifyComplete();

        verify(resourceAllocationService, never()).allocateResources(1L);
    }

    @Test
    void startService_ShouldThrowException_WhenServiceNotReady() {
        // Given
        testService.setState(ServiceState.PLANIFIE);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));

        // When
        Mono<ServiceDTO> result = serviceService.startService(1L);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidStateTransitionException.class)
                .verify();
    }

    @Test
    void completeService_ShouldReturnServiceDTO_WhenServiceInProgress() {
        // Given
        testService.setState(ServiceState.EN_COURS);
        testService.setStartedAt(LocalDateTime.now().minusMinutes(10));
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(resourceAllocationService.releaseResources(1L)).thenReturn(Mono.empty());
        when(transitionService.recordTransition(any(), any(), any(), any())).thenReturn(Mono.just(createMockTransitionDTO()));

        Services completedService = new Services(testService.getName(), testService.getDescription(), testService.getType(), testService.getPriority());
        completedService.setId(1L);
        completedService.setState(ServiceState.TERMINE);
        completedService.setStartedAt(testService.getStartedAt());
        completedService.setCompletedAt(LocalDateTime.now());

        when(serviceRepository.save(any(Services.class))).thenReturn(Mono.just(completedService));

        // When
        Mono<ServiceDTO> result = serviceService.completeService(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto ->
                        dto.state() == ServiceState.TERMINE &&
                                dto.completedAt() != null
                )
                .verifyComplete();

        verify(resourceAllocationService).releaseResources(1L);
    }

    @Test
    void completeService_ShouldThrowException_WhenServiceNotInProgress() {
        // Given
        testService.setState(ServiceState.PRET);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));

        // When
        Mono<ServiceDTO> result = serviceService.completeService(1L);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidStateTransitionException.class)
                .verify();
    }

    @Test
    void cancelService_ShouldReturnServiceDTO_WhenServiceCancellable() {
        // Given
        testService.setState(ServiceState.PRET);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));
        when(resourceAllocationService.releaseResources(1L)).thenReturn(Mono.empty());
        when(transitionService.recordTransition(any(), any(), any(), any())).thenReturn(Mono.just(createMockTransitionDTO()));

        Services cancelledService = new Services(testService.getName(), testService.getDescription(), testService.getType(), testService.getPriority());
        cancelledService.setId(1L);
        cancelledService.setState(ServiceState.ANNULE);
        cancelledService.setCompletedAt(LocalDateTime.now());

        when(serviceRepository.save(any(Services.class))).thenReturn(Mono.just(cancelledService));

        // When
        Mono<ServiceDTO> result = serviceService.cancelService(1L);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.state() == ServiceState.ANNULE)
                .verifyComplete();

        verify(resourceAllocationService).releaseResources(1L);
    }

    @Test
    void cancelService_ShouldThrowException_WhenServiceInFinalState() {
        // Given
        testService.setState(ServiceState.TERMINE);
        when(serviceRepository.findById(1L)).thenReturn(Mono.just(testService));

        // When
        Mono<ServiceDTO> result = serviceService.cancelService(1L);

        // Then
        StepVerifier.create(result)
                .expectError(InvalidStateTransitionException.class)
                .verify();
    }

    // Méthodes utilitaires pour les tests

    private com.petri.statetransition.dto.TransitionDTO createMockTransitionDTO() {
        return new com.petri.statetransition.dto.TransitionDTO(
                1L,
                com.petri.statetransition.model.enums.TransitionType.NORMALE,
                com.petri.statetransition.model.enums.TransitionStatus.TERMINEE,
                "Test Transition",
                "Description de test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(1L),
                null,
                null,
                null,
                null
        );
    }
}