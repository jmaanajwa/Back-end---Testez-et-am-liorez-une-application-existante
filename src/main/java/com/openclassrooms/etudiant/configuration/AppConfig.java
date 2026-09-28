package com.openclassrooms.etudiant.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

/**
 * Configures application property resolution using the local {@code .env} file.
 */
@Configuration
public class AppConfig {

    /**
     * Loads properties from the {@code .env} file in the working directory.
     *
     * @return the configurer used to resolve property placeholders
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource(".env"));
        return configurer;
    }
}
