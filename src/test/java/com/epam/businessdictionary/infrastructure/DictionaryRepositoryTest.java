package com.epam.businessdictionary.infrastructure;

import com.epam.businessdictionary.domain.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none"
})
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A boundary definition.");

        DictionaryEntry saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getDefinition()).isEqualTo("A boundary definition.");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A boundary definition.");
        repository.save(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("bounded context");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
        assertThat(found.get().getNormalizedTerm()).isEqualTo("bounded context");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = buildEntry("Bounded Context", "bounded context", "First definition.");
        DictionaryEntry duplicate = buildEntry("BOUNDED CONTEXT", "bounded context", "Duplicate definition.");
        repository.saveAndFlush(first);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setId(UUID.randomUUID());
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        return entry;
    }
}
