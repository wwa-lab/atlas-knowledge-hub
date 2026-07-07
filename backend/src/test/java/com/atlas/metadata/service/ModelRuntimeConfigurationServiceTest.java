package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.ConfiguredModelAdapter;
import com.atlas.metadata.dto.SaveModelConfigurationRequest;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for runtime model configuration and masking. */
class ModelRuntimeConfigurationServiceTest {

  @Test
  void saveReadAndClearDeepSeekConfigurationWithoutReturningRawKey() {
    ModelRuntimeConfigurationService service =
        new ModelRuntimeConfigurationService(
            Map.of(
                "ATLAS_MODEL_PROVIDER",
                "deepseek",
                "ATLAS_MODEL_ENDPOINT",
                "https://api.deepseek.com",
                "ATLAS_MODEL_API_KEY",
                "env-secret",
                "ATLAS_MODEL_NAME",
                "deepseek-env"));

    var envState = service.readChatConfiguration();

    assertThat(envState.credentialStatus()).isEqualTo("ENV_CONFIGURED");
    assertThat(envState.modelKey()).isEqualTo("deepseek-env");
    assertThat(envState.secretStatuses())
        .anySatisfy(
            status -> {
              assertThat(status.reference().key()).isEqualTo("credential");
              assertThat(status.status()).isEqualTo("ENV_CONFIGURED");
              assertThat(status.reference().provider()).isEqualTo("deepseek");
              assertThat(status.maskedLabel()).isEqualTo("Configured from environment");
            });
    assertThat(envState.toString()).doesNotContain("env-secret", "api.deepseek.com");

    var saved =
        service.saveChatConfiguration(
            new SaveModelConfigurationRequest(
                "deepseek", "https://api.deepseek.com/", "deepseek-chat", "runtime-secret"));

    assertThat(saved.credentialStatus()).isEqualTo("CONFIGURED");
    assertThat(saved.modelKey()).isEqualTo("deepseek-chat");
    assertThat(saved.secretStatuses())
        .anySatisfy(
            status -> {
              assertThat(status.reference().key()).isEqualTo("credential");
              assertThat(status.status()).isEqualTo("CONFIGURED");
              assertThat(status.source()).isEqualTo("runtime");
            });
    assertThat(saved.toString()).doesNotContain("runtime-secret", "api.deepseek.com");
    assertThat(service.effectiveEnvironment())
        .containsEntry("ATLAS_MODEL_API_KEY", "runtime-secret")
        .containsEntry("ATLAS_MODEL_ENDPOINT", "https://api.deepseek.com");

    var cleared = service.clearChatConfiguration();

    assertThat(cleared.credentialStatus()).isEqualTo("ENV_CONFIGURED");
    assertThat(service.effectiveEnvironment()).containsEntry("ATLAS_MODEL_API_KEY", "env-secret");
  }

  @Test
  void saveCanRetainExistingMaskedCredentialWhenUpdatingRuntimeSettings() {
    ModelRuntimeConfigurationService service =
        new ModelRuntimeConfigurationService(
            Map.of(
                "ATLAS_MODEL_PROVIDER",
                "deepseek",
                "ATLAS_MODEL_ENDPOINT",
                "https://api.deepseek.com",
                "ATLAS_MODEL_API_KEY",
                "env-secret",
                "ATLAS_MODEL_NAME",
                "deepseek-chat"));

    var saved =
        service.saveChatConfiguration(
            new SaveModelConfigurationRequest(
                "deepseek", "https://api.deepseek.com/v1/", "deepseek-reasoner", ""));

    assertThat(saved.credentialStatus()).isEqualTo("CONFIGURED");
    assertThat(saved.modelKey()).isEqualTo("deepseek-reasoner");
    assertThat(service.effectiveEnvironment())
        .containsEntry("ATLAS_MODEL_API_KEY", "env-secret")
        .containsEntry("ATLAS_MODEL_ENDPOINT", "https://api.deepseek.com/v1")
        .containsEntry("ATLAS_MODEL_NAME", "deepseek-reasoner");
    assertThat(saved.toString()).doesNotContain("env-secret", "api.deepseek.com");
  }

  @Test
  void saveRejectsUnsupportedProviderBlankKeyAndUnsafeEndpoint() {
    ModelRuntimeConfigurationService service = new ModelRuntimeConfigurationService(Map.of());

    assertThatThrownBy(
            () ->
                service.saveChatConfiguration(
                    new SaveModelConfigurationRequest("github-models", "ftp://example.com", "model", "")))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsKeys("provider", "endpoint", "apiKey"));
  }

  @Test
  void configuredAdapterUsesRuntimeConfigurationWithoutRestart() {
    ModelRuntimeConfigurationService service = new ModelRuntimeConfigurationService(Map.of());
    ConfiguredModelAdapter adapter =
        new ConfiguredModelAdapter(
            service::effectiveEnvironment,
            request -> new ConfiguredModelAdapter.ChatCompletionResponse("Configured answer.", 4, 2));

    assertThat(adapter.capabilities().getFirst().status()).isEqualTo(ModelAdapterStatus.MISCONFIGURED);

    service.saveChatConfiguration(
        new SaveModelConfigurationRequest("deepseek", null, "deepseek-runtime", "runtime-secret"));

    var capability = adapter.capabilities().getFirst();

    assertThat(capability.status()).isEqualTo(ModelAdapterStatus.AVAILABLE);
    assertThat(capability.modelKey()).isEqualTo("deepseek-runtime");
    assertThat(capability.maskedConfigSummary().values()).allSatisfy(value -> assertThat(value).doesNotContain("runtime-secret"));
  }
}
