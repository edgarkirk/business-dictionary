package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private BusinessDictionaryRepository repository;

    private DictionaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(repository);
    }

    // --- create ---

    @Test
    void create_saves_and_returns_entry() {
        String term = "API";
        String definition = "Application Programming Interface";
        BusinessDictionaryEntry saved = new BusinessDictionaryEntry(term, definition);

        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.empty());
        when(repository.save(any(BusinessDictionaryEntry.class))).thenReturn(saved);

        BusinessDictionaryEntry result = service.create(term, definition);

        assertThat(result.getTerm()).isEqualTo(term);
        assertThat(result.getDefinition()).isEqualTo(definition);
        verify(repository).save(any(BusinessDictionaryEntry.class));
    }

    @Test
    void create_throws_when_term_already_exists() {
        BusinessDictionaryEntry existing = new BusinessDictionaryEntry("API", "old definition");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create("API", "new definition"))
                .isInstanceOf(TermAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_normalizes_term_to_lowercase_for_duplicate_check() {
        BusinessDictionaryEntry existing = new BusinessDictionaryEntry("api", "old definition");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create("API", "new definition"))
                .isInstanceOf(TermAlreadyExistsException.class);
    }

    // --- findByTerm ---

    @Test
    void find_by_term_returns_entry() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("API");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void find_by_term_throws_when_not_found() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTerm("Unknown"))
                .isInstanceOf(TermNotFoundException.class);
    }

    // --- updateDefinition ---

    @Test
    void update_definition_updates_and_returns_entry() {
        String term = "API";
        String newDefinition = "Updated definition";
        BusinessDictionaryEntry existing = new BusinessDictionaryEntry(term, "old definition");

        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        BusinessDictionaryEntry result = service.updateDefinition(term, newDefinition);

        assertThat(result.getDefinition()).isEqualTo(newDefinition);
        verify(repository).save(existing);
    }

    @Test
    void update_definition_throws_when_term_not_found() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefinition("Unknown", "definition"))
                .isInstanceOf(TermNotFoundException.class);

        verify(repository, never()).save(any());
    }
}
