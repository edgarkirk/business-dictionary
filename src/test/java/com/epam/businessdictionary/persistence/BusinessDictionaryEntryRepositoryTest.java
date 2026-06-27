package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class BusinessDictionaryEntryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BusinessDictionaryEntryRepository repository;

    @Test
    void find_by_normalized_term_returns_entry_when_exists() {
        entityManager.persistAndFlush(new BusinessDictionaryEntry("Cloud", "cloud", "Cloud computing concept"));

        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("cloud");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("Cloud");
        assertThat(result.get().getNormalizedTerm()).isEqualTo("cloud");
    }

    @Test
    void find_by_normalized_term_returns_empty_when_not_exists() {
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void exists_by_normalized_term_returns_true_when_exists() {
        entityManager.persistAndFlush(new BusinessDictionaryEntry("DevOps", "devops", "Development and Operations"));

        assertThat(repository.existsByNormalizedTerm("devops")).isTrue();
    }

    @Test
    void exists_by_normalized_term_returns_false_when_not_exists() {
        assertThat(repository.existsByNormalizedTerm("nonexistent")).isFalse();
    }

    @Test
    void save_sets_created_at_and_updated_at() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");

        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_throws_on_duplicate_normalized_term() {
        entityManager.persistAndFlush(new BusinessDictionaryEntry("SLA", "sla", "Service Level Agreement"));
        entityManager.clear();

        BusinessDictionaryEntry duplicate = new BusinessDictionaryEntry("SLA", "sla", "Another definition");
        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void created_at_unchanged_after_definition_update() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        repository.saveAndFlush(entry);
        entityManager.clear();

        // Reload from DB so originalCreatedAt has DB-precision (microseconds, not nanoseconds)
        BusinessDictionaryEntry loaded = entityManager.find(BusinessDictionaryEntry.class, entry.getId());
        var originalCreatedAt = loaded.getCreatedAt();

        loaded.updateDefinition("Iterative and incremental development methodology");
        entityManager.flush();
        entityManager.refresh(loaded);

        assertThat(loaded.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(loaded.getUpdatedAt()).isNotNull();
    }
}
