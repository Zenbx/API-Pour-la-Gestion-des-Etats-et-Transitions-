package com.petri.statetransition.model.entity;

import com.petri.statetransition.model.enums.ServiceState;
import com.petri.statetransition.model.enums.ServiceType;
import com.petri.statetransition.model.enums.Priority;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * Entité Service représentant un service dans le système de réseaux de Petri
 */
@Table("services")
public class Services {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("state")
    private ServiceState state;

    @Column("type")
    private ServiceType type;

    @Column("priority")
    private Priority priority;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("max_execution_time_minutes")
    private Integer maxExecutionTimeMinutes;

    @Column("auto_retry")
    private Boolean autoRetry;

    // Constructeurs
    public Services() {
        this.state = ServiceState.PLANIFIE;
        this.autoRetry = false;
    }

    public Services(String name, String description, ServiceType type, Priority priority) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.priority = priority;
    }

    // Méthodes métier
    public boolean canTransitionTo(ServiceState newState) {
        if (this.state == null) return false;

        return switch (this.state) {
            case PLANIFIE -> newState == ServiceState.PUBLIE || newState == ServiceState.ANNULE;
            case PUBLIE -> newState == ServiceState.PRET || newState == ServiceState.ANNULE;
            case PRET -> newState == ServiceState.EN_COURS || newState == ServiceState.BLOQUE ||
                    newState == ServiceState.RETARDE || newState == ServiceState.ANNULE;
            case BLOQUE -> newState == ServiceState.PRET || newState == ServiceState.ANNULE;
            case RETARDE -> newState == ServiceState.PRET || newState == ServiceState.ANNULE;
            case EN_PAUSE -> newState == ServiceState.EN_COURS || newState == ServiceState.ANNULE;
            case EN_COURS -> newState == ServiceState.TERMINE || newState == ServiceState.ARRETE ||
                    newState == ServiceState.EN_PAUSE || newState == ServiceState.BLOQUE;
            case ARRETE, ANNULE, TERMINE -> false; // États finaux
        };
    }

    public void transitionTo(ServiceState newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                    String.format("Transition impossible de %s vers %s", this.state, newState)
            );
        }

        ServiceState previousState = this.state;
        this.state = newState;

        // Mise à jour des timestamps selon la transition
        LocalDateTime now = LocalDateTime.now();
        if (newState == ServiceState.EN_COURS && previousState != ServiceState.EN_PAUSE) {
            this.startedAt = now;
        } else if (newState.isFinalState()) {
            this.completedAt = now;
        }

        this.updatedAt = now;
    }

    public boolean isFinalState() {
        return state != null && state.isFinalState();
    }

    public boolean isExecutableState() {
        return state != null && state.isExecutableState();
    }

    public boolean isErrorState() {
        return state != null && state.isErrorState();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceState getState() { return state; }
    public void setState(ServiceState state) { this.state = state; }

    public ServiceType getType() { return type; }
    public void setType(ServiceType type) { this.type = type; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getMaxExecutionTimeMinutes() { return maxExecutionTimeMinutes; }
    public void setMaxExecutionTimeMinutes(Integer maxExecutionTimeMinutes) {
        this.maxExecutionTimeMinutes = maxExecutionTimeMinutes;
    }

    public Boolean getAutoRetry() { return autoRetry; }
    public void setAutoRetry(Boolean autoRetry) { this.autoRetry = autoRetry; }
}