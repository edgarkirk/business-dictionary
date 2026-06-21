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
    private UUID id;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(name = "normalized_term", nullable = false, unique = true, length = 100)
    private String normalizedTerm;

    @Column(nullable = false, length = 1000)
    private String definition;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public String getNormalizedTerm() {
        return normalizedTerm;
    }

    public void setNormalizedTerm(String normalizedTerm) {
        this.normalizedTerm = normalizedTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
