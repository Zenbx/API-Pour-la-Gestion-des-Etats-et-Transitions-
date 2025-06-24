package com.petri.statetransition.exception;

/**
 * Exception levée lors d'une violation de règle métier
 */
public class BusinessLogicException extends StateTransitionException {

    private final String businessRule;
    private final Object[] parameters;

    public BusinessLogicException(String message) {
        super(message);
        this.businessRule = null;
        this.parameters = null;
    }

    public BusinessLogicException(String message, String businessRule) {
        super(message);
        this.businessRule = businessRule;
        this.parameters = null;
    }

    public BusinessLogicException(String message, String businessRule, Object... parameters) {
        super(message);
        this.businessRule = businessRule;
        this.parameters = parameters;
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
        this.businessRule = null;
        this.parameters = null;
    }

    public String getBusinessRule() {
        return businessRule;
    }

    public Object[] getParameters() {
        return parameters;
    }
}
