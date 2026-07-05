package com.atlas.metadata.enums;

import java.util.Locale;
import java.util.Optional;

/** Space and platform roles used by Atlas RBAC. */
public enum AtlasRole {
  VIEWER,
  EDITOR,
  KNOWLEDGE_MANAGER,
  SPACE_OWNER,
  AUDITOR,
  PLATFORM_ADMIN;

  /** Parses canonical and legacy mock role names. */
  public static Optional<AtlasRole> fromHeader(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    return switch (normalized) {
      case "ADMIN" -> Optional.of(PLATFORM_ADMIN);
      case "OWNER" -> Optional.of(SPACE_OWNER);
      case "REVIEWER" -> Optional.of(KNOWLEDGE_MANAGER);
      default -> {
        try {
          yield Optional.of(AtlasRole.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
          yield Optional.empty();
        }
      }
    };
  }
}
