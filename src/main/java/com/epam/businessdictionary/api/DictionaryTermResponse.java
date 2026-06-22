package com.epam.businessdictionary.api;

import java.time.Instant;
import java.util.UUID;

public class DictionaryTermResponse {

    private final UUID id;
    private final String term;
    private final String definition;
    private final Instant createdAt;
    private final Instant updatedAt;

    public DictionaryTermResponse(UUID id, String term, String definition, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }

    public String getTerm() { return term; }

    public String getDefinition() { return definition; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
