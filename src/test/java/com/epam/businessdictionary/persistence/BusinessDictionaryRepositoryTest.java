package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void saves_and_finds_by_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("API", "api", "Application Programming Interface"));

        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("api");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("API");
        assertThat(found.get().getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void returns_empty_for_non_existent_term() {
        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void sets_audit_fields_on_creation() {
        BusinessDictionaryEntry entry =
                repository.saveAndFlush(new BusinessDictionaryEntry("ROI", "roi", "Return on Investment"));

        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_does_not_change_after_update() {
        BusinessDictionaryEntry entry =
                repository.saveAndFlush(new BusinessDictionaryEntry("KPI", "kpi", "Key Performance Indicator"));
        var originalCreatedAt = entry.getCreatedAt();

        entry.updateDefinition("Key Performance Indicators used to measure success");
        repository.saveAndFlush(entry);

        assertThat(entry.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void throws_on_duplicate_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("SLA", "sla", "Service Level Agreement"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new BusinessDictionaryEntry("SLA", "sla", "Something else")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
