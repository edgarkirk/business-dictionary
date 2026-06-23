package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class DictionaryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = new DictionaryEntry("Bounded Context", "bounded context",
                "A boundary within a domain model.");

        DictionaryEntry saved = repository.save(entry);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getNormalizedTerm()).isEqualTo("bounded context");
        assertThat(saved.getDefinition()).isEqualTo("A boundary within a domain model.");
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = new DictionaryEntry("Bounded Context", "bounded context",
                "A boundary within a domain model.");
        entityManager.persistAndFlush(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("bounded context");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = new DictionaryEntry("Bounded Context", "bounded context", "First definition.");
        entityManager.persistAndFlush(first);

        DictionaryEntry duplicate = new DictionaryEntry("BOUNDED CONTEXT", "bounded context", "Second definition.");

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistedEntryHasNonNullCreatedAtAndUpdatedAt() {
        DictionaryEntry entry = new DictionaryEntry("Event Sourcing", "event sourcing",
                "Storing state as a sequence of events.");
        entityManager.persistAndFlush(entry);

        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void createdAtDoesNotChangeOnUpdate_updatedAtChangesOnUpdate() throws InterruptedException {
        DictionaryEntry entry = new DictionaryEntry("CQRS", "cqrs",
                "Command Query Responsibility Segregation.");
        entityManager.persistAndFlush(entry);
        entityManager.clear();

        var createdAt = repository.findByNormalizedTerm("cqrs").orElseThrow().getCreatedAt();

        Thread.sleep(10);

        DictionaryEntry loaded = repository.findByNormalizedTerm("cqrs").orElseThrow();
        loaded.setDefinition("Updated: Command Query Responsibility Segregation.");
        repository.save(loaded);
        entityManager.flush();
        entityManager.clear();

        DictionaryEntry updated = repository.findByNormalizedTerm("cqrs").orElseThrow();

        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(createdAt);
    }
}
