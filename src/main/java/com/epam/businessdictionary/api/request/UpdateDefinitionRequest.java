package com.epam.businessdictionary.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDefinitionRequest(
        @NotBlank @Size(max = 1000)
        @Schema(description = "The updated definition of the business term (max 1000 characters)")
        String definition) {
}
