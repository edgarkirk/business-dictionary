package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class BusinessDictionaryTermRepositoryTest {

    @Autowired
    private BusinessDictionaryTermRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saves_and_finds_by_normalized_term() {
        repository.save(new BusinessDictionaryTerm("Agile", "An iterative approach to project management"));

        var found = repository.findByNormalizedTerm("agile");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Agile");
        assertThat(found.get().getDefinition()).isEqualTo("An iterative approach to project management");
    }

    @Test
    void returns_empty_optional_for_unknown_normalized_term() {
        var found = repository.findByNormalizedTerm("nonexistentterm");

        assertThat(found).isEmpty();
    }

    @Test
    void findByNormalizedTerm_does_not_match_original_casing() {
        repository.save(new BusinessDictionaryTerm("Kanban", "A visual workflow management method"));

        // normalized_term column stores "kanban"; a lookup with original casing must return empty,
        // enforcing that callers are responsible for lowercasing before lookup.
        assertThat(repository.findByNormalizedTerm("Kanban")).isEmpty();
    }

    @Test
    void duplicate_normalized_term_throws_constraint_violation() {
        repository.saveAndFlush(new BusinessDictionaryTerm("Scrum", "An agile framework"));

        assertThatThrownBy(() -> repository.saveAndFlush(new BusinessDictionaryTerm("SCRUM", "Another definition")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void created_at_and_updated_at_are_set_after_persist() {
        var term = new BusinessDictionaryTerm("Sprint", "A time-boxed development iteration");
        repository.saveAndFlush(term);

        assertThat(term.getCreatedAt()).isNotNull();
        assertThat(term.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_is_unchanged_and_definition_persisted_after_update() {
        repository.saveAndFlush(new BusinessDictionaryTerm("Framework", "A structured foundation for development"));

        // Reload from DB so originalCreatedAt has the same DB-truncated precision as later comparisons.
        entityManager.clear();
        var loaded = repository.findByNormalizedTerm("framework").orElseThrow();
        var originalCreatedAt = loaded.getCreatedAt();

        loaded.updateDefinition("An opinionated set of libraries and tools for development");
        repository.saveAndFlush(loaded);

        entityManager.clear();

        var updated = repository.findByNormalizedTerm("framework").orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getDefinition()).isEqualTo("An opinionated set of libraries and tools for development");
    }
}
