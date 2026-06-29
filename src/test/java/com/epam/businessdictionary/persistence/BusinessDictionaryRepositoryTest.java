package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void saves_entry_and_assigns_id_and_timestamps() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API Gateway", "api gateway", "A server-side entry point");

        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("API Gateway");
        assertThat(saved.getNormalizedTerm()).isEqualTo("api gateway");
        assertThat(saved.getDefinition()).isEqualTo("A server-side entry point");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void finds_by_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("Microservice", "microservice", "A small service"));

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("microservice");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("Microservice");
    }

    @Test
    void returns_empty_when_normalized_term_not_found() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void rejects_duplicate_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("DevOps", "devops", "First definition"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new BusinessDictionaryEntry("DEVOPS", "devops", "Duplicate entry")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void created_at_does_not_change_after_update() {
        BusinessDictionaryEntry saved = repository.saveAndFlush(
                new BusinessDictionaryEntry("SLA", "sla", "Service Level Agreement"));

        saved.updateDefinition("Updated definition");
        BusinessDictionaryEntry updated = repository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isNotNull();
    }
}
