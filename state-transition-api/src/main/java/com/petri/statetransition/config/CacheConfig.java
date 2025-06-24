package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du cache (simple cache en mémoire pour démo)
 */
@Configuration
@org.springframework.cache.annotation.EnableCaching
public class CacheConfig {

    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        org.springframework.cache.concurrent.ConcurrentMapCacheManager cacheManager =
                new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "services", "unitResources", "compositeResources", "transitions", "metrics"
        ));
        return cacheManager;
    }
}
