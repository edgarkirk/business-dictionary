package com.epam.businessdictionary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_dictionary")
public class DictionaryEntry {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(name = "normalized_term", nullable = false, unique = true, length = 100)
    private String normalizedTerm;

    @Column(nullable = false, length = 1000)
    private String definition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DictionaryEntry() {}

    public DictionaryEntry(UUID id, String term, String normalizedTerm, String definition, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.term = term;
        this.normalizedTerm = normalizedTerm;
        this.definition = definition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }

    public String getTerm() { return term; }

    public String getNormalizedTerm() { return normalizedTerm; }

    public String getDefinition() { return definition; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void setDefinition(String definition) { this.definition = definition; }

    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
