package com.atlas.metadata.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Local browser E2E CORS policy for API-backed frontend verification. */
@Configuration
public class LocalCorsConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  public LocalCorsConfig(
      @Value(
              "${atlas.cors.allowed-origins:http://127.0.0.1:4173,http://localhost:4173,http://127.0.0.1:5173,http://localhost:5173}")
          String allowedOrigins) {
    this.allowedOrigins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toArray(String[]::new);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(false);
  }
}
