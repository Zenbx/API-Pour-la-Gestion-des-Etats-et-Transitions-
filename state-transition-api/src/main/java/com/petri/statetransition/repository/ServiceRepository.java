package com.petri.statetransition.repository;

import com.petri.statetransition.model.entity.Services;
import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.Priority;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository pour l'entité Service
 */
@Repository
public interface ServiceRepository extends R2dbcRepository<Services, Long> {

    /**
     * Trouve les services par état
     */
    Flux<Services> findByState(ServiceState state);

    /**
     * Trouve les services par type
     */
    Flux<Services> findByType(ServiceType type);

    /**
     * Trouve les services par priorité
     */
    Flux<Services> findByPriority(Priority priority);

    /**
     * Trouve les services par nom (recherche partielle)
     */
    @Query("SELECT * FROM services WHERE name LIKE CONCAT('%', :name, '%')")
    Flux<Services> findByNameContaining(@Param("name") String name);

    /**
     * Trouve les services créés après une date donnée
     */
    Flux<Services> findByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Trouve les services dans des états spécifiques
     */
    @Query("SELECT * FROM services WHERE state IN (:states)")
    Flux<Services> findByStates(@Param("states") ServiceState... states);

    /**
     * Trouve les services actifs (non terminés)
     */
    @Query("SELECT * FROM services WHERE state NOT IN ('TERMINE', 'ANNULE', 'ARRETE')")
    Flux<Services> findActiveServices();

    /**
     * Trouve les services qui ont dépassé leur temps d'exécution maximum
     */
    @Query("""
        SELECT * FROM services 
        WHERE state = 'EN_COURS' 
        AND max_execution_time_minutes IS NOT NULL 
        AND started_at IS NOT NULL
        AND TIMESTAMPDIFF(MINUTE, started_at, NOW()) > max_execution_time_minutes
        """)
    Flux<Services> findServicesExceedingMaxExecutionTime();

    /**
     * Compte les services par état
     */
    @Query("SELECT COUNT(*) FROM services WHERE state = :state")
    Mono<Long> countByState(@Param("state") ServiceState state);

    /**
     * Trouve les services nécessitant une ressource unitaire spécifique
     */
    @Query("""
        SELECT s.* FROM services s
        INNER JOIN service_unit_resources sur ON s.id = sur.service_id
        WHERE sur.unit_resource_id = :unitResourceId
        """)
    Flux<Services> findByRequiredUnitResourceId(@Param("unitResourceId") Long unitResourceId);

    /**
     * Trouve les services nécessitant une ressource composite spécifique
     */
    @Query("""
        SELECT s.* FROM services s
        INNER JOIN service_composite_resources scr ON s.id = scr.service_id
        WHERE scr.composite_resource_id = :compositeResourceId
        """)
    Flux<Services> findByRequiredCompositeResourceId(@Param("compositeResourceId") Long compositeResourceId);

    /**
     * Trouve les services prêts à être démarrés selon leur priorité
     */
    @Query("""
        SELECT * FROM services 
        WHERE state = 'PRET' 
        ORDER BY 
            CASE priority 
                WHEN 'CRITIQUE' THEN 1 
                WHEN 'HAUTE' THEN 2 
                WHEN 'NORMALE' THEN 3 
                WHEN 'BASSE' THEN 4 
            END,
            created_at ASC
        """)
    Flux<Services> findReadyServicesByPriority();

    /**
     * Recherche avancée avec filtres multiples
     */
    @Query("""
        SELECT * FROM services 
        WHERE (:name IS NULL OR name LIKE CONCAT('%', :name, '%'))
        AND (:state IS NULL OR state = :state)
        AND (:type IS NULL OR type = :type)
        AND (:priority IS NULL OR priority = :priority)
        AND (:createdAfter IS NULL OR created_at >= :createdAfter)
        AND (:createdBefore IS NULL OR created_at <= :createdBefore)
        ORDER BY created_at DESC
        """)
    Flux<Services> findWithFilters(
            @Param("name") String name,
            @Param("state") ServiceState state,
            @Param("type") ServiceType type,
            @Param("priority") Priority priority,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore
    );

    /**
     * Statistiques des services par état
     */
    @Query("""
        SELECT state as service_state, COUNT(*) as count_services
        FROM services 
        GROUP BY state
        """)
    Flux<ServiceStateCount> getServiceCountByState();

    /**
     * Interface pour les statistiques
     */
    interface ServiceStateCount {
        String getServiceState();
        Long getCountServices();
    }
}