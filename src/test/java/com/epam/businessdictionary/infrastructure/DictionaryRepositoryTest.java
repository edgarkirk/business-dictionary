package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = new DictionaryEntry(
                "Bounded Context", "bounded context", "A boundary within a domain model.", Instant.now(), Instant.now()
        );

        DictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getNormalizedTerm()).isEqualTo("bounded context");
        assertThat(saved.getDefinition()).isEqualTo("A boundary within a domain model.");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = new DictionaryEntry(
                "Ubiquitous Language", "ubiquitous language", "Shared vocabulary in a bounded context.", Instant.now(), Instant.now()
        );
        repository.saveAndFlush(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("ubiquitous language");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Ubiquitous Language");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = new DictionaryEntry(
                "Aggregate", "aggregate", "A cluster of entities.", Instant.now(), Instant.now()
        );
        repository.saveAndFlush(first);

        DictionaryEntry duplicate = new DictionaryEntry(
                "AGGREGATE", "aggregate", "Another definition.", Instant.now(), Instant.now()
        );

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
