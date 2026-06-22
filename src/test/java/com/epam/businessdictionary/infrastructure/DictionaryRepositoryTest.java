package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A DDD concept.");

        DictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getDefinition()).isEqualTo("A DDD concept.");
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A DDD concept.");
        repository.saveAndFlush(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("bounded context");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = buildEntry("Bounded Context", "bounded context", "First definition.");
        repository.saveAndFlush(first);

        DictionaryEntry duplicate = buildEntry("Bounded Context", "bounded context", "Duplicate definition.");

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistedEntryHasNonNullCreatedAtAndUpdatedAt() {
        DictionaryEntry entry = buildEntry("Ubiquitous Language", "ubiquitous language", "Shared vocabulary.");

        DictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void createdAtDoesNotChangeOnUpdateUpdatedAtChangesOnUpdate() throws InterruptedException {
        DictionaryEntry entry = buildEntry("Aggregate", "aggregate", "A cluster of domain objects.");
        DictionaryEntry saved = repository.saveAndFlush(entry);

        Instant initialCreatedAt = saved.getCreatedAt();
        Instant initialUpdatedAt = saved.getUpdatedAt();

        Thread.sleep(20);

        saved.setDefinition("A cluster of domain objects treated as a single unit.");
        DictionaryEntry updated = repository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updated.getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    // ---- helpers ----

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        return entry;
    }
}
