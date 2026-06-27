package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void saves_and_finds_entry_by_normalized_term() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("KPI", "Key Performance Indicator");
        repository.save(entry);

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("kpi");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("KPI");
        assertThat(result.get().getDefinition()).isEqualTo("Key Performance Indicator");
    }

    @Test
    void find_by_normalized_term_returns_empty_when_not_found() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void exists_by_normalized_term_returns_true_when_entry_exists() {
        repository.save(BusinessDictionaryEntry.of("ROI", "Return on Investment"));

        assertThat(repository.existsByNormalizedTerm("roi")).isTrue();
    }

    @Test
    void exists_by_normalized_term_returns_false_when_entry_does_not_exist() {
        assertThat(repository.existsByNormalizedTerm("nonexistent")).isFalse();
    }

    @Test
    void saves_entry_with_non_null_audit_timestamps() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("MVP", "Minimum Viable Product");
        repository.saveAndFlush(entry);

        Optional<BusinessDictionaryEntry> saved = repository.findByNormalizedTerm("mvp");

        assertThat(saved).isPresent();
        assertThat(saved.get().getCreatedAt()).isNotNull();
        assertThat(saved.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_unchanged_and_updated_at_refreshes_after_definition_update() throws InterruptedException {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("API", "Application Programming Interface");
        repository.saveAndFlush(entry);
        Instant originalCreatedAt = entry.getCreatedAt();
        Instant originalUpdatedAt = entry.getUpdatedAt();

        Thread.sleep(10);
        entry.updateDefinition("Application Program Interface");
        repository.saveAndFlush(entry);

        assertThat(entry.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entry.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}
