package com.petri.statetransition.model.enums;

/**
 * Énumération des états possibles pour un service
 * Basée sur la modélisation mathématique des réseaux de Petri
 */
public enum ServiceState {
    PLANIFIE("PLANIFIÉ", "Service planifié"),
    PUBLIE("PUBLIÉ", "Service publié"),
    PRET("PRÊT", "Service prêt à démarrer"),
    BLOQUE("BLOQUÉ", "Service bloqué par manque de ressources"),
    ANNULE("ANNULÉ", "Service annulé"),
    RETARDE("RETARDÉ", "Service retardé"),
    EN_PAUSE("EN_PAUSE", "Service en pause"),
    EN_COURS("EN_COURS", "Service en cours d'exécution"),
    ARRETE("ARRÊTÉ", "Service arrêté"),
    TERMINE("TERMINÉ", "Service terminé avec succès");

    private final String code;
    private final String description;

    ServiceState(String code, String description) {
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
     * Vérifie si l'état est un état final
     */
    public boolean isFinalState() {
        return this == TERMINE || this == ANNULE || this == ARRETE;
    }

    /**
     * Vérifie si l'état permet l'exécution
     */
    public boolean isExecutableState() {
        return this == PRET || this == EN_COURS;
    }

    /**
     * Vérifie si l'état indique un problème
     */
    public boolean isErrorState() {
        return this == BLOQUE || this == ARRETE || this == ANNULE;
    }
}