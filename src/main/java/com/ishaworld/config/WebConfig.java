package com.ishaworld.config;

import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    /**
     * app.frontend-origin: the site address, or several separated by commas (e.g. "https://example.org,https://www.example.org").
     * app.extra-origins: more addresses, which may use a wildcard for preview deploys (e.g. "https://*.my-site.pages.dev").
     */
    public WebConfig(
            @Value("${app.frontend-origin:http://localhost:5173}") String frontendOrigin,
            @Value("${app.extra-origins:}") String extraOrigins
    ) {
        this.allowedOrigins = Stream.concat(Arrays.stream(frontendOrigin.split(",")), Arrays.stream(extraOrigins.split(",")))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
