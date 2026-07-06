package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.AuditEvent;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.AuditEventRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuditLogServiceTest {

  @Test
  void recordKeepsOnlyAllowlistedMetadataAndSanitizesSummary() {
    AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
    SpaceRepository spaceRepository = Mockito.mock(SpaceRepository.class);
    Clock clock = Clock.fixed(Instant.parse("2026-07-06T00:00:00Z"), ZoneOffset.UTC);
    when(spaceRepository.existsById("ibm-i-modernization")).thenReturn(true);
    when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AuditLogService service = new AuditLogService(repository, spaceRepository, clock);
    AuditEvent event =
        service.record(
            new AuditLogService.CreateAuditEventCommand(
                "mock-owner",
                "Atlas Owner",
                "SETTINGS_UPDATED",
                AuditCategory.SETTINGS,
                AuditResult.SUCCEEDED,
                AuditSeverity.NOTICE,
                "ibm-i-modernization",
                "model_configuration",
                "deepseek",
                "req-001",
                "Saved runtime token api_key=secret",
                Map.of(
                    "adapterKey", "deepseek",
                    "unexpectedSafeLookingKey", "should not pass allowlist",
                    "token", "runtime-secret",
                    "endpoint", "https://internal.example.test/v1",
                    "privatePath", System.getProperty("user.home") + "/atlas.txt",
                    "count", 2)));

    assertThat(event.getSafeSummary()).isEqualTo("Audit event recorded with safe metadata.");
    assertThat(event.getMetadata()).containsEntry("adapterKey", "deepseek").containsEntry("count", 2);
    assertThat(event.getMetadata())
        .doesNotContainKeys("token", "endpoint", "privatePath", "unexpectedSafeLookingKey");
  }

  @Test
  void rejectsInvalidTimeRangeFilters() {
    AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
    SpaceRepository spaceRepository = Mockito.mock(SpaceRepository.class);
    when(spaceRepository.existsById("ibm-i-modernization")).thenReturn(true);

    AuditLogService service =
        new AuditLogService(
            repository,
            spaceRepository,
            Clock.fixed(Instant.parse("2026-07-06T00:00:00Z"), ZoneOffset.UTC));

    OffsetDateTime from = OffsetDateTime.parse("2026-07-06T01:00:00Z");
    OffsetDateTime to = OffsetDateTime.parse("2026-07-06T00:00:00Z");

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () ->
                service.list(
                    "ibm-i-modernization",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    from,
                    to,
                    0,
                    25))
        .isInstanceOf(com.atlas.metadata.exception.RequestValidationException.class)
        .extracting(error -> ((RequestValidationException) error).getFields())
        .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
        .containsKey("createdFrom");
  }
}
