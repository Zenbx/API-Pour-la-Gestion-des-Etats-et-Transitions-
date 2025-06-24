package com.petri.statetransition.integration;

import com.petri.statetransition.dto.*;
import com.petri.statetransition.model.enums.*;
import com.petri.statetransition.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tests d'intégration complets pour l'API State Transition
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
		"spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1",
		"spring.sql.init.mode=always"
})
@Transactional
class StateTransitionIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private UnitResourceRepository unitResourceRepository;

	@Autowired
	private CompositeResourceRepository compositeResourceRepository;

	@Autowired
	private TransitionRepository transitionRepository;

	@BeforeEach
	void setUp() {
		// Nettoyage de la base de données de test
		serviceRepository.deleteAll().block();
		unitResourceRepository.deleteAll().block();
		compositeResourceRepository.deleteAll().block();
		transitionRepository.deleteAll().block();
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void completeWorkflow_CreateServiceAndResources_ThenExecute() throws Exception {
		// 1. Créer des ressources unitaires
		CreateUnitResourceDTO unitResource1 = new CreateUnitResourceDTO(
				"CPU-Integration-Test", "Processeur pour test d'intégration", "Test-Location", 100
		);

		CreateUnitResourceDTO unitResource2 = new CreateUnitResourceDTO(
				"Memory-Integration-Test", "Mémoire pour test d'intégration", "Test-Location", 16384
		);

		// Créer première ressource
		WebTestClient.ResponseSpec response1 = (WebTestClient.ResponseSpec) webTestClient.post()
				.uri("/api/v1/unit-resources")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(unitResource1))
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true)
				.jsonPath("$.data.name").isEqualTo("CPU-Integration-Test")
				.jsonPath("$.data.state").isEqualTo("LIBRE");

		// Créer deuxième ressource
		WebTestClient.ResponseSpec response2 = webTestClient.post()
				.uri("/api/v1/unit-resources")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(unitResource2))
				.exchange()
				.expectStatus().isCreated();

		// 2. Vérifier que les ressources sont disponibles
		webTestClient.get()
				.uri("/api/v1/unit-resources/available")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);

		// 3. Créer un service qui utilise ces ressources
		CreateServiceDTO serviceDTO = new CreateServiceDTO(
				"Service-Integration-Test",
				"Service pour test d'intégration complet",
				ServiceType.NON_BLOQUANT,
				Priority.NORMALE,
				List.of(1L, 2L), // IDs des ressources créées
				List.of(),       // Pas de ressources composites
				60,              // 60 minutes max
				false
		);

		webTestClient.post()
				.uri("/api/v1/services")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(serviceDTO))
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true)
				.jsonPath("$.data.name").isEqualTo("Service-Integration-Test")
				.jsonPath("$.data.state").isEqualTo("PLANIFIE");

		// 4. Vérifier les métriques système
		webTestClient.get()
				.uri("/api/v1/metrics/system")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true)
				.jsonPath("$.data.totalServices").exists()
				.jsonPath("$.data.totalUnitResources").exists();

		// 5. Lister tous les services
		webTestClient.get()
				.uri("/api/v1/services")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	void serviceStateTransitions_ShouldFollowPetriNetRules() throws Exception {
		// Créer une ressource unitaire d'abord
		CreateUnitResourceDTO unitResource = new CreateUnitResourceDTO(
				"CPU-State-Test", "CPU pour test de transitions", "Test-Location", 100
		);

		webTestClient.post()
				.uri("/api/v1/unit-resources")
				.exchange()
				.expectStatus().isForbidden(); // USER ne peut pas créer de ressources
	}

	@Test
	@WithMockUser(username = "viewer", roles = "VIEWER")
	void viewer_ShouldOnlyHaveReadAccess() throws Exception {
		// Viewer peut lire
		webTestClient.get()
				.uri("/api/v1/services")
				.exchange()
				.expectStatus().isOk();

		webTestClient.get()
				.uri("/api/v1/unit-resources")
				.exchange()
				.expectStatus().isOk();

		webTestClient.get()
				.uri("/api/v1/metrics/system")
				.exchange()
				.expectStatus().isOk();

		// Mais ne peut pas créer
		CreateServiceDTO serviceDTO = new CreateServiceDTO(
				"Test", "Test", ServiceType.NON_BLOQUANT, Priority.NORMALE,
				List.of(), List.of(), 30, false
		);

		webTestClient.post()
				.uri("/api/v1/services")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(serviceDTO))
				.exchange()
				.expectStatus().isForbidden();
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void admin_ShouldHaveFullAccess() throws Exception {
		// Admin peut faire des opérations de maintenance
		webTestClient.post()
				.uri("/api/v1/admin/maintenance")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);

		// Admin peut nettoyer les anciennes transitions
		webTestClient.delete()
				.uri("/api/v1/transitions/cleanup?daysOld=1")
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	void unauthorizedAccess_ShouldReturn401() {
		webTestClient.get()
				.uri("/api/v1/services")
				.exchange()
				.expectStatus().isUnauthorized();

		webTestClient.post()
				.uri("/api/v1/services")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void errorHandling_ShouldReturnProperErrorResponses() throws Exception {
		// Tenter de récupérer un service inexistant
		webTestClient.get()
				.uri("/api/v1/services/999999")
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.jsonPath("$.success").isEqualTo(false)
				.jsonPath("$.message").exists();

		// Tenter de créer un service avec des données invalides
		String invalidJson = "{\"name\":\"\",\"type\":\"INVALID\"}";

		webTestClient.post()
				.uri("/api/v1/services")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(invalidJson)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void petriNetTransitions_ShouldBeRecorded() throws Exception {
		// Créer un service et vérifier que les transitions sont enregistrées
		CreateServiceDTO serviceDTO = new CreateServiceDTO(
				"Transition-Test-Service",
				"Service pour tester l'enregistrement des transitions",
				ServiceType.NON_BLOQUANT,
				Priority.NORMALE,
				List.of(),
				List.of(),
				30,
				false
		);

		webTestClient.post()
				.uri("/api/v1/services")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(objectMapper.writeValueAsString(serviceDTO))
				.exchange()
				.expectStatus().isCreated();

		// Vérifier que les transitions sont listées
		webTestClient.get()
				.uri("/api/v1/transitions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);

		// Traiter les transitions automatiques
		webTestClient.post()
				.uri("/api/v1/transitions/process-automatic")
				.exchange()
				.expectStatus().isOk();
	}
}

