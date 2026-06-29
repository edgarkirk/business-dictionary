package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class BusinessDictionaryRepositoryTest {

    @Autowired
    private BusinessDictionaryRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saves_and_finds_by_normalized_term() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        repository.saveAndFlush(entry);
        entityManager.clear();

        // Act
        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("api");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("API");
        assertThat(found.get().getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void returns_empty_for_unknown_term() {
        // Act
        Optional<BusinessDictionaryEntry> found = repository.findByNormalizedTerm("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void enforces_unique_normalized_term() {
        // Arrange
        repository.saveAndFlush(new BusinessDictionaryEntry("API", "api", "First definition"));
        entityManager.clear();

        // Act & Assert
        assertThatThrownBy(() ->
                repository.saveAndFlush(new BusinessDictionaryEntry("API", "api", "Duplicate definition")))
                .isInstanceOf(Exception.class);
    }

    @Test
    void created_at_and_updated_at_are_set_on_save() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("SLA", "sla", "Service Level Agreement");

        // Act
        BusinessDictionaryEntry saved = repository.saveAndFlush(entry);

        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updated_at_changes_after_definition_update() {
        // Arrange
        BusinessDictionaryEntry entry = repository.saveAndFlush(
                new BusinessDictionaryEntry("KPI", "kpi", "Key Performance Indicator"));
        entityManager.clear();

        // Act
        BusinessDictionaryEntry found = repository.findByNormalizedTerm("kpi").orElseThrow();
        found.updateDefinition("Updated KPI definition");
        BusinessDictionaryEntry updated = repository.saveAndFlush(found);

        // Assert
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getCreatedAt()).isEqualTo(found.getCreatedAt());
    }
}
