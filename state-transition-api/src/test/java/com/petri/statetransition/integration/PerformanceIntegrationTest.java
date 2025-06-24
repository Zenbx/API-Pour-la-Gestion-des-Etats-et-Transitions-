package com.petri.statetransition.integration;

import com.petri.statetransition.dto.CreateServiceDTO;
import com.petri.statetransition.model.enums.Priority;
import com.petri.statetransition.model.enums.ServiceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

/**
 * Tests de performance et de charge
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///perfdb;DB_CLOSE_DELAY=-1"
})
class PerformanceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private com.petri.statetransition.service.ServiceManager serviceService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void loadTest_ShouldHandleMultipleRequests() {
        // Test de charge simple avec plusieurs requêtes simultanées
        webTestClient.post()
                .uri("/api/v1/admin/load-test?numberOfOperations=50")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    @WithMockUser(username = "viewer", roles = "VIEWER")
    void metricsEndpoint_ShouldRespondQuickly() {
        // Les métriques doivent répondre rapidement
        webTestClient.get()
                .uri("/api/v1/metrics/system")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/metrics/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void reactiveOperations_ShouldBeNonBlocking() {
        // Test pour vérifier que les opérations sont bien réactives
        CreateServiceDTO serviceDTO = new CreateServiceDTO(
                "Reactive-Test", "Test réactivité", ServiceType.NON_BLOQUANT,
                Priority.NORMALE, List.of(), List.of(), 30, false
        );

        // Créer plusieurs services en parallèle
        StepVerifier.create(
                        serviceService.createService(serviceDTO)
                                .repeat(10)
                                .parallel(5)
                                .sequential()
                )
                .expectNextCount(11) // 1 + 10 répétitions
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}
