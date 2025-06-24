package com.petri.statetransition.model.entity;

import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;


@Table("composite_resources")
public class CompositeResource {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("state")
    private CompositeResourceState state;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("last_used_at")
    private LocalDateTime lastUsedAt;

    @Column("location")
    private String location;

    @Column("total_capacity")
    private Integer totalCapacity;

    @Column("min_required_components")
    private Integer minRequiredComponents;

    // Constructeurs
    public CompositeResource() {
        this.state = CompositeResourceState.VIDE;
    }

    public CompositeResource(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Méthodes métier
    public boolean canTransitionTo(CompositeResourceState newState) {
        if (this.state == null) return false;

        return switch (this.state) {
            case VIDE -> newState == CompositeResourceState.EN_COURS_RESERVATION ||
                    newState == CompositeResourceState.INDISPONIBLE;
            case EN_COURS_RESERVATION -> newState == CompositeResourceState.PRET ||
                    newState == CompositeResourceState.VIDE;
            case PRET -> newState == CompositeResourceState.AFFECTE ||
                    newState == CompositeResourceState.VIDE ||
                    newState == CompositeResourceState.INDISPONIBLE;
            case AFFECTE -> newState == CompositeResourceState.VIDE ||
                    newState == CompositeResourceState.ZOMBIE;
            case INDISPONIBLE -> newState == CompositeResourceState.VIDE;
            case ZOMBIE -> newState == CompositeResourceState.VIDE;
        };
    }

    public void transitionTo(CompositeResourceState newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                    String.format("Transition impossible de %s vers %s", this.state, newState)
            );
        }

        this.state = newState;
        this.updatedAt = LocalDateTime.now();

        if (newState == CompositeResourceState.AFFECTE) {
            this.lastUsedAt = LocalDateTime.now();
        }
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CompositeResourceState getState() { return state; }
    public void setState(CompositeResourceState state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getMinRequiredComponents() { return minRequiredComponents; }
    public void setMinRequiredComponents(Integer minRequiredComponents) {
        this.minRequiredComponents = minRequiredComponents;
    }
}