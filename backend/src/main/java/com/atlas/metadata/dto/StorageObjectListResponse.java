package com.atlas.metadata.dto;

import java.util.List;

/** Paginated stored-object descriptor list response body. */
public record StorageObjectListResponse(List<StorageObjectResponse> objects) {}
