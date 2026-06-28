package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(BusinessDictionaryRepositoryTest.AuditingConfig.class)
class BusinessDictionaryRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingConfig {
    }

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void save_persists_entry_with_generated_uuid() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Term", "A definition");

        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Term");
        assertThat(saved.getNormalizedTerm()).isEqualTo("term");
        assertThat(saved.getDefinition()).isEqualTo("A definition");
    }

    @Test
    void save_sets_created_at_and_updated_at() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("AuditTest", "A definition");

        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void find_by_normalized_term_returns_entry_when_exists() {
        repository.saveAndFlush(new BusinessDictionaryEntry("FindMe", "A definition"));

        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("findme");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("FindMe");
    }

    @Test
    void find_by_normalized_term_returns_empty_when_not_found() {
        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void save_duplicate_normalized_term_throws_data_integrity_violation() {
        repository.saveAndFlush(new BusinessDictionaryEntry("Duplicate", "First definition"));

        BusinessDictionaryEntry duplicate = new BusinessDictionaryEntry("DUPLICATE", "Second definition");

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_definition_changes_updated_at_but_not_created_at() throws InterruptedException {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("UpdateTest", "Original");
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        Thread.sleep(10);

        saved.updateDefinition("Updated definition");
        BusinessDictionaryEntry updated = repository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(saved.getCreatedAt());
        assertThat(updated.getDefinition()).isEqualTo("Updated definition");
    }
}
