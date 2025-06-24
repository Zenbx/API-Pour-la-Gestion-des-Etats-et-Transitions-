package com.petri.statetransition.exception;

public class ResourceAllocationException extends StateTransitionException {

    private final Long resourceId;
    private final String resourceType;

    public ResourceAllocationException(String message) {
        super(message);
        this.resourceId = null;
        this.resourceType = null;
    }

    public ResourceAllocationException(String message, Long resourceId, String resourceType) {
        super(message);
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }

    public ResourceAllocationException(String message, Throwable cause) {
        super(message, cause);
        this.resourceId = null;
        this.resourceType = null;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }
}