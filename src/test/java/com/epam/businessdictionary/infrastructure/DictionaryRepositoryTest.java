package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "A boundary definition.");
        DictionaryEntry saved = repository.save(entry);
        repository.flush();

        Optional<DictionaryEntry> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
        assertThat(found.get().getDefinition()).isEqualTo("A boundary definition.");
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "A boundary definition.");
        repository.save(entry);
        repository.flush();

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("bounded context");
        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = entry("Bounded Context", "bounded context", "First definition.");
        repository.save(first);
        repository.flush();

        DictionaryEntry duplicate = entry("BOUNDED CONTEXT", "bounded context", "Duplicate definition.");
        assertThatThrownBy(() -> {
            repository.save(duplicate);
            repository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private DictionaryEntry entry(String term, String normalizedTerm, String definition) {
        Instant now = Instant.now();
        return new DictionaryEntry(UUID.randomUUID(), term, normalizedTerm, definition, now, now);
    }
}
