package com.mdanyarov.weblibrary.config;

import com.mdanyarov.weblibrary.dao.ConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

/**
 * Application configuration class for the Library application.
 * Handles core application setup and database configuration.
 */
@Configuration
@ComponentScan({"com.mdanyarov.weblibrary.service", "com.mdanyarov.weblibrary.dao", "com.mdanyarov.weblibrary.config", "com.mdanyarov.weblibrary.security"})
@PropertySource("classpath:application.properties")
public class AppConfig {

    private final Environment environment;

    @Autowired
    public AppConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Configures the message source for internationalization.
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Configures the database connection pool.
     *
     * @return ConnectionPool instance
     * @throws SQLException if there is an error creating the connection pool
     */
    @Bean
    public ConnectionPool connectionPool() throws SQLException {
        String url = environment.getProperty("db.url", "jdbc:mysql://localhost:3306/library");
        String username = environment.getProperty("db.username", "root");
        String password = environment.getProperty("db.password", "root");
        int maxPoolSize = Integer.parseInt(environment.getProperty("db.pool.maxSize", "10"));

        return ConnectionPool.getInstance(url, username, password, maxPoolSize);
    }
}
