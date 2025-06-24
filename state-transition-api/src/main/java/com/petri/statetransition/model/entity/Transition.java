
package com.petri.statetransition.model.entity;

import com.petri.statetransition.model.enums.TransitionType;
import com.petri.statetransition.model.enums.TransitionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * Entité Transition représentant une transition dans le réseau de Petri
 */
@Table("transitions")
public class Transition {

    @Id
    private Long id;

    @Column("type")
    private TransitionType type;

    @Column("status")
    private TransitionStatus status;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("error_message")
    private String errorMessage;

    @Column("metadata_json")
    private String metadataJson; // JSON string pour stocker les métadonnées

    // Constructeurs
    public Transition() {
        this.status = TransitionStatus.EN_ATTENTE;
    }

    public Transition(TransitionType type, String name, String description) {
        this();
        this.type = type;
        this.name = name;
        this.description = description;
    }

    // Méthodes métier
    public void start() {
        if (this.status != TransitionStatus.EN_ATTENTE) {
            throw new IllegalStateException("La transition ne peut être démarrée que si elle est en attente");
        }
        this.status = TransitionStatus.EN_COURS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != TransitionStatus.EN_COURS) {
            throw new IllegalStateException("La transition ne peut être complétée que si elle est en cours");
        }
        this.status = TransitionStatus.TERMINEE;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = TransitionStatus.ECHOUEE;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransitionType getType() { return type; }
    public void setType(TransitionType type) { this.type = type; }

    public TransitionStatus getStatus() { return status; }
    public void setStatus(TransitionStatus status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}