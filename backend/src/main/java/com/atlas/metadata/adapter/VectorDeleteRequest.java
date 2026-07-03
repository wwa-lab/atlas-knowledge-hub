package com.atlas.metadata.adapter;

import java.util.List;

/** Product-facing vector delete request. */
public record VectorDeleteRequest(String runId, String spaceId, String batchId, List<String> sourceChunkIds) {}
