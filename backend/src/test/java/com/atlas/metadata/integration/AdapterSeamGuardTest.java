package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Static guard that keeps engine details inside Phase 3 adapter scopes only. */
class AdapterSeamGuardTest {

  private static final Path BACKEND = Path.of("").toAbsolutePath();
  private static final List<String> ENGINE_REFERENCES =
      List.of(
          "document-normalize",
          "trinity-office",
          "MinerU",
          "Docling",
          "PaddleOCR",
          "pgvector",
          "Milvus",
          "Qdrant",
          "AmazonS3",
          "software.amazon.awssdk",
          "MinioClient",
          "MinIO",
          "S3Client",
          "putObject",
          "getObject",
          "OpenAI",
          "Ollama",
          "DeepSeek",
          "GitHub Models",
          "Copilot",
          "ProcessBuilder",
          "Runtime.getRuntime(",
          "WebClient",
          "RestTemplate",
          "HttpClient");

  @Test
  void adapterPackageMayContainAdapterContractsButNoOutboundNetworkClient() throws IOException {
    Path adapterDir =
        BACKEND.resolve("src/main/java/com/atlas/metadata/adapter").normalize();
    Path runtimeDir = adapterDir.resolve("runtime").normalize();
    String adapterSource = readAllExcluding(adapterDir, runtimeDir);
    String runtimeSource = readAll(runtimeDir);

    assertThat(adapterSource)
        .doesNotContain("WebClient")
        .doesNotContain("RestTemplate")
        .doesNotContain("ProcessBuilder")
        .doesNotContain("Runtime.getRuntime(");
    assertThat(runtimeSource)
        .contains("ProcessBuilder")
        .doesNotContain("WebClient")
        .doesNotContain("RestTemplate")
        .doesNotContain("Runtime.getRuntime(");
    assertThat(adapterSource)
        .contains("trinity-office")
        .contains("document-normalize")
        .contains("mock-model")
        .contains("mock-vector")
        .contains("pgvector")
        .contains("HttpClient")
        .contains("DeepSeek");
  }

  @Test
  void nonAdapterProductLayersHaveNoOutboundClientCommandRunnerOrEngineCall() throws IOException {
    Path mainDir = BACKEND.resolve("src/main/java/com/atlas/metadata").normalize();
    try (var stream = Files.walk(mainDir)) {
      List<Path> files =
          stream
              .filter(Files::isRegularFile)
              .filter(path -> !path.toString().contains("/adapter/"))
              .toList();
      for (Path file : files) {
        String source = read(file);
        for (String forbidden : ENGINE_REFERENCES) {
          assertThat(source).as(file + " must not contain " + forbidden).doesNotContain(forbidden);
        }
      }
    }
  }

  @Test
  void adapterImplementationAndDocsDoNotExposeSecretsOrPrivatePaths() throws IOException {
    List<Path> paths =
        List.of(
            BACKEND.resolve("src/main/java/com/atlas/metadata/adapter").normalize(),
            BACKEND.resolve("src/main/java/com/atlas/metadata/controller").normalize(),
            BACKEND.resolve("src/main/java/com/atlas/metadata/service").normalize(),
            BACKEND.resolve("src/main/java/com/atlas/metadata/repository").normalize(),
            BACKEND.resolve("src/main/java/com/atlas/metadata/domain").normalize(),
            BACKEND.getParent().resolve("docs/01-requirements/storage-adapter-requirements.md").normalize(),
            BACKEND.getParent().resolve("docs/02-user-stories/storage-adapter-stories.md").normalize(),
            BACKEND.getParent().resolve("docs/03-spec/storage-adapter-spec.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/storage-adapter-architecture.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/storage-adapter-data-flow.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/storage-adapter-data-model.md").normalize(),
            BACKEND.getParent().resolve("docs/05-design/storage-adapter-design.md").normalize(),
            BACKEND
                .getParent()
                .resolve("docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md")
                .normalize(),
            BACKEND.getParent().resolve("docs/06-tasks/storage-adapter-tasks.md").normalize(),
            BACKEND.getParent().resolve("docs/01-requirements/model-adapter-requirements.md").normalize(),
            BACKEND.getParent().resolve("docs/02-user-stories/model-adapter-stories.md").normalize(),
            BACKEND.getParent().resolve("docs/03-spec/model-adapter-spec.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/model-adapter-architecture.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/model-adapter-data-flow.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/model-adapter-data-model.md").normalize(),
            BACKEND.getParent().resolve("docs/05-design/model-adapter-design.md").normalize(),
            BACKEND
                .getParent()
                .resolve("docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md")
                .normalize(),
            BACKEND.getParent().resolve("docs/06-tasks/model-adapter-tasks.md").normalize(),
            BACKEND.getParent().resolve("docs/01-requirements/vector-adapter-requirements.md").normalize(),
            BACKEND.getParent().resolve("docs/02-user-stories/vector-adapter-stories.md").normalize(),
            BACKEND.getParent().resolve("docs/03-spec/vector-adapter-spec.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/vector-adapter-architecture.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/vector-adapter-data-flow.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/vector-adapter-data-model.md").normalize(),
            BACKEND.getParent().resolve("docs/05-design/vector-adapter-design.md").normalize(),
            BACKEND
                .getParent()
                .resolve("docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md")
                .normalize(),
            BACKEND.getParent().resolve("docs/06-tasks/vector-adapter-tasks.md").normalize(),
            BACKEND.getParent().resolve("docs/00-context/vector-adapter-traceability.md").normalize(),
            BACKEND.getParent().resolve("docs/01-requirements/ask-rag-requirements.md").normalize(),
            BACKEND.getParent().resolve("docs/02-user-stories/ask-rag-stories.md").normalize(),
            BACKEND.getParent().resolve("docs/03-spec/ask-rag-spec.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/ask-rag-architecture.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/ask-rag-data-flow.md").normalize(),
            BACKEND.getParent().resolve("docs/04-architecture/ask-rag-data-model.md").normalize(),
            BACKEND.getParent().resolve("docs/05-design/ask-rag-design.md").normalize(),
            BACKEND
                .getParent()
                .resolve("docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md")
                .normalize(),
            BACKEND.getParent().resolve("docs/06-tasks/ask-rag-tasks.md").normalize(),
            BACKEND.getParent().resolve("docs/00-context/ask-rag-traceability.md").normalize());
    String source =
        String.join(
            "\n",
            paths.stream()
                .filter(Files::exists)
                .map(this::readPath)
                .toList());

    assertThat(source)
        .doesNotContain("AK" + "IA")
        .doesNotContain("BE" + "GIN PRIVATE KEY")
        .doesNotContain("BE" + "GIN RSA PRIVATE KEY")
        .doesNotContain("/" + "Users/")
        .doesNotContain("C:" + "\\\\");
  }

  private String readAll(Path dir) throws IOException {
    try (var stream = Files.walk(dir)) {
      return String.join("\n", stream.filter(Files::isRegularFile).map(this::read).toList());
    }
  }

  private String readAllExcluding(Path dir, Path excludedDir) throws IOException {
    try (var stream = Files.walk(dir)) {
      return String.join(
          "\n",
          stream
              .filter(Files::isRegularFile)
              .filter(path -> !path.normalize().startsWith(excludedDir))
              .map(this::read)
              .toList());
    }
  }

  private String readPath(Path path) {
    try {
      return Files.isDirectory(path) ? readAll(path) : read(path);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private String read(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
