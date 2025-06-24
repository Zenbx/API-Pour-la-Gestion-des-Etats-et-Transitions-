package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;


/**
 * Configuration générale de l'application
 */
@Configuration
public class AppConfig {

    /**
     * Configuration Jackson pour la sérialisation JSON
     */
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * Configuration du pool de threads pour les opérations asynchrones
     */
    @Bean
    public java.util.concurrent.Executor taskExecutor() {
        java.util.concurrent.ThreadPoolExecutor executor = new java.util.concurrent.ThreadPoolExecutor(
                5,  // Core pool size
                20, // Maximum pool size
                60L, // Keep alive time
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(100)
        );
        executor.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName("petri-async-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
        return executor;
    }
}
