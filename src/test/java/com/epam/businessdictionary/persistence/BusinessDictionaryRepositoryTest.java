package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saves_entry_and_assigns_uuid() {
        var entry = new BusinessDictionaryEntry("API", "Application Programming Interface");

        var saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void saves_entry_with_created_and_updated_timestamps() {
        var entry = new BusinessDictionaryEntry("SDK", "Software Development Kit");

        repository.saveAndFlush(entry);

        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void finds_entry_by_normalized_term() {
        var entry = new BusinessDictionaryEntry("REST", "Representational State Transfer");
        repository.saveAndFlush(entry);

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("rest");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("REST");
        assertThat(result.get().getDefinition()).isEqualTo("Representational State Transfer");
    }

    @Test
    void returns_empty_optional_when_normalized_term_not_found() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void exists_by_normalized_term_returns_true_when_entry_exists() {
        var entry = new BusinessDictionaryEntry("JPA", "Java Persistence API");
        repository.saveAndFlush(entry);

        assertThat(repository.existsByNormalizedTerm("jpa")).isTrue();
    }

    @Test
    void exists_by_normalized_term_returns_false_when_entry_not_found() {
        assertThat(repository.existsByNormalizedTerm("unknown")).isFalse();
    }

    @Test
    void created_at_unchanged_after_update_definition() throws InterruptedException {
        var entry = new BusinessDictionaryEntry("ORM", "Object Relational Mapping");
        var saved = repository.saveAndFlush(entry);
        var originalCreatedAt = saved.getCreatedAt();

        Thread.sleep(10);
        saved.updateDefinition("Object Relational Mapping — revised");
        var updated = repository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void updated_at_changes_after_update_definition() throws InterruptedException {
        var entry = new BusinessDictionaryEntry("DTO", "Data Transfer Object");
        var saved = repository.saveAndFlush(entry);
        var originalUpdatedAt = saved.getUpdatedAt();

        Thread.sleep(10);
        saved.updateDefinition("Data Transfer Object — revised");
        var updated = repository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}
