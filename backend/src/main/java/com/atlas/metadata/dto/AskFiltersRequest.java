package com.atlas.metadata.dto;

import com.atlas.metadata.enums.SourceType;
import java.util.List;

/** Optional trusted ask retrieval filters. */
public record AskFiltersRequest(List<String> fileItemIds, List<SourceType> sourceTypes) {}
