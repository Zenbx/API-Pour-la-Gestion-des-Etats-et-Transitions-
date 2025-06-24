package com.petri.statetransition.repository;

import com.petri.statetransition.model.entity.UnitResource;
import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Repository pour l'entité UnitResource
 */
@Repository
public interface UnitResourceRepository extends R2dbcRepository<UnitResource, Long> {

    /**
     * Trouve les ressources unitaires par état
     */
    Flux<UnitResource> findByState(UnitResourceState state);

    /**
     * Trouve les ressources unitaires disponibles pour allocation
     */
    @Query("SELECT * FROM unit_resources WHERE state = 'LIBRE'")
    Flux<UnitResource> findAvailableResources();

    /**
     * Trouve les ressources unitaires par localisation
     */
    Flux<UnitResource> findByLocation(String location);

    /**
     * Trouve les ressources unitaires par nom (recherche partielle)
     */
    @Query("SELECT * FROM unit_resources WHERE name LIKE CONCAT('%', :name, '%')")
    Flux<UnitResource> findByNameContaining(@Param("name") String name);

    /**
     * Trouve les ressources unitaires utilisées récemment
     */
    Flux<UnitResource> findByLastUsedAtAfter(LocalDateTime lastUsedAt);

    /**
     * Trouve les ressources unitaires qui font partie d'une ressource composite
     */
    @Query("""
        SELECT ur.* FROM unit_resources ur
        INNER JOIN composite_unit_resources cur ON ur.id = cur.unit_resource_id
        WHERE cur.composite_resource_id = :compositeResourceId
        """)
    Flux<UnitResource> findByCompositeResourceId(@Param("compositeResourceId") Long compositeResourceId);

    /**
     * Trouve les ressources unitaires requises par un service
     */
    @Query("""
        SELECT ur.* FROM unit_resources ur
        INNER JOIN service_unit_resources sur ON ur.id = sur.unit_resource_id
        WHERE sur.service_id = :serviceId
        """)
    Flux<UnitResource> findByServiceId(@Param("serviceId") Long serviceId);

    /**
     * Compte les ressources par état
     */
    @Query("SELECT COUNT(*) FROM unit_resources WHERE state = :state")
    Mono<Long> countByState(@Param("state") UnitResourceState state);

    /**
     * Trouve les ressources avec une capacité minimale
     */
    @Query("SELECT * FROM unit_resources WHERE capacity >= :minCapacity AND state = 'LIBRE'")
    Flux<UnitResource> findAvailableWithMinCapacity(@Param("minCapacity") Integer minCapacity);

    /**
     * Statistiques des ressources unitaires par état
     */
    @Query("""
        SELECT state as resource_state, COUNT(*) as count_resources
        FROM unit_resources 
        GROUP BY state
        """)
    Flux<UnitResourceStateCount> getResourceCountByState();

    interface UnitResourceStateCount {
        String getResourceState();
        Long getCountResources();
    }
}
