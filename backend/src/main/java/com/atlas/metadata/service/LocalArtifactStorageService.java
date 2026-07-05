package com.atlas.metadata.service;

import com.atlas.metadata.exception.RequestValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Local artifact storage for uploaded source files and generated Markdown. */
@Service
public class LocalArtifactStorageService {

  private static final String ARTIFACT_ROOT_ENV = "ATLAS_ARTIFACT_ROOT";

  private final Path root;

  /** Creates storage using ATLAS_ARTIFACT_ROOT or a local tmp default. */
  public LocalArtifactStorageService() {
    this(defaultRoot());
  }

  /** Creates storage rooted at the provided path. */
  public LocalArtifactStorageService(Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  /** Stores a PDF upload and returns its relative artifact path. */
  public StoredArtifact storeUploadedPdf(String spaceId, String originalFilename, InputStream input) {
    String safeName = safeFileName(originalFilename);
    String relativePath =
        "uploads/"
            + safeSegment(spaceId, "space")
            + "/"
            + UUID.randomUUID().toString().substring(0, 12)
            + "/"
            + safeName;
    Path target = resolveForWrite(relativePath);
    try {
      Files.createDirectories(target.getParent());
      Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to store uploaded PDF safely.");
    }
    return new StoredArtifact(safeName, relativePath);
  }

  /** Writes generated Markdown and returns its relative artifact path. */
  public String writeGeneratedMarkdown(String markdownRoot, String fileId, String markdown) {
    String relativePath =
        cleanRelativeRoot(markdownRoot, "generated/markdown") + "/" + safeSegment(fileId, "file") + ".md";
    Path target = resolveForWrite(relativePath);
    try {
      Files.createDirectories(target.getParent());
      Files.writeString(target, markdown == null ? "" : markdown, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to write generated Markdown safely.");
    }
    return relativePath;
  }

  /** Reads a stored UTF-8 text artifact from a safe relative path. */
  public String readText(String relativePath) {
    Path target = resolveForWrite(relativePath);
    try {
      return Files.readString(target, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read text artifact safely.");
    }
  }

  /** Rewrites a stored UTF-8 text artifact at a safe relative path. */
  public void writeText(String relativePath, String content) {
    Path target = resolveForWrite(relativePath);
    try {
      Files.createDirectories(target.getParent());
      Files.writeString(target, content == null ? "" : content, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to write text artifact safely.");
    }
  }

  /** Resolves a stored relative path inside the artifact root. */
  public Path resolve(String relativePath) {
    return resolveForWrite(relativePath);
  }

  /** Converts an untrusted filename to a storage-safe leaf filename. */
  public String safeFileName(String originalFilename) {
    String normalized = originalFilename == null ? "" : originalFilename.replace('\\', '/');
    int lastSlash = normalized.lastIndexOf('/');
    String leaf = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    String safe = leaf.replaceAll("[^A-Za-z0-9._-]", "-").replaceAll("^[.]+", "");
    if (safe.isBlank()) {
      return "document.pdf";
    }
    return safe;
  }

  private Path resolveForWrite(String relativePath) {
    String cleaned = cleanRelativeRoot(relativePath, "");
    if (cleaned.isBlank()) {
      throw new RequestValidationException(Map.of("path", "must be a safe relative path"));
    }
    Path target = root.resolve(cleaned).normalize();
    if (!target.startsWith(root)) {
      throw new RequestValidationException(Map.of("path", "must remain inside artifact root"));
    }
    return target;
  }

  private String cleanRelativeRoot(String value, String defaultValue) {
    String cleaned = value == null || value.isBlank() ? defaultValue : value.trim().replace('\\', '/');
    cleaned = cleaned.replaceAll("^/+", "").replaceAll("/+$", "");
    if (cleaned.contains("..")) {
      throw new RequestValidationException(Map.of("path", "must not contain traversal segments"));
    }
    return cleaned;
  }

  private String safeSegment(String value, String defaultValue) {
    String safe = (value == null ? "" : value).replaceAll("[^A-Za-z0-9._-]", "-");
    return safe.isBlank() ? defaultValue : safe;
  }

  private static Path defaultRoot() {
    String configured = System.getenv(ARTIFACT_ROOT_ENV);
    return Path.of(configured == null || configured.isBlank() ? "tmp/atlas-artifacts" : configured);
  }

  /** Stored artifact metadata. */
  public record StoredArtifact(String fileName, String relativePath) {}
}
