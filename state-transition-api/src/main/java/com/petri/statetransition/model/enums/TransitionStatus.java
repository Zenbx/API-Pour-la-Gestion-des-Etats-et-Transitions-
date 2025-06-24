package com.petri.statetransition.model.enums;

public enum TransitionStatus {
    EN_ATTENTE("EN_ATTENTE", "Transition en attente"),
    EN_COURS("EN_COURS", "Transition en cours"),
    TERMINEE("TERMINÉE", "Transition terminée"),
    ECHOUEE("ÉCHOUÉE", "Transition échouée");

    private final String code;
    private final String description;

    TransitionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}