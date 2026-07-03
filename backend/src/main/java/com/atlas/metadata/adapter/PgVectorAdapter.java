package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorAdapterStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Safe configured boundary placeholder for a future pgvector-compatible adapter. */
@Component
public class PgVectorAdapter implements VectorAdapter {

  @Override
  public VectorCapability capability() {
    return new VectorCapability(
        "pgvector",
        "PgVector Adapter",
        "contract-1",
        List.of(384, 768, 1536),
        List.of("INDEX", "DEINDEX", "QUERY"),
        false,
        VectorAdapterStatus.MISCONFIGURED,
        Map.of(
            "engine",
            "pgvector",
            "endpoint",
            "not_configured",
            "collection",
            "not_configured",
            "credentials",
            "not_configured"));
  }

  @Override
  public VectorIndexResult index(VectorIndexRequest request) {
    throw new IllegalStateException("PgVector adapter is not configured.");
  }

  @Override
  public VectorDeleteResult delete(VectorDeleteRequest request) {
    throw new IllegalStateException("PgVector adapter is not configured.");
  }

  @Override
  public VectorQueryResult query(VectorQueryRequest request) {
    throw new IllegalStateException("PgVector adapter is not configured.");
  }
}
