package com.epam.businessdictionary.api.response;

import java.time.Instant;
import java.util.UUID;

public record TermResponse(
        UUID id,
        String term,
        String definition,
        Instant createdAt,
        Instant updatedAt) {
}
