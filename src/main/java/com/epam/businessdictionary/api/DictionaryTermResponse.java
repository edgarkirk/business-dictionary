package com.epam.businessdictionary.api;

import com.epam.businessdictionary.domain.DictionaryEntry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DictionaryTermResponse(
        UUID id,
        String term,
        String definition,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static DictionaryTermResponse from(DictionaryEntry entry) {
        return new DictionaryTermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
