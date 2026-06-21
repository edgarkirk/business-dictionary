package com.epam.businessdictionary.api;

import java.time.Instant;
import java.util.UUID;

public record DictionaryTermResponse(
        UUID id,
        String term,
        String definition,
        Instant createdAt,
        Instant updatedAt
) {}
