package com.epam.businessdictionary.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTermRequest(
        @NotBlank
        @Size(max = 1000)
        @Schema(description = "Updated definition", maxLength = 1000)
        String definition
) {}
