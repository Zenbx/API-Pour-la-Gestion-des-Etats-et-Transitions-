package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de validation
 */
@Configuration
public class ValidationConfig {

    /**
     * Validateur pour les requêtes
     */
    @Bean
    public jakarta.validation.Validator validator() {
        return jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
    }
}
