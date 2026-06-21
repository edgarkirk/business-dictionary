package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A clear boundary.");

        DictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getNormalizedTerm()).isEqualTo("bounded context");
        assertThat(saved.getDefinition()).isEqualTo("A clear boundary.");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = buildEntry("Ubiquitous Language", "ubiquitous language", "Shared domain vocabulary.");
        repository.saveAndFlush(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("ubiquitous language");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Ubiquitous Language");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry entry1 = buildEntry("Aggregate", "aggregate", "A cluster of entities.");
        DictionaryEntry entry2 = buildEntry("AGGREGATE", "aggregate", "Another definition.");
        repository.saveAndFlush(entry1);

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(entry2));
    }

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        entry.setCreatedAt(OffsetDateTime.now());
        entry.setUpdatedAt(OffsetDateTime.now());
        return entry;
    }
}
