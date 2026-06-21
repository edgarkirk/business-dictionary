package com.epam.businessdictionary.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DictionaryTermResponse(
        UUID id,
        String term,
        String definition,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
