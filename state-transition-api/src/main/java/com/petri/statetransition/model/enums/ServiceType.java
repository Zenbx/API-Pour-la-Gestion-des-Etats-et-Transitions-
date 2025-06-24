package com.petri.statetransition.model.enums;

/**
 * Type de service selon le modèle de réseaux de Petri
 */
public enum ServiceType {
    BLOQUANT("BLOQUANT", "Service bloquant - toutes ressources requises"),
    NON_BLOQUANT("NON_BLOQUANT", "Service non-bloquant - au moins une ressource");

    private final String code;
    private final String description;

    ServiceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
