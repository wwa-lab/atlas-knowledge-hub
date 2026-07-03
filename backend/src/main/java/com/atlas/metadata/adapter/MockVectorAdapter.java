package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorAdapterStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Deterministic in-memory vector adapter for mock-only CI verification. */
@Component
public class MockVectorAdapter implements VectorAdapter {

  private final Map<String, StoredVector> items = new ConcurrentHashMap<>();

  @Override
  public VectorCapability capability() {
    return new VectorCapability(
        "mock-vector",
        "Mock Vector Adapter",
        "mock-1",
        List.of(3, 384, 768, 1536),
        List.of("INDEX", "DEINDEX", "QUERY"),
        true,
        VectorAdapterStatus.AVAILABLE,
        Map.of("engine", "mock", "endpoint", "not_configured", "credentials", "not_configured"));
  }

  @Override
  public VectorIndexResult index(VectorIndexRequest request) {
    List<VectorIndexResult.VectorItemResult> results =
        request.items().stream()
            .map(
                item -> {
                  String key = request.spaceId() + "/" + item.sourceChunkId();
                  List<BigDecimal> vector = effectiveVector(item.sourceChunkId(), item.vector(), request.dimension());
                  items.put(key, new StoredVector(item.sourceChunkId(), key, vector));
                  return new VectorIndexResult.VectorItemResult(
                      item.sourceChunkId(), key, VectorItemStatus.INDEXED, null, null);
                })
            .toList();
    return new VectorIndexResult("mock-vector", results, "Vector index completed.");
  }

  @Override
  public VectorDeleteResult delete(VectorDeleteRequest request) {
    List<VectorDeleteResult.VectorDeletedItem> results =
        request.sourceChunkIds().stream()
            .map(
                chunkId -> {
                  String key = request.spaceId() + "/" + chunkId;
                  StoredVector removed = items.remove(key);
                  return new VectorDeleteResult.VectorDeletedItem(
                      chunkId,
                      key,
                      removed == null ? VectorItemStatus.SKIPPED : VectorItemStatus.DELETED,
                      removed == null ? "Skipped: vector item was not indexed." : null);
                })
            .toList();
    return new VectorDeleteResult("mock-vector", results, "Vector delete completed.");
  }

  @Override
  public VectorQueryResult query(VectorQueryRequest request) {
    List<BigDecimal> queryVector =
        request.queryVector() == null || request.queryVector().isEmpty()
            ? deterministicVector(request.mockQuery() == null ? request.spaceId() : request.mockQuery(), 3)
            : request.queryVector();
    List<VectorQueryResult.VectorMatch> matches =
        items.values().stream()
            .filter(item -> item.vector().size() == queryVector.size())
            .map(
                item ->
                    new VectorQueryResult.VectorMatch(
                        item.sourceChunkId(),
                        item.vectorItemKey(),
                        similarity(queryVector, item.vector()),
                        Map.of("vectorItemKey", item.vectorItemKey())))
            .sorted(
                Comparator.comparing(VectorQueryResult.VectorMatch::score)
                    .reversed()
                    .thenComparing(VectorQueryResult.VectorMatch::sourceChunkId))
            .limit(request.limit())
            .toList();
    return new VectorQueryResult("mock-vector", matches, matches.size() + " match returned.");
  }

  private List<BigDecimal> effectiveVector(String seed, List<BigDecimal> vector, Integer dimension) {
    if (vector != null && !vector.isEmpty()) {
      return vector;
    }
    return deterministicVector(seed, dimension == null ? 3 : dimension);
  }

  private List<BigDecimal> deterministicVector(String seed, int dimension) {
    int hash = Math.abs(seed == null ? 0 : seed.hashCode());
    return java.util.stream.IntStream.range(0, dimension)
        .mapToObj(index -> BigDecimal.valueOf(((hash + index * 31) % 100) / 100.0).setScale(3, RoundingMode.HALF_UP))
        .toList();
  }

  private BigDecimal similarity(List<BigDecimal> left, List<BigDecimal> right) {
    BigDecimal distance = BigDecimal.ZERO;
    for (int index = 0; index < left.size(); index++) {
      distance = distance.add(left.get(index).subtract(right.get(index)).abs());
    }
    BigDecimal normalized =
        BigDecimal.ONE.subtract(distance.divide(BigDecimal.valueOf(left.size()), 6, RoundingMode.HALF_UP));
    if (normalized.compareTo(BigDecimal.ZERO) < 0) {
      return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }
    if (normalized.compareTo(BigDecimal.ONE) > 0) {
      return BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);
    }
    return normalized.setScale(3, RoundingMode.HALF_UP);
  }

  private record StoredVector(String sourceChunkId, String vectorItemKey, List<BigDecimal> vector) {}
}
