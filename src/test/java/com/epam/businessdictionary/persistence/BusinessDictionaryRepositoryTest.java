package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void saves_entry_and_populates_audit_fields() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "Iterative delivery approach");
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Agile");
        assertThat(saved.getNormalizedTerm()).isEqualTo("agile");
        assertThat(saved.getDefinition()).isEqualTo("Iterative delivery approach");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void finds_entry_by_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("Scrum", "An agile framework"));

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("scrum");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("Scrum");
    }

    @Test
    void returns_empty_when_normalized_term_not_found() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void enforces_unique_constraint_on_normalized_term() {
        repository.saveAndFlush(new BusinessDictionaryEntry("Kanban", "A visual workflow method"));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new BusinessDictionaryEntry("kanban", "Different definition")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void created_at_does_not_change_after_update() {
        BusinessDictionaryEntry entry = repository.saveAndFlush(new BusinessDictionaryEntry("Sprint", "Time-boxed iteration"));
        var originalCreatedAt = entry.getCreatedAt();

        entry.updateDefinition("Fixed-duration development iteration");
        BusinessDictionaryEntry updated = repository.saveAndFlush(entry);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }
}
