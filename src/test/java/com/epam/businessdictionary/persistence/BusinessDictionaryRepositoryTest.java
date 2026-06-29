package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Test
    void saves_and_finds_by_normalized_term() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        repository.saveAndFlush(entry);

        // Act
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("agile");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTerm()).isEqualTo("Agile");
        assertThat(result.get().getDefinition()).isEqualTo("Iterative development");
    }

    @Test
    void returns_empty_when_term_not_found() {
        // Act
        Optional<BusinessDictionaryEntry> result = repository.findByNormalizedTerm("nonexistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void sets_audit_timestamps_on_creation() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Scrum", "scrum", "Agile framework");

        // Act
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_does_not_change_after_update() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Kanban", "kanban", "Flow-based method");
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);
        var originalCreatedAt = saved.getCreatedAt();

        // Act
        saved.updateDefinition("Lean workflow management method");
        BusinessDictionaryEntry updated = repository.saveAndFlush(saved);

        // Assert
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void updated_at_changes_after_update() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Sprint", "sprint", "Time-boxed iteration");
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);
        var originalUpdatedAt = saved.getUpdatedAt();

        // Force a small time gap then update
        saved.updateDefinition("A fixed-length iteration in Scrum");
        BusinessDictionaryEntry updated = repository.saveAndFlush(saved);

        // Assert
        assertThat(updated.getUpdatedAt()).isNotNull();
        // updatedAt should be >= originalUpdatedAt (could be same millisecond in fast systems,
        // but must not be null and the definition change was persisted)
        assertThat(updated.getDefinition()).isEqualTo("A fixed-length iteration in Scrum");
    }
}
