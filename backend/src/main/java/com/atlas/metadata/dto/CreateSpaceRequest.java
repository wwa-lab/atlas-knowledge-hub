package com.atlas.metadata.dto;

import com.atlas.metadata.enums.IndexStrategy;
import com.atlas.metadata.enums.SpaceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request to create a Knowledge Space. */
public record CreateSpaceRequest(
    @NotBlank @Size(max = 200) String name,
    String description,
    @NotNull SpaceType type,
    @NotNull IndexStrategy indexStrategy,
    String owner) {}
