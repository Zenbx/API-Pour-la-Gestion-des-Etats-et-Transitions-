package com.petri.statetransition.model.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
@Table("composite_unit_resources")
public class CompositeUnitResource {

    @Id
    private Long id;

    @Column("composite_resource_id")
    private Long compositeResourceId;

    @Column("unit_resource_id")
    private Long unitResourceId;

    @Column("is_required")
    private Boolean isRequired;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    // Constructeurs
    public CompositeUnitResource() {
        this.isRequired = true;
    }

    public CompositeUnitResource(Long compositeResourceId, Long unitResourceId, Boolean isRequired) {
        this();
        this.compositeResourceId = compositeResourceId;
        this.unitResourceId = unitResourceId;
        this.isRequired = isRequired;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompositeResourceId() { return compositeResourceId; }
    public void setCompositeResourceId(Long compositeResourceId) {
        this.compositeResourceId = compositeResourceId;
    }

    public Long getUnitResourceId() { return unitResourceId; }
    public void setUnitResourceId(Long unitResourceId) { this.unitResourceId = unitResourceId; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}