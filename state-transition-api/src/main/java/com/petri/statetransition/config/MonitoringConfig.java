package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des métriques et monitoring
 */
@Configuration
public class MonitoringConfig {

    /**
     * Configures les métriques personnalisées
     */
    @Bean
    public io.micrometer.core.instrument.MeterRegistry meterRegistry() {
        return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
    }
}
