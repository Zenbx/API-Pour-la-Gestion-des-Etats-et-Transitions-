package com.petri.statetransition.exception;

/**
 * Exception levée lors d'un problème de transition
 */
public class TransitionException extends StateTransitionException {

    private final Long transitionId;
    private final String transitionType;

    public TransitionException(String message) {
        super(message);
        this.transitionId = null;
        this.transitionType = null;
    }

    public TransitionException(String message, Long transitionId, String transitionType) {
        super(message);
        this.transitionId = transitionId;
        this.transitionType = transitionType;
    }

    public TransitionException(String message, Throwable cause) {
        super(message, cause);
        this.transitionId = null;
        this.transitionType = null;
    }

    public Long getTransitionId() {
        return transitionId;
    }

    public String getTransitionType() {
        return transitionType;
    }
}
