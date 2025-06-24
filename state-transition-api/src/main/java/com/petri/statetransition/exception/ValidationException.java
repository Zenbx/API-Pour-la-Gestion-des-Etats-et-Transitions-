package com.petri.statetransition.exception;

/**
 * Exception levée lors d'un problème de validation
 */
public class ValidationException extends StateTransitionException {

    private final java.util.List<String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public ValidationException(String message, java.util.List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = null;
    }

    public java.util.List<String> getValidationErrors() {
        return validationErrors;
    }
}
