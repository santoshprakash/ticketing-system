package com.ticketing.system.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class WebConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(ApplicationProperties properties) {
        ApplicationProperties.Cors cors = properties.cors();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(nullSafe(cors.allowedOrigins()));
        configuration.setAllowedMethods(nullSafe(cors.allowedMethods()));
        configuration.setAllowedHeaders(nullSafe(cors.allowedHeaders()));
        configuration.setExposedHeaders(nullSafe(cors.exposedHeaders()));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(cors.maxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static List<String> nullSafe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
