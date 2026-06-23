package com.epam.businessdictionary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "business_dictionary")
@EntityListeners(AuditingEntityListener.class)
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "term", length = 100, nullable = false, updatable = false)
    private String term;

    @Column(name = "normalized_term", length = 100, nullable = false, unique = true, updatable = false)
    private String normalizedTerm;

    @Column(name = "definition", length = 1000, nullable = false)
    private String definition;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DictionaryEntry() {}

    public DictionaryEntry(String term, String normalizedTerm, String definition) {
        this.term = term;
        this.normalizedTerm = normalizedTerm;
        this.definition = definition;
    }

    public UUID getId() { return id; }

    public String getTerm() { return term; }

    public String getNormalizedTerm() { return normalizedTerm; }

    public String getDefinition() { return definition; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
