package com.petri.statetransition.exception;

/**
 * Exception levée lors d'une transition d'état invalide
 */
public class InvalidStateTransitionException extends StateTransitionException {

    private final String currentState;
    private final String targetState;

    public InvalidStateTransitionException(String message) {
        super(message);
        this.currentState = null;
        this.targetState = null;
    }

    public InvalidStateTransitionException(String message, String currentState, String targetState) {
        super(message);
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
        this.currentState = null;
        this.targetState = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTargetState() {
        return targetState;
    }
}
