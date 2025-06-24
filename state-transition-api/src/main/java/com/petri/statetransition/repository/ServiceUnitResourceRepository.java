package com.petri.statetransition.repository;



import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface ServiceUnitResourceRepository extends R2dbcRepository<com.petri.statetransition.model.entity.ServiceUnitResource, Long> {

    Flux<com.petri.statetransition.model.entity.ServiceUnitResource> findByServiceId(Long serviceId);
    Flux<com.petri.statetransition.model.entity.ServiceUnitResource> findByUnitResourceId(Long unitResourceId);
    Mono<Void> deleteByServiceIdAndUnitResourceId(Long serviceId, Long unitResourceId);
}
