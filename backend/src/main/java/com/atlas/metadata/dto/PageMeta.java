package com.atlas.metadata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Pagination metadata for list endpoints. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageMeta(Integer page, Integer size, Long total, Integer pageSize, String nextPageToken) {

  /** Creates offset-style pagination metadata for existing list endpoints. */
  public PageMeta(int page, int size, long total) {
    this(page, size, total, null, null);
  }

  /** Creates continuation-token pagination metadata for storage object listing. */
  public static PageMeta continuation(int pageSize, String nextPageToken) {
    return new PageMeta(null, null, null, pageSize, nextPageToken);
  }
}
