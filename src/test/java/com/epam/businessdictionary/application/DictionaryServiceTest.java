package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private BusinessDictionaryRepository repository;

    private DictionaryService service;

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(repository);
    }

    @Test
    void creates_term_successfully() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        given(repository.saveAndFlush(any(BusinessDictionaryEntry.class))).willReturn(entry);

        // Act
        BusinessDictionaryEntry result = service.createTerm("Agile", "Iterative development");

        // Assert
        assertThat(result.getTerm()).isEqualTo("Agile");
        assertThat(result.getDefinition()).isEqualTo("Iterative development");
    }

    @Test
    void throws_duplicate_exception_when_term_already_exists() {
        // Arrange
        given(repository.saveAndFlush(any(BusinessDictionaryEntry.class)))
                .willThrow(new DataIntegrityViolationException("unique constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> service.createTerm("Agile", "Iterative development"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("Agile");
    }

    @Test
    void gets_term_successfully() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        given(repository.findByNormalizedTerm("agile")).willReturn(Optional.of(entry));

        // Act
        BusinessDictionaryEntry result = service.getTerm("Agile");

        // Assert
        assertThat(result.getTerm()).isEqualTo("Agile");
    }

    @Test
    void get_term_is_case_insensitive() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        given(repository.findByNormalizedTerm("agile")).willReturn(Optional.of(entry));

        // Act
        BusinessDictionaryEntry result = service.getTerm("AGILE");

        // Assert
        assertThat(result.getTerm()).isEqualTo("Agile");
    }

    @Test
    void throws_not_found_when_term_missing() {
        // Arrange
        given(repository.findByNormalizedTerm("nonexistent")).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getTerm("nonexistent"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void updates_term_definition_successfully() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Old definition");
        given(repository.findByNormalizedTerm("agile")).willReturn(Optional.of(entry));
        given(repository.saveAndFlush(entry)).willReturn(entry);

        // Act
        BusinessDictionaryEntry result = service.updateTerm("Agile", "New definition");

        // Assert
        assertThat(result.getDefinition()).isEqualTo("New definition");
    }

    @Test
    void throws_not_found_when_updating_nonexistent_term() {
        // Arrange
        given(repository.findByNormalizedTerm("missing")).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateTerm("missing", "some definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void normalizes_term_to_lowercase_on_create() {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("SCRUM", "scrum", "Agile framework");
        given(repository.saveAndFlush(any(BusinessDictionaryEntry.class))).willReturn(entry);

        // Act
        service.createTerm("SCRUM", "Agile framework");

        // Assert
        assertThat(entry.getNormalizedTerm()).isEqualTo("scrum");
    }
}
