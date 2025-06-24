package com.petri.statetransition.model.enums;

public enum TransitionType {
    NORMALE("NORMALE", "Transition normale"),
    SYNCHRONE("SYNCHRONE", "Transition couplée/synchronisée"),
    AUTOMATIQUE("AUTOMATIQUE", "Transition automatique");

    private final String code;
    private final String description;

    TransitionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}