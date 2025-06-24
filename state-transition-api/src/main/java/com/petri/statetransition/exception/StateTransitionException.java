package com.petri.statetransition.exception;

 /* Exception de base pour l'application State Transition
         */
public class StateTransitionException extends RuntimeException {

    public StateTransitionException(String message) {
        super(message);
    }

    public StateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}