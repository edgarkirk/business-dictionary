package com.epam.businessdictionary.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTermRequest(
        @NotBlank @Size(max = 100)
        @Schema(description = "The business term to define (max 100 characters)")
        String term,

        @NotBlank @Size(max = 1000)
        @Schema(description = "The definition of the business term (max 1000 characters)")
        String definition) {
}
