package com.epam.businessdictionary.api.response;

import java.time.Instant;
import java.util.UUID;

public class DictionaryTermResponse {

    private UUID id;
    private String term;
    private String definition;
    private Instant createdAt;
    private Instant updatedAt;

    public DictionaryTermResponse(UUID id, String term, String definition, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
