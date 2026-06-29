package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private BusinessDictionaryRepository repository;

    @InjectMocks
    private DictionaryServiceImpl service;

    @Test
    void createTerm_saves_and_returns_response() {
        // Arrange
        BusinessDictionaryEntry entry = entryWithAuditFields("REST", "rest", "Representational State Transfer");
        when(repository.saveAndFlush(any())).thenReturn(entry);

        // Act
        TermResponse response = service.createTerm(new CreateTermRequest("REST", "Representational State Transfer"));

        // Assert
        assertThat(response.term()).isEqualTo("REST");
        assertThat(response.definition()).isEqualTo("Representational State Transfer");
        assertThat(response.id()).isNotNull();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void createTerm_throws_duplicate_exception_on_constraint_violation() {
        // Arrange
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique constraint"));

        // Act & Assert
        assertThatThrownBy(() -> service.createTerm(new CreateTermRequest("REST", "Some definition")))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("REST");
    }

    @Test
    void getTerm_returns_response() {
        // Arrange
        BusinessDictionaryEntry entry = entryWithAuditFields("API", "api", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));

        // Act
        TermResponse response = service.getTerm("API");

        // Assert
        assertThat(response.term()).isEqualTo("API");
        assertThat(response.definition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void getTerm_is_case_insensitive() {
        // Arrange
        BusinessDictionaryEntry entry = entryWithAuditFields("API", "api", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));

        // Act
        TermResponse response = service.getTerm("API");

        // Assert
        assertThat(response.term()).isEqualTo("API");
    }

    @Test
    void getTerm_throws_not_found_exception() {
        // Arrange
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void updateTerm_updates_and_returns_response() {
        // Arrange
        BusinessDictionaryEntry entry = entryWithAuditFields("API", "api", "Old definition");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));
        when(repository.saveAndFlush(entry)).thenReturn(entry);

        // Act
        TermResponse response = service.updateTerm("API", new UpdateTermRequest("New definition"));

        // Assert
        assertThat(response.definition()).isEqualTo("New definition");
    }

    @Test
    void updateTerm_throws_not_found_exception() {
        // Arrange
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.updateTerm("unknown", new UpdateTermRequest("Some definition")))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    private BusinessDictionaryEntry entryWithAuditFields(String term, String normalizedTerm, String definition) {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry(term, normalizedTerm, definition);
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        return entry;
    }
}
