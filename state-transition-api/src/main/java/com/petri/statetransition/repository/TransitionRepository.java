package com.petri.statetransition.repository;

import com.petri.statetransition.model.entity.Transition;
import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository pour l'entité Transition
 */
@Repository
public interface TransitionRepository extends R2dbcRepository<Transition, Long> {

    /**
     * Trouve les transitions par type
     */
    Flux<Transition> findByType(TransitionType type);

    /**
     * Trouve les transitions par statut
     */
    Flux<Transition> findByStatus(TransitionStatus status);

    /**
     * Trouve les transitions actives (en attente ou en cours)
     */
    @Query("SELECT * FROM transitions WHERE status IN ('EN_ATTENTE', 'EN_COURS')")
    Flux<Transition> findActiveTransitions();

    /**
     * Trouve les transitions en cours depuis plus de X minutes
     */
    @Query("""
        SELECT * FROM transitions 
        WHERE status = 'EN_COURS' 
        AND started_at IS NOT NULL
        AND TIMESTAMPDIFF(MINUTE, started_at, NOW()) > :minutes
        """)
    Flux<Transition> findLongRunningTransitions(@Param("minutes") Integer minutes);

    /**
     * Trouve les transitions échouées dans une période donnée
     */
    @Query("""
        SELECT * FROM transitions 
        WHERE status = 'ECHOUEE' 
        AND completed_at >= :startDate 
        AND completed_at <= :endDate
        """)
    Flux<Transition> findFailedTransitionsInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Trouve les transitions automatiques à déclencher
     */
    @Query("SELECT * FROM transitions WHERE type = 'AUTOMATIQUE' AND status = 'EN_ATTENTE'")
    Flux<Transition> findPendingAutomaticTransitions();

    /**
     * Compte les transitions par statut
     */
    @Query("SELECT COUNT(*) FROM transitions WHERE status = :status")
    Mono<Long> countByStatus(@Param("status") TransitionStatus status);

    /**
     * Compte les transitions par type
     */
    @Query("SELECT COUNT(*) FROM transitions WHERE type = :type")
    Mono<Long> countByType(@Param("type") TransitionType type);

    /**
     * Trouve les transitions récentes (dernières 24h)
     */
    @Query("SELECT * FROM transitions WHERE created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    Flux<Transition> findRecentTransitions();

    /**
     * Calcule le temps moyen d'exécution des transitions
     */
    @Query("""
        SELECT AVG(TIMESTAMPDIFF(SECOND, started_at, completed_at)) as avg_execution_time
        FROM transitions 
        WHERE status = 'TERMINEE' 
        AND started_at IS NOT NULL 
        AND completed_at IS NOT NULL
        """)
    Mono<Double> getAverageExecutionTimeSeconds();

    /**
     * Statistiques des transitions par statut
     */
    @Query("""
        SELECT status as transition_status, COUNT(*) as count_transitions
        FROM transitions 
        GROUP BY status
        """)
    Flux<TransitionStatusCount> getTransitionCountByStatus();

    /**
     * Statistiques des transitions par type
     */
    @Query("""
        SELECT type as transition_type, COUNT(*) as count_transitions
        FROM transitions 
        GROUP BY type
        """)
    Flux<TransitionTypeCount> getTransitionCountByType();

    /**
     * Trouve les transitions créées dans une période donnée
     */
    Flux<Transition> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouve les transitions par nom (recherche partielle)
     */
    @Query("SELECT * FROM transitions WHERE name LIKE CONCAT('%', :name, '%')")
    Flux<Transition> findByNameContaining(@Param("name") String name);

    /**
     * Supprime les anciennes transitions terminées
     */
    @Query("DELETE FROM transitions WHERE status IN ('TERMINEE', 'ECHOUEE') AND completed_at < :beforeDate")
    Mono<Integer> deleteOldCompletedTransitions(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Interfaces pour les statistiques
     */
    interface TransitionStatusCount {
        String getTransitionStatus();
        Long getCountTransitions();
    }

    interface TransitionTypeCount {
        String getTransitionType();
        Long getCountTransitions();
    }
}