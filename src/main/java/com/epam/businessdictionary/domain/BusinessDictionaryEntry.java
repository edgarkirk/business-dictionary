package com.epam.businessdictionary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "business_dictionary",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_business_dictionary_normalized_term",
                columnNames = "normalized_term"
        )
)
@EntityListeners(AuditingEntityListener.class)
public class BusinessDictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "term", nullable = false, length = 100)
    private String term;

    @Column(name = "normalized_term", nullable = false, length = 100)
    private String normalizedTerm;

    @Column(name = "definition", nullable = false, length = 1000)
    private String definition;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BusinessDictionaryEntry() {
    }

    public BusinessDictionaryEntry(String term, String definition) {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("Term must not be blank");
        }
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("Definition must not be blank");
        }
        this.term = term;
        this.normalizedTerm = term.toLowerCase();
        this.definition = definition;
    }

    public void updateDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            throw new IllegalArgumentException("Definition must not be blank");
        }
        this.definition = definition;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
