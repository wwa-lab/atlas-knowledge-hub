package com.atlas.metadata.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Validator rejecting absolute, host-prefixed, drive-prefixed, and traversal paths. */
public class RelativePathValidator implements ConstraintValidator<RelativePath, String> {

  private static final Pattern DRIVE_PREFIX = Pattern.compile("^[A-Za-z]:.*");
  private static final Pattern URI_PREFIX = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:.*");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    if (value.startsWith("/") || value.startsWith("\\") || value.startsWith("//")) {
      return false;
    }
    if (value.contains("\\") || DRIVE_PREFIX.matcher(value).matches()) {
      return false;
    }
    if (URI_PREFIX.matcher(value).matches()) {
      return false;
    }
    try {
      Path normalized = Path.of(value).normalize();
      if (normalized.isAbsolute()) {
        return false;
      }
      for (Path part : normalized) {
        if ("..".equals(part.toString())) {
          return false;
        }
      }
      return !normalized.toString().startsWith("..");
    } catch (InvalidPathException ex) {
      return false;
    }
  }
}
