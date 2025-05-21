package com.mdanyarov.weblibrary.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;

/**
 * Application configuration class for the Library application.
 * Handles core application setup and database configuration.
 */
@Configuration
@ComponentScan("com.mdanyarov.weblibrary.service", "com.mdanyarov.weblibrary.dao")
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

    // TODO: add database configuration
}
