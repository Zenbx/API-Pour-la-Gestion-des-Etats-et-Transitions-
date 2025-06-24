package com.petri.statetransition.controller;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration pour les endpoints des ressources
 */
@WebFluxTest(UnitResourceController.class)
@Import({com.petri.statetransition.config.SecurityConfig.class})
class UnitResourceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private com.petri.statetransition.service.UnitResourceService unitResourceService;

    @Autowired
    private ObjectMapper objectMapper;

    private com.petri.statetransition.dto.UnitResourceDTO testResourceDTO;
    private com.petri.statetransition.dto.CreateUnitResourceDTO createResourceDTO;

    @BeforeEach
    void setUp() {
        testResourceDTO = new com.petri.statetransition.dto.UnitResourceDTO(
                1L,
                "CPU-01",
                "Processeur de test",
                com.petri.statetransition.model.enums.UnitResourceState.LIBRE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                "Datacenter-A",
                100,
                0
        );

        createResourceDTO = new com.petri.statetransition.dto.CreateUnitResourceDTO(
                "CPU-01",
                "Processeur de test",
                "Datacenter-A",
                100
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUnitResource_ShouldReturnCreated_WhenAdminRole() throws Exception {
        // Given
        when(unitResourceService.createUnitResource(any()))
                .thenReturn(Mono.just(testResourceDTO));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/unit-resources")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createResourceDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.name").isEqualTo("CPU-01");
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUnitResource_ShouldReturnForbidden_WhenUserRole() throws Exception {
        // When & Then
        webTestClient.post()
                .uri("/api/v1/unit-resources")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createResourceDTO))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getAvailableResources_ShouldReturnResources() {
        // Given
        when(unitResourceService.findAvailableResources())
                .thenReturn(Flux.just(testResourceDTO));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/unit-resources/available")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    @WithMockUser(roles = "USER")
    void allocateResource_ShouldReturnOk_WhenUserRole() {
        // Given
        com.petri.statetransition.dto.UnitResourceDTO allocatedResource =
                new com.petri.statetransition.dto.UnitResourceDTO(
                        testResourceDTO.id(),
                        testResourceDTO.name(),
                        testResourceDTO.description(),
                        com.petri.statetransition.model.enums.UnitResourceState.AFFECTE,
                        testResourceDTO.createdAt(),
                        testResourceDTO.updatedAt(),
                        testResourceDTO.lastUsedAt(),
                        testResourceDTO.location(),
                        testResourceDTO.capacity(),
                        testResourceDTO.currentLoad()
                );

        when(unitResourceService.allocateResource(1L))
                .thenReturn(Mono.just(allocatedResource));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/unit-resources/1/allocate")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.state").isEqualTo("AFFECTE");
    }
}
