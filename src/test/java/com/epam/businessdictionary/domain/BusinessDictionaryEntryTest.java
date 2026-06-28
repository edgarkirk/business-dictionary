package com.epam.businessdictionary.domain;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(JpaAuditingConfig.class)
class BusinessDictionaryEntryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void constructor_normalizes_term_to_lowercase() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "An iterative approach.");

        assertThat(entry.getNormalizedTerm()).isEqualTo("agile");
    }

    @Test
    void constructor_preserves_original_term_casing() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("DevOps", "Dev and Ops collaboration.");

        assertThat(entry.getTerm()).isEqualTo("DevOps");
    }

    @Test
    void update_definition_changes_definition_field() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Scrum", "Old definition.");

        entry.updateDefinition("New definition.");

        assertThat(entry.getDefinition()).isEqualTo("New definition.");
    }

    @Test
    void persisted_entry_has_id_and_auditing_fields_populated() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Kanban", "Visual workflow.");

        entityManager.persistAndFlush(entry);

        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getCreatedAt()).isNotNull();
        assertThat(entry.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_is_unchanged_and_updated_at_advances_after_definition_update() throws InterruptedException {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("CI", "Continuous Integration.");
        entityManager.persistAndFlush(entry);
        Instant originalCreatedAt = entry.getCreatedAt();
        Instant originalUpdatedAt = entry.getUpdatedAt();

        Thread.sleep(50);
        entry.updateDefinition("Automated build and test on every commit.");
        entityManager.flush();

        assertThat(entry.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entry.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}
