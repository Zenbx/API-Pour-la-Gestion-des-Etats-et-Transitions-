package com.petri.statetransition.repository;



import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
@Repository
public interface CompositeUnitResourceRepository extends R2dbcRepository<com.petri.statetransition.model.entity.CompositeUnitResource, Long> {

    Flux<com.petri.statetransition.model.entity.CompositeUnitResource> findByCompositeResourceId(Long compositeResourceId);
    Flux<com.petri.statetransition.model.entity.CompositeUnitResource> findByUnitResourceId(Long unitResourceId);
    Mono<Void> deleteByCompositeResourceIdAndUnitResourceId(Long compositeResourceId, Long unitResourceId);
}
