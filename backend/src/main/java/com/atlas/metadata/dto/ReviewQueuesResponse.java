package com.atlas.metadata.dto;

import java.util.List;

/** Review queue summary for a Knowledge Space. */
public record ReviewQueuesResponse(String spaceId, List<ReviewQueueItemResponse> queues) {}
