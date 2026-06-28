package com.epam.businessdictionary.api.response;

import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TermResponse(
        @Schema(description = "Term identifier") UUID id,
        @Schema(description = "Business term") String term,
        @Schema(description = "Term definition") String definition,
        @Schema(description = "Creation timestamp") OffsetDateTime createdAt,
        @Schema(description = "Last modification timestamp") OffsetDateTime updatedAt
) {
    public static TermResponse from(BusinessDictionaryTerm entity) {
        return new TermResponse(
                entity.getId(),
                entity.getTerm(),
                entity.getDefinition(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
