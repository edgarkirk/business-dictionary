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
public class BusinessTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "term", nullable = false, length = 100)
    private String term;

    @Column(name = "normalized_term", nullable = false, length = 100, unique = true)
    private String normalizedTerm;

    @Column(name = "definition", nullable = false, length = 1000)
    private String definition;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BusinessTerm() {
    }

    public BusinessTerm(String term, String definition) {
        this.term = term;
        this.normalizedTerm = term.toLowerCase();
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

    public void updateDefinition(String newDefinition) {
        if (newDefinition == null || newDefinition.isBlank()) {
            throw new IllegalArgumentException("Definition must not be blank");
        }
        this.definition = newDefinition;
    }
}
