package com.epam.businessdictionary.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DictionaryTermResponse {

    private UUID id;
    private String term;
    private String definition;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DictionaryTermResponse() {
    }

    public DictionaryTermResponse(UUID id, String term, String definition,
                                   OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
