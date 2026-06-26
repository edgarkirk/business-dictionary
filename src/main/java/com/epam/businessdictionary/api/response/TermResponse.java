package com.epam.businessdictionary.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record TermResponse(
        @Schema(description = "Unique identifier of the term")
        UUID id,

        @Schema(description = "The business term as submitted")
        String term,

        @Schema(description = "The definition of the business term")
        String definition,

        @Schema(description = "Timestamp when the term was created")
        Instant createdAt,

        @Schema(description = "Timestamp when the term was last updated")
        Instant updatedAt) {
}
