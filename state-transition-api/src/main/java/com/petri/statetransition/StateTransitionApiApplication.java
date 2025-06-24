package com.petri.statetransition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

/**
 * Application principale Spring Boot pour l'API State Transition
 *
 * Cette application implémente un système de gestion d'états et de transitions
 * basé sur la modélisation mathématique des réseaux de Petri.
 *
 * Fonctionnalités principales:
 * - Gestion des services avec états et transitions
 * - Gestion des ressources unitaires et composites
 * - Allocation et libération automatiques de ressources
 * - Transitions synchrones et automatiques
 * - Métriques et monitoring en temps réel
 * - API REST réactive avec WebFlux
 * - Sécurité avec Spring Security
 * - Base de données MySQL avec R2DBC
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class StateTransitionApiApplication {

	private static final Logger logger = LoggerFactory.getLogger(StateTransitionApiApplication.class);

	public static void main(String[] args) {
		// Activation des hooks Reactor pour un meilleur debugging
		Hooks.onOperatorDebug();

		// Démarrage de l'application
		logger.info("=========================================");
		logger.info("DÉMARRAGE DE L'API STATE TRANSITION");
		logger.info("=========================================");
		logger.info("Basée sur la modélisation mathématique des réseaux de Petri");
		logger.info("Version: 1.0.0");
		logger.info("Environnement: Development");
		logger.info("=========================================");

		SpringApplication app = new SpringApplication(StateTransitionApiApplication.class);

		// Configuration du contexte
		app.setAdditionalProfiles("development");

		try {
			var context = app.run(args);

			// Log des informations de démarrage
			String port = context.getEnvironment().getProperty("server.port", "8080");
			String contextPath = context.getEnvironment().getProperty("spring.webflux.base-path", "");

			logger.info("=========================================");
			logger.info("APPLICATION DÉMARRÉE AVEC SUCCÈS");
			logger.info("=========================================");
			logger.info("URL locale: http://localhost:{}{}", port, contextPath);
			logger.info("URL API: http://localhost:{}{}/api/v1", port, contextPath);
			logger.info("URL Actuator: http://localhost:{}/actuator", port);
			logger.info("URL Health: http://localhost:{}/actuator/health", port);
			logger.info("URL Metrics: http://localhost:{}{}/api/v1/metrics/system", port, contextPath);
			logger.info("=========================================");
			logger.info("Utilisateurs de test:");
			logger.info("- admin/admin123 (ROLE_ADMIN)");
			logger.info("- user/user123 (ROLE_USER)");
			logger.info("- viewer/viewer123 (ROLE_VIEWER)");
			logger.info("=========================================");
			logger.info("Documentation des endpoints:");
			logger.info("GET    /api/v1/services - Liste des services");
			logger.info("POST   /api/v1/services - Créer un service");
			logger.info("POST   /api/v1/services/{id}/start - Démarrer un service");
			logger.info("GET    /api/v1/unit-resources - Liste des ressources unitaires");
			logger.info("GET    /api/v1/composite-resources - Liste des ressources composites");
			logger.info("GET    /api/v1/transitions - Liste des transitions");
			logger.info("GET    /api/v1/metrics/system - Métriques système");
			logger.info("=========================================");

		} catch (Exception e) {
			logger.error("Erreur lors du démarrage de l'application", e);
			System.exit(1);
		}
	}
}
