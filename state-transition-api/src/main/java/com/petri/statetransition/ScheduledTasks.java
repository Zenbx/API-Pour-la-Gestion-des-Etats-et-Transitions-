package com.petri.statetransition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Component
class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final com.petri.statetransition.service.TransitionService transitionService;
    private final com.petri.statetransition.service.MetricsService metricsService;

    public ScheduledTasks(com.petri.statetransition.service.TransitionService transitionService,
                          com.petri.statetransition.service.MetricsService metricsService) {
        this.transitionService = transitionService;
        this.metricsService = metricsService;
    }

    /**
     * Traite les transitions automatiques toutes les minutes
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void processAutomaticTransitions() {
        logger.debug("Traitement des transitions automatiques planifié");
        transitionService.processAutomaticTransitions()
                .doOnNext(transition -> logger.debug("Transition automatique traitée: {}", transition.id()))
                .doOnError(error -> logger.warn("Erreur lors du traitement des transitions automatiques", error))
                .subscribe();
    }

    /**
     * Nettoie les anciennes transitions toutes les heures
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 3600000)
    public void cleanupOldTransitions() {
        logger.debug("Nettoyage des anciennes transitions planifié");
        transitionService.cleanupOldTransitions(7) // Garde 7 jours
                .doOnSuccess(count -> logger.info("Nettoyage terminé: {} transitions supprimées", count))
                .doOnError(error -> logger.warn("Erreur lors du nettoyage des transitions", error))
                .subscribe();
    }

    /**
     * Log des métriques système toutes les 5 minutes
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000)
    public void logSystemMetrics() {
        metricsService.getSystemMetrics()
                .doOnNext(metrics -> {
                    logger.info("=== MÉTRIQUES SYSTÈME ===");
                    logger.info("Services totaux: {}", metrics.totalServices());
                    logger.info("Ressources unitaires totales: {}", metrics.totalUnitResources());
                    logger.info("Ressources composites totales: {}", metrics.totalCompositeResources());
                    logger.info("Transitions actives: {}", metrics.activeTransitions());
                    logger.info("Débit système: {:.2f} transitions/heure", metrics.systemThroughput());
                    logger.info("========================");
                })
                .doOnError(error -> logger.warn("Erreur lors de la collecte des métriques", error))
                .subscribe();
    }
}
