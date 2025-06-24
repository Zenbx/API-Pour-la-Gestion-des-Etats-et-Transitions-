package com.petri.statetransition.repository;



import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface ServiceCompositeResourceRepository extends R2dbcRepository<com.petri.statetransition.model.entity.ServiceCompositeResource, Long> {

    Flux<com.petri.statetransition.model.entity.ServiceCompositeResource> findByServiceId(Long serviceId);
    Flux<com.petri.statetransition.model.entity.ServiceCompositeResource> findByCompositeResourceId(Long compositeResourceId);
    Mono<Void> deleteByServiceIdAndCompositeResourceId(Long serviceId, Long compositeResourceId);
}
