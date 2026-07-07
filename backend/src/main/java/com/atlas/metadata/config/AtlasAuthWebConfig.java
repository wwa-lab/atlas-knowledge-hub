package com.atlas.metadata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers Atlas API auth enforcement. */
@Configuration
public class AtlasAuthWebConfig implements WebMvcConfigurer {

  private final AtlasAuthInterceptor authInterceptor;
  private final LocalRateLimitInterceptor rateLimitInterceptor;

  public AtlasAuthWebConfig(
      AtlasAuthInterceptor authInterceptor, LocalRateLimitInterceptor rateLimitInterceptor) {
    this.authInterceptor = authInterceptor;
    this.rateLimitInterceptor = rateLimitInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**").order(0);
    registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").order(1);
  }
}
