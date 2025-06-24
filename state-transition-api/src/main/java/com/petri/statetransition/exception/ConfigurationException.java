package com.petri.statetransition.exception;

/**
 * Exception levée lors d'un problème de configuration
 */
public class ConfigurationException extends StateTransitionException {

    private final String configurationKey;
    private final String configurationValue;

    public ConfigurationException(String message) {
        super(message);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    public ConfigurationException(String message, String configurationKey, String configurationValue) {
        super(message);
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    public String getConfigurationValue() {
        return configurationValue;
    }
}