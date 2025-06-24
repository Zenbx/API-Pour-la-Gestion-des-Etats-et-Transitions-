package com.petri.statetransition.exception;

/**
 * Exception levée lors d'un conflit de concurrence
 */
public class ConcurrencyException extends StateTransitionException {

    private final String resourceType;
    private final Long resourceId;
    private final String expectedVersion;
    private final String actualVersion;

    public ConcurrencyException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
        this.expectedVersion = null;
        this.actualVersion = null;
    }

    public ConcurrencyException(String message, String resourceType, Long resourceId,
                                String expectedVersion, String actualVersion) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getExpectedVersion() {
        return expectedVersion;
    }

    public String getActualVersion() {
        return actualVersion;
    }
}
