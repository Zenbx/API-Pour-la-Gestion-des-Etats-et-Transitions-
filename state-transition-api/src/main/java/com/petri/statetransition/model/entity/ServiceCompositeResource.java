package com.petri.statetransition.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
@Table("service_composite_resources")
public class ServiceCompositeResource {

    @Id
    private Long id;

    @Column("service_id")
    private Long serviceId;

    @Column("composite_resource_id")
    private Long compositeResourceId;

    @Column("is_required")
    private Boolean isRequired;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    // Constructeurs
    public ServiceCompositeResource() {
        this.isRequired = true;
    }

    public ServiceCompositeResource(Long serviceId, Long compositeResourceId, Boolean isRequired) {
        this();
        this.serviceId = serviceId;
        this.compositeResourceId = compositeResourceId;
        this.isRequired = isRequired;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getCompositeResourceId() { return compositeResourceId; }
    public void setCompositeResourceId(Long compositeResourceId) {
        this.compositeResourceId = compositeResourceId;
    }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

