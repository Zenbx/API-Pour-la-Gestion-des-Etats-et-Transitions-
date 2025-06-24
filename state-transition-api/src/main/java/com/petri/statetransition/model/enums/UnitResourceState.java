package com.petri.statetransition.model.enums;

/**
 * Énumération des états possibles pour une ressource unitaire
 * Basée sur la modélisation mathématique des réseaux de Petri
 */
public enum UnitResourceState {
    LIBRE("LIBRE", "Ressource disponible"),
    AFFECTE("AFFECTÉ", "Ressource affectée mais pas encore utilisée"),
    OCCUPE("OCCUPÉ", "Ressource en cours d'utilisation"),
    BLOQUE("BLOQUÉ", "Ressource bloquée temporairement"),
    INDISPONIBLE("INDISPONIBLE", "Ressource indisponible"),
    ZOMBIE("ZOMBIE", "Ressource en état zombie");

    private final String code;
    private final String description;

    UnitResourceState(String code, String description) {
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
     * Vérifie si la ressource est disponible pour allocation
     */
    public boolean isAvailableForAllocation() {
        return this == LIBRE;
    }

    /**
     * Vérifie si la ressource est actuellement utilisée
     */
    public boolean isInUse() {
        return this == OCCUPE;
    }

    /**
     * Vérifie si la ressource est dans un état problématique
     */
    public boolean isProblematic() {
        return this == BLOQUE || this == INDISPONIBLE || this == ZOMBIE;
    }

    /**
     * Vérifie si la ressource peut être libérée
     */
    public boolean canBeReleased() {
        return this == AFFECTE || this == OCCUPE;
    }
}