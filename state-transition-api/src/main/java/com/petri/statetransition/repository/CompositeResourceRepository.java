package com.petri.statetransition.repository;

import com.petri.statetransition.model.entity.CompositeResource;
import com.petri.statetransition.model.enums.CompositeResourceState;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface CompositeResourceRepository extends R2dbcRepository<CompositeResource, Long> {

    /**
     * Trouve les ressources composites par état
     */
    Flux<CompositeResource> findByState(CompositeResourceState state);

    /**
     * Trouve les ressources composites disponibles pour réservation
     */
    @Query("SELECT * FROM composite_resources WHERE state = 'VIDE'")
    Flux<CompositeResource> findAvailableResources();

    /**
     * Trouve les ressources composites par localisation
     */
    Flux<CompositeResource> findByLocation(String location);

    /**
     * Trouve les ressources composites par nom (recherche partielle)
     */
    @Query("SELECT * FROM composite_resources WHERE name LIKE CONCAT('%', :name, '%')")
    Flux<CompositeResource> findByNameContaining(@Param("name") String name);

    /**
     * Trouve les ressources composites requises par un service
     */
    @Query("""
        SELECT cr.* FROM composite_resources cr
        INNER JOIN service_composite_resources scr ON cr.id = scr.composite_resource_id
        WHERE scr.service_id = :serviceId
        """)
    Flux<CompositeResource> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Trouve les ressources composites qui contiennent une ressource unitaire spécifique
     */
    @Query("""
        SELECT cr.* FROM composite_resources cr
        INNER JOIN composite_unit_resources cur ON cr.id = cur.composite_resource_id
        WHERE cur.unit_resource_id = :unitResourceId
        """)
    Flux<CompositeResource> findByUnitResourceId(@Param("unitResourceId") Long unitResourceId);

    /**
     * Compte les ressources composites par état
     */
    @Query("SELECT COUNT(*) FROM composite_resources WHERE state = :state")
    Mono<Long> countByState(@Param("state") CompositeResourceState state);

    /**
     * Trouve les ressources composites avec une capacité minimale
     */
    @Query("SELECT * FROM composite_resources WHERE total_capacity >= :minCapacity AND state = 'VIDE'")
    Flux<CompositeResource> findAvailableWithMinCapacity(@Param("minCapacity") Integer minCapacity);

    /**
     * Statistiques des ressources composites par état
     */
    @Query("""
        SELECT state as resource_state, COUNT(*) as count_resources
        FROM composite_resources 
        GROUP BY state
        """)
    Flux<CompositeResourceStateCount> getResourceCountByState();

    interface CompositeResourceStateCount {
        String getResourceState();
        Long getCountResources();
    }
}