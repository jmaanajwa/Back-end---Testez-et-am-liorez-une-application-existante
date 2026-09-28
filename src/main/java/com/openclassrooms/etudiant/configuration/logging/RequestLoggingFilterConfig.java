package com.openclassrooms.etudiant.configuration.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configures HTTP request logging.
 */
@Configuration
public class RequestLoggingFilterConfig {
    /**
     * Creates a filter that includes query parameters and up to 10,000 characters
     * of the request body, excluding headers. Output depends on the filter logging level.
     *
     * @return the request logging filter
     */
    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
