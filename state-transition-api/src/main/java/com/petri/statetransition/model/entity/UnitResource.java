package com.petri.statetransition.model.entity;

import com.petri.statetransition.model.enums.UnitResourceState;
import com.petri.statetransition.model.enums.CompositeResourceState;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * Entité UnitResource représentant une ressource unitaire
 */
@Table("unit_resources")
public class UnitResource {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("state")
    private UnitResourceState state;

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

    @Column("capacity")
    private Integer capacity;

    @Column("current_load")
    private Integer currentLoad;

    // Constructeurs
    public UnitResource() {
        this.state = UnitResourceState.LIBRE;
        this.currentLoad = 0;
    }

    public UnitResource(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Méthodes métier
    public boolean canTransitionTo(UnitResourceState newState) {
        if (this.state == null) return false;

        return switch (this.state) {
            case LIBRE -> newState == UnitResourceState.AFFECTE ||
                    newState == UnitResourceState.INDISPONIBLE;
            case AFFECTE -> newState == UnitResourceState.OCCUPE ||
                    newState == UnitResourceState.LIBRE ||
                    newState == UnitResourceState.BLOQUE;
            case OCCUPE -> newState == UnitResourceState.LIBRE ||
                    newState == UnitResourceState.BLOQUE ||
                    newState == UnitResourceState.ZOMBIE;
            case BLOQUE -> newState == UnitResourceState.LIBRE ||
                    newState == UnitResourceState.INDISPONIBLE;
            case INDISPONIBLE -> newState == UnitResourceState.LIBRE;
            case ZOMBIE -> newState == UnitResourceState.LIBRE;
        };
    }

    public void transitionTo(UnitResourceState newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                    String.format("Transition impossible de %s vers %s", this.state, newState)
            );
        }

        this.state = newState;
        this.updatedAt = LocalDateTime.now();

        if (newState == UnitResourceState.OCCUPE) {
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

    public UnitResourceState getState() { return state; }
    public void setState(UnitResourceState state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(Integer currentLoad) { this.currentLoad = currentLoad; }
}