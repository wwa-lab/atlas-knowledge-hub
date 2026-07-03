package com.atlas.metadata.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates that a path is relative and traversal-free. */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RelativePathValidator.class)
public @interface RelativePath {

  /** Validation message. */
  String message() default "must be a relative path without traversal";

  /** Bean Validation groups. */
  Class<?>[] groups() default {};

  /** Bean Validation payload. */
  Class<? extends Payload>[] payload() default {};
}
