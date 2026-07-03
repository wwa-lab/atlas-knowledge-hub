package com.atlas.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Atlas metadata API Spring Boot entry point. */
@SpringBootApplication
public class MetadataApiApplication {

  /** Starts the internal metadata API. */
  public static void main(String[] args) {
    SpringApplication.run(MetadataApiApplication.class, args);
  }
}
