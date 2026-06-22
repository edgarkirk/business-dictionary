package com.epam.businessdictionary;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        return entry;
    }

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = buildEntry("api", "api", "Application Programming Interface");
        DictionaryEntry saved = repository.saveAndFlush(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("api");
        assertThat(saved.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void findsEntryByNormalizedTerm() {
        repository.saveAndFlush(buildEntry("api", "api", "Application Programming Interface"));

        Optional<DictionaryEntry> result = repository.findByNormalizedTerm("api");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("api");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        repository.saveAndFlush(buildEntry("api", "api", "Application Programming Interface"));

        assertThatThrownBy(() -> repository.saveAndFlush(buildEntry("API", "api", "Another definition")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistedEntryHasNonNullAuditTimestamps() {
        DictionaryEntry saved = repository.saveAndFlush(
                buildEntry("api", "api", "Application Programming Interface"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void createdAtDoesNotChangeOnUpdate() throws InterruptedException {
        DictionaryEntry entry = repository.saveAndFlush(
                buildEntry("api", "api", "Application Programming Interface"));
        var originalCreatedAt = entry.getCreatedAt();

        Thread.sleep(10);
        entry.setDefinition("Updated definition");
        DictionaryEntry updated = repository.saveAndFlush(entry);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }
}
