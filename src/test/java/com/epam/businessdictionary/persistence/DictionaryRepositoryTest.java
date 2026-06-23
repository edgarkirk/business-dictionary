package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class DictionaryRepositoryTest {

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        var entry = new DictionaryEntry("SLA", "sla", "Service Level Agreement");

        var saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("SLA");
        assertThat(saved.getDefinition()).isEqualTo("Service Level Agreement");
    }

    @Test
    void findsByNormalizedTerm() {
        repository.save(new DictionaryEntry("API", "api", "Application Programming Interface"));

        var result = repository.findByNormalizedTerm("api");

        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("API");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        repository.save(new DictionaryEntry("API", "api", "Application Programming Interface"));

        assertThatThrownBy(() -> repository.saveAndFlush(
                new DictionaryEntry("API", "api", "Duplicate entry")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void persistedEntryHasNonNullTimestamps() {
        var saved = repository.save(new DictionaryEntry("SLA", "sla", "Service Level Agreement"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void createdAtDoesNotChangeOnUpdate_updatedAtChangesOnUpdate() throws InterruptedException {
        var entry = repository.saveAndFlush(
                new DictionaryEntry("KPI", "kpi", "Key Performance Indicator"));
        var originalCreatedAt = entry.getCreatedAt();
        var originalUpdatedAt = entry.getUpdatedAt();

        Thread.sleep(10);

        entry.setDefinition("Updated definition");
        var updated = repository.saveAndFlush(entry);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
