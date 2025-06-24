package com.petri.statetransition.model.enums;

/**
 * Énumération des états possibles pour une ressource composite
 * Basée sur la modélisation mathématique des réseaux de Petri
 */
public enum CompositeResourceState {
    VIDE("VIDE", "Ressource composite vide"),
    EN_COURS_RESERVATION("EN_COURS_RÉSERVATION", "Réservation en cours"),
    PRET("PRÊT", "Ressource composite prête"),
    ZOMBIE("ZOMBIE", "Ressource composite en état zombie"),
    INDISPONIBLE("INDISPONIBLE", "Ressource composite indisponible"),
    AFFECTE("AFFECTÉ", "Ressource composite affectée");

    private final String code;
    private final String description;

    CompositeResourceState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si la ressource composite est disponible pour réservation
     */
    public boolean isAvailableForReservation() {
        return this == VIDE;
    }

    /**
     * Vérifie si la ressource composite est prête pour utilisation
     */
    public boolean isReadyForUse() {
        return this == PRET;
    }

    /**
     * Vérifie si la ressource composite est en cours d'allocation
     */
    public boolean isAllocating() {
        return this == EN_COURS_RESERVATION;
    }

    /**
     * Vérifie si la ressource composite est dans un état problématique
     */
    public boolean isProblematic() {
        return this == ZOMBIE || this == INDISPONIBLE;
    }

    /**
     * Vérifie si la ressource composite est utilisée
     */
    public boolean isInUse() {
        return this == AFFECTE;
    }
}