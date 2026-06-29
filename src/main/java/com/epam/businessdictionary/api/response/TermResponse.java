package com.epam.businessdictionary.api.response;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record TermResponse(
        @Schema(description = "Unique identifier of the term") UUID id,
        @Schema(description = "Original term text as submitted") String term,
        @Schema(description = "Definition of the term") String definition,
        @Schema(description = "Timestamp when the term was created") Instant createdAt,
        @Schema(description = "Timestamp when the term was last updated") Instant updatedAt) {

    public static TermResponse from(BusinessDictionaryEntry entry) {
        return new TermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
