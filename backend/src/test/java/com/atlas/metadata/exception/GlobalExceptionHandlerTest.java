package com.atlas.metadata.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.dto.ApiEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

/** Unit checks for user-safe error responses and server-side fault logging. */
@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

  @Test
  void unexpectedFaultReturnsGenericEnvelopeAndLogsCorrelationId(CapturedOutput output) {
    GlobalExceptionHandler handler =
        new GlobalExceptionHandler(new SafeErrorResponseFactory(new SafeErrorSanitizer()));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/spaces");

    ResponseEntity<ApiEnvelope<Void>> response =
        handler.onUnexpected(
            new IllegalStateException("api" + "Key=sensitive-value /" + "Users/internal/source.md"),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().error().code()).isEqualTo("SAFE_SYSTEM_ERROR");
    assertThat(response.getBody().error().message()).isEqualTo("Unexpected server error.");
    assertThat(response.getBody().error().correlationId()).isNotBlank();
    assertThat(response.toString())
        .doesNotContain("sensitive-value")
        .doesNotContain("/" + "Users/internal")
        .doesNotContain("IllegalStateException");
    assertThat(output)
        .contains("Unhandled metadata API error")
        .contains("correlationId=")
        .contains("IllegalStateException");
  }

  @Test
  void sanitizerRedactsSecretsPathsEndpointsExceptionsAndSourceBlocks() {
    SafeErrorSanitizer sanitizer = new SafeErrorSanitizer();

    String sanitized =
        sanitizer.sanitize(
            "api" + "Key=sample-value https:" + "//internal.example.test /"
                + "Users/person/source.md "
                + "java.lang.IllegalArgumentException ```raw source```");

    assertThat(sanitized)
        .doesNotContain("sample-value")
        .doesNotContain("https:" + "//internal.example.test")
        .doesNotContain("/" + "Users/person")
        .doesNotContain("IllegalArgumentException")
        .doesNotContain("raw source")
        .contains("[redacted]");
  }
}
