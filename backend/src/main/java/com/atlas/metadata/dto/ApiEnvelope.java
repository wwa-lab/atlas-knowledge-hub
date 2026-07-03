package com.atlas.metadata.dto;

/** Standard Atlas success/error response envelope. */
public record ApiEnvelope<T>(boolean success, T data, ErrorBody error, PageMeta meta) {

  /** Creates a non-paginated success envelope. */
  public static <T> ApiEnvelope<T> ok(T data) {
    return new ApiEnvelope<>(true, data, null, null);
  }

  /** Creates a paginated success envelope. */
  public static <T> ApiEnvelope<T> ok(T data, PageMeta meta) {
    return new ApiEnvelope<>(true, data, null, meta);
  }

  /** Creates an error envelope. */
  public static <T> ApiEnvelope<T> fail(ErrorBody error) {
    return new ApiEnvelope<>(false, null, error, null);
  }
}
