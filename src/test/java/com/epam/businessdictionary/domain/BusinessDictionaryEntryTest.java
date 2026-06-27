package com.epam.businessdictionary.domain;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class BusinessDictionaryEntryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persists_entry_with_all_fields() {
        var entry = new BusinessDictionaryEntry("Java", "java", "A programming language");

        var saved = entityManager.persistFlushFind(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Java");
        assertThat(saved.getNormalizedTerm()).isEqualTo("java");
        assertThat(saved.getDefinition()).isEqualTo("A programming language");
    }

    @Test
    void populates_audit_timestamps_on_create() {
        var entry = new BusinessDictionaryEntry("Kafka", "kafka", "A distributed event streaming platform");

        var saved = entityManager.persistFlushFind(entry);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_is_not_changed_by_update() {
        var entry = new BusinessDictionaryEntry("REST", "rest", "Original definition");
        var saved = entityManager.persistFlushFind(entry);
        var originalCreatedAt = saved.getCreatedAt();

        saved.updateDefinition("Updated definition");
        entityManager.flush();
        entityManager.refresh(saved);

        assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void update_definition_changes_definition_field() {
        var entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        var saved = entityManager.persistAndFlush(entry);

        saved.updateDefinition("A way for two applications to talk to each other");
        entityManager.flush();
        entityManager.refresh(saved);

        assertThat(saved.getDefinition()).isEqualTo("A way for two applications to talk to each other");
    }

    @Test
    void enforces_normalized_term_unique_constraint() {
        entityManager.persistAndFlush(new BusinessDictionaryEntry("Java", "java", "First definition"));
        entityManager.clear();

        var duplicate = new BusinessDictionaryEntry("JAVA", "java", "Second definition");
        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }

    @Test
    void rejects_null_term() {
        var entry = new BusinessDictionaryEntry(null, null, "A definition");
        assertThatThrownBy(() -> entityManager.persistAndFlush(entry))
                .isInstanceOf(Exception.class);
    }

    @Test
    void rejects_null_definition() {
        var entry = new BusinessDictionaryEntry("Java", "java", null);
        assertThatThrownBy(() -> entityManager.persistAndFlush(entry))
                .isInstanceOf(Exception.class);
    }
}
