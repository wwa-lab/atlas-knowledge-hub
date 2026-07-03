package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Static guard that keeps Phase 2 adapter and engine scopes empty. */
class AdapterSeamGuardTest {

  private static final Path BACKEND = Path.of("").toAbsolutePath();

  @Test
  void adapterPackageContainsOnlyPackageInfoAndNoEngineReferences() throws IOException {
    Path adapterDir =
        BACKEND.resolve("src/main/java/com/atlas/metadata/adapter").normalize();
    List<Path> files;
    try (var stream = Files.walk(adapterDir)) {
      files = stream.filter(Files::isRegularFile).toList();
    }

    assertThat(files).singleElement().satisfies(path -> assertThat(path.getFileName().toString())
        .isEqualTo("package-info.java"));
    String adapterSource = Files.readString(files.getFirst());
    assertThat(adapterSource)
        .doesNotContain("WebClient")
        .doesNotContain("RestTemplate")
        .doesNotContain("HttpClient")
        .doesNotContain("document-normalize")
        .doesNotContain("trinity-office")
        .doesNotContain("pgvector")
        .doesNotContain("S3");
  }

  @Test
  void mainSourceHasNoOutboundHttpClientOrEngineCall() throws IOException {
    Path mainDir = BACKEND.resolve("src/main/java/com/atlas/metadata").normalize();
    String source;
    try (var stream = Files.walk(mainDir)) {
      source =
          String.join(
              "\n",
              stream
                  .filter(Files::isRegularFile)
                  .map(this::read)
                  .toList());
    }

    assertThat(source)
        .doesNotContain("WebClient")
        .doesNotContain("RestTemplate")
        .doesNotContain("HttpClient")
        .doesNotContain("document-normalize")
        .doesNotContain("trinity-office")
        .doesNotContain("MinerU")
        .doesNotContain("Docling")
        .doesNotContain("PaddleOCR")
        .doesNotContain("pgvector")
        .doesNotContain("Milvus")
        .doesNotContain("Qdrant");
  }

  private String read(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
