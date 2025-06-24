package com.petri.statetransition.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.Option;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.petri.statetransition.repository")
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "mysql")
                        .option(ConnectionFactoryOptions.HOST, "localhost")
                        .option(ConnectionFactoryOptions.PORT, 3306)
                        .option(ConnectionFactoryOptions.DATABASE, "petri_state_db")
                        .option(ConnectionFactoryOptions.USER, username)
                        .option(ConnectionFactoryOptions.PASSWORD, password)
                        .build()
        );
    }

    /**
     * Initialise la base de données avec le schéma et les données de test
     */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("schema.sql"),
                new ClassPathResource("data.sql")
        );
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}








