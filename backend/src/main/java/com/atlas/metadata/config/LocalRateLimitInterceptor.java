package com.atlas.metadata.config;

import com.atlas.metadata.exception.SafeErrorCodes;
import com.atlas.metadata.exception.SafeErrorResponseFactory;
import com.atlas.metadata.service.LocalRateLimitService;
import com.atlas.metadata.service.RateLimitDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Local mock-safe rate limiting boundary for Atlas API endpoints. */
@Component
public class LocalRateLimitInterceptor implements HandlerInterceptor {

  private static final String USER_HEADER = "X-Atlas-User";
  private static final String TEST_KEY_HEADER = "X-Atlas-Test-Rate-Key";
  private static final String TEST_LIMIT_HEADER = "X-Atlas-Test-Rate-Limit";
  private static final String TEST_WINDOW_HEADER = "X-Atlas-Test-Rate-Window-Millis";

  private final LocalRateLimitService rateLimitService;
  private final SafeErrorResponseFactory safeErrors;
  private final ObjectMapper objectMapper;

  public LocalRateLimitInterceptor(
      LocalRateLimitService rateLimitService,
      SafeErrorResponseFactory safeErrors,
      ObjectMapper objectMapper) {
    this.rateLimitService = rateLimitService;
    this.safeErrors = safeErrors;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    if ("OPTIONS".equals(request.getMethod())) {
      return true;
    }
    RateLimitDecision decision =
        rateLimitService.evaluate(
            request.getHeader(TEST_KEY_HEADER),
            request.getHeader(USER_HEADER),
            request.getRemoteAddr(),
            request.getHeader(TEST_LIMIT_HEADER),
            request.getHeader(TEST_WINDOW_HEADER));
    if (decision.allowed()) {
      return true;
    }
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
    objectMapper.writeValue(
        response.getOutputStream(),
        safeErrors.envelope(
            SafeErrorCodes.RATE_LIMITED,
            null,
            null,
            null,
            decision.retryAfterSeconds(),
            request));
    return false;
  }
}
