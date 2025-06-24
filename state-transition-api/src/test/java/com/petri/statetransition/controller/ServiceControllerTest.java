package com.petri.statetransition.controller;

import com.petri.statetransition.dto.CreateServiceDTO;
import com.petri.statetransition.dto.ServiceDTO;
import com.petri.statetransition.dto.ApiResponse;
import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.Priority;
import com.petri.statetransition.service.ServiceManager;
import com.petri.statetransition.exception.ResourceNotFoundException;
import com.petri.statetransition.exception.InvalidStateTransitionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour ServiceController
 */
@WebFluxTest(ServiceController.class)
@Import({com.petri.statetransition.config.SecurityConfig.class})
class ServiceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ServiceManager serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceDTO testServiceDTO;
    private CreateServiceDTO createServiceDTO;

    @BeforeEach
    void setUp() {
        testServiceDTO = new ServiceDTO(
                1L,
                "Test Service",
                "Description de test",
                ServiceState.PLANIFIE,
                ServiceType.NON_BLOQUANT,
                Priority.NORMALE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                List.of(1L, 2L),
                List.of(1L),
                30,
                false
        );

        createServiceDTO = new CreateServiceDTO(
                "Test Service",
                "Description de test",
                ServiceType.NON_BLOQUANT,
                Priority.NORMALE,
                List.of(1L, 2L),
                List.of(1L),
                30,
                false
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createService_ShouldReturnCreated_WhenValidInput() throws Exception {
        // Given
        when(serviceService.createService(any(CreateServiceDTO.class)))
                .thenReturn(Mono.just(testServiceDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createServiceDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.name").isEqualTo("Test Service")
                .jsonPath("$.data.state").isEqualTo("PLANIFIE")
                .jsonPath("$.data.type").isEqualTo("NON_BLOQUANT");
    }

    @Test
    @WithMockUser(roles = "USER")
    void createService_ShouldReturnCreated_WhenUserRole() throws Exception {
        // Given
        when(serviceService.createService(any(CreateServiceDTO.class)))
                .thenReturn(Mono.just(testServiceDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createServiceDTO))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createService_ShouldReturnForbidden_WhenViewerRole() throws Exception {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createServiceDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createService_ShouldReturnUnauthorized_WhenNoAuth() throws Exception {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createServiceDTO))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getServiceById_ShouldReturnService_WhenExists() {
        // Given
        when(serviceService.findById(1L)).thenReturn(Mono.just(testServiceDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.name").isEqualTo("Test Service");
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getServiceById_ShouldReturnNotFound_WhenNotExists() {
        // Given
        when(serviceService.findById(999L))
                .thenReturn(Mono.error(new ResourceNotFoundException("Service non trouvé")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").exists();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getAllServices_ShouldReturnAllServices() {
        // Given
        when(serviceService.findAll()).thenReturn(Flux.just(testServiceDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").exists();
    }

    @Test
    @WithMockUser(roles = "USER")
    void startService_ShouldReturnOk_WhenServiceCanBeStarted() {
        // Given
        ServiceDTO startedService = new ServiceDTO(
                testServiceDTO.id(),
                testServiceDTO.name(),
                testServiceDTO.description(),
                ServiceState.EN_COURS, // État changé
                testServiceDTO.type(),
                testServiceDTO.priority(),
                testServiceDTO.createdAt(),
                testServiceDTO.updatedAt(),
                LocalDateTime.now(), // Started at
                testServiceDTO.completedAt(),
                testServiceDTO.requiredUnitResourceIds(),
                testServiceDTO.requiredCompositeResourceIds(),
                testServiceDTO.maxExecutionTimeMinutes(),
                testServiceDTO.autoRetry()
        );

        when(serviceService.startService(1L)).thenReturn(Mono.just(startedService));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services/1/start")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.state").isEqualTo("EN_COURS");
    }

    @Test
    @WithMockUser(roles = "USER")
    void startService_ShouldReturnBadRequest_WhenInvalidStateTransition() {
        // Given
        when(serviceService.startService(1L))
                .thenReturn(Mono.error(new InvalidStateTransitionException("Transition invalide")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/services/1/start")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteService_ShouldReturnOk_WhenAdminRole() {
        // Given
        when(serviceService.deleteService(1L)).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/v1/services/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteService_ShouldReturnForbidden_WhenUserRole() {
        // When & Then
        webTestClient.delete()
                .uri("/api/v1/services/1")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getServicesByState_ShouldReturnFilteredServices() {
        // Given
        when(serviceService.findByState(ServiceState.PLANIFIE))
                .thenReturn(Flux.just(testServiceDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/services/state/PLANIFIE")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").exists();
    }
}

