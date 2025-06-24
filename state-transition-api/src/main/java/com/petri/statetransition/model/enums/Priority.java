package com.petri.statetransition.model.enums;

public enum Priority {
    CRITIQUE("CRITIQUE", 1),
    HAUTE("HAUTE", 2),
    NORMALE("NORMALE", 3),
    BASSE("BASSE", 4);

    private final String code;
    private final int level;

    Priority(String code, int level) {
        this.code = code;
        this.level = level;
    }

    public String getCode() { return code; }
    public int getLevel() { return level; }
}
