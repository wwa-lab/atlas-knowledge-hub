package com.atlas.metadata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers Atlas API auth enforcement. */
@Configuration
public class AtlasAuthWebConfig implements WebMvcConfigurer {

  private final AtlasAuthInterceptor authInterceptor;

  public AtlasAuthWebConfig(AtlasAuthInterceptor authInterceptor) {
    this.authInterceptor = authInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
  }
}
