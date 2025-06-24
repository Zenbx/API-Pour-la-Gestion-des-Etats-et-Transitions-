package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des événements système
 */
@Configuration
public class EventConfig {

    /**
     * Publisher d'événements pour notifier les changements d'état
     */
    @Bean
    public org.springframework.context.ApplicationEventPublisher eventPublisher(
            org.springframework.context.ApplicationContext applicationContext) {
        return applicationContext;
    }
}
