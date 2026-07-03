package com.atlas.metadata.controller;

import com.atlas.metadata.exception.RequestValidationException;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Shared pagination helpers for controllers. */
final class PageRequests {

  private static final int MAX_SIZE = 200;

  private PageRequests() {}

  /** Creates a bounded pageable from API query parameters. */
  static Pageable of(int page, int size, Sort sort) {
    if (page < 0) {
      throw new RequestValidationException(Map.of("page", "must be greater than or equal to 0"));
    }
    if (size < 1 || size > MAX_SIZE) {
      throw new RequestValidationException(Map.of("size", "must be between 1 and 200"));
    }
    return PageRequest.of(page, size, sort);
  }

  /** Builds response pagination metadata. */
  static com.atlas.metadata.dto.PageMeta meta(Page<?> page) {
    return new com.atlas.metadata.dto.PageMeta(
        page.getNumber(), page.getSize(), page.getTotalElements());
  }
}
