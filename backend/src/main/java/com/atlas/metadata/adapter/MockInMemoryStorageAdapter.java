package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Deterministic in-memory storage adapter for CI and local contract tests. */
@Component
public class MockInMemoryStorageAdapter implements StorageAdapter {

  public static final String ADAPTER_KEY = "mock-storage";
  private static final List<StorageLayer> SUPPORTED_LAYERS = List.of(StorageLayer.values());

  private final Map<String, StorageObjectDescriptor> objects = new ConcurrentHashMap<>();

  @Override
  public StorageCapability capability() {
    return new StorageCapability(
        ADAPTER_KEY,
        "Mock In-Memory Object Storage",
        "test",
        SUPPORTED_LAYERS,
        true,
        StorageAdapterStatus.AVAILABLE,
        Map.of(
            "endpoint", "not-used",
            "bucket", "not-used",
            "region", "not-used",
            "credentials", "not-used",
            "externalNetwork", "disabled"));
  }

  @Override
  public StorageObjectDescriptor put(StoragePutRequest request) {
    String seed = request.contentRef() == null ? request.objectKey() : request.contentRef();
    StorageObjectDescriptor descriptor =
        new StorageObjectDescriptor(
            request.layer(),
            request.objectKey(),
            request.contentType(),
            (long) seed.getBytes(StandardCharsets.UTF_8).length,
            checksum(seed),
            ADAPTER_KEY,
            StorageObjectStatus.STORED,
            request.fileId(),
            null);
    objects.put(namespace(request.batchId(), request.workspaceId(), request.layer(), request.objectKey()), descriptor);
    return descriptor;
  }

  @Override
  public StorageObjectDescriptor get(StorageObjectRef ref) {
    StorageObjectDescriptor descriptor = objects.get(namespace(ref));
    if (descriptor != null) {
      return descriptor;
    }
    return missing(ref);
  }

  @Override
  public boolean exists(StorageObjectRef ref) {
    return objects.containsKey(namespace(ref));
  }

  @Override
  public StorageListResult list(StorageListRequest request) {
    int pageSize = Math.max(1, request.pageSize());
    int offset = parseOffset(request.pageToken());
    String namespacePrefix = namespacePrefix(request.batchId(), request.workspaceId(), request.layer());
    List<StorageObjectDescriptor> all =
        objects.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(namespacePrefix))
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(Map.Entry::getValue)
            .toList();
    List<StorageObjectDescriptor> page = all.stream().skip(offset).limit(pageSize).toList();
    int nextOffset = offset + page.size();
    String nextToken = nextOffset < all.size() ? Integer.toString(nextOffset) : null;
    return new StorageListResult(page, nextToken);
  }

  @Override
  public StorageDeleteResult delete(StorageObjectRef ref) {
    StorageObjectDescriptor removed = objects.remove(namespace(ref));
    if (removed == null) {
      return new StorageDeleteResult(missing(ref));
    }
    return new StorageDeleteResult(
        new StorageObjectDescriptor(
            ref.layer(),
            ref.objectKey(),
            removed.contentType(),
            removed.sizeBytes(),
            removed.checksum(),
            ADAPTER_KEY,
            StorageObjectStatus.DELETED,
            ref.fileId(),
            null));
  }

  private StorageObjectDescriptor missing(StorageObjectRef ref) {
    return new StorageObjectDescriptor(
        ref.layer(),
        ref.objectKey(),
        null,
        null,
        null,
        ADAPTER_KEY,
        StorageObjectStatus.MISSING,
        ref.fileId(),
        null);
  }

  private String namespace(StorageObjectRef ref) {
    return namespace(ref.batchId(), ref.workspaceId(), ref.layer(), ref.objectKey());
  }

  private String namespace(String batchId, String workspaceId, StorageLayer layer, String objectKey) {
    return namespacePrefix(batchId, workspaceId, layer) + objectKey;
  }

  private String namespacePrefix(String batchId, String workspaceId, StorageLayer layer) {
    return workspaceId + "/" + batchId + "/" + layer.name() + "/";
  }

  private int parseOffset(String pageToken) {
    if (pageToken == null || pageToken.isBlank()) {
      return 0;
    }
    try {
      return Math.max(0, Integer.parseInt(pageToken));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  private String checksum(String seed) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder("sha256:");
      for (byte value : bytes) {
        hex.append(String.format("%02x", value));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 digest unavailable.");
    }
  }
}
