package com.epam.businessdictionary.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTermRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "Business term", maxLength = 100)
        String term,

        @NotBlank
        @Size(max = 1000)
        @Schema(description = "Term definition", maxLength = 1000)
        String definition
) {}
