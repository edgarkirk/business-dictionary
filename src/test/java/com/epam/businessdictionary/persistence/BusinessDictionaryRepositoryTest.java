package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaAuditingConfig.class)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void finds_entry_by_normalized_term_when_it_exists() {
        repository.save(new BusinessDictionaryEntry("Agile", "An iterative approach."));

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("agile");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("Agile");
        assertThat(result.get().getDefinition()).isEqualTo("An iterative approach.");
    }

    @Test
    void returns_empty_when_normalized_term_not_found() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void save_populates_id_and_auditing_fields() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Scrum", "A framework.");

        repository.save(entry);

        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void duplicate_normalized_term_throws_data_integrity_violation() {
        repository.saveAndFlush(new BusinessDictionaryEntry("Kanban", "Visual management."));

        assertThatThrownBy(() ->
                repository.saveAndFlush(new BusinessDictionaryEntry("KANBAN", "Another definition.")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
