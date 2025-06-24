package com.petri.statetransition.exception;

/**
 * Exception levée quand une ressource n'est pas trouvée
 */
public class ResourceNotFoundException extends StateTransitionException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
