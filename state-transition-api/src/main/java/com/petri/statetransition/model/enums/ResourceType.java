package com.petri.statetransition.model.enums;

public enum ResourceType {
    UNITAIRE("UNITAIRE", "Ressource unitaire simple"),
    COMPOSITE("COMPOSITE", "Ressource composée de plusieurs unités");

    private final String code;
    private final String description;

    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}