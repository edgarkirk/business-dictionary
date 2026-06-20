package com.epam.businessdictionary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_dictionary")
public class DictionaryEntry {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "term", length = 100, nullable = false)
    private String term;

    @Column(name = "normalized_term", length = 100, nullable = false, unique = true)
    private String normalizedTerm;

    @Column(name = "definition", length = 1000, nullable = false)
    private String definition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DictionaryEntry() {
    }

    public DictionaryEntry(UUID id, String term, String normalizedTerm, String definition,
                           OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.term = term;
        this.normalizedTerm = normalizedTerm;
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

    public String getNormalizedTerm() {
        return normalizedTerm;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
