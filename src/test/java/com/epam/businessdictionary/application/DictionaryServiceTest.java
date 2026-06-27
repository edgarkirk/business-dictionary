package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private BusinessDictionaryEntryRepository repository;

    private DictionaryService service;

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(repository);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void creates_term_successfully() {
        var entry = new BusinessDictionaryEntry("Microservice", "microservice", "A small independent service");
        when(repository.existsByNormalizedTerm("microservice")).thenReturn(false);
        when(repository.save(any())).thenReturn(entry);

        BusinessDictionaryEntry result = service.create("Microservice", "A small independent service");

        assertThat(result.getTerm()).isEqualTo("Microservice");
        assertThat(result.getNormalizedTerm()).isEqualTo("microservice");
        assertThat(result.getDefinition()).isEqualTo("A small independent service");
        verify(repository).save(any(BusinessDictionaryEntry.class));
    }

    @Test
    void create_throws_duplicate_term_exception_when_term_exists() {
        when(repository.existsByNormalizedTerm("java")).thenReturn(true);

        assertThatThrownBy(() -> service.create("java", "A programming language"))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void create_normalizes_term_before_duplicate_check() {
        when(repository.existsByNormalizedTerm("java")).thenReturn(true);

        assertThatThrownBy(() -> service.create("JAVA", "A programming language"))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void create_persists_entry_with_lowercased_normalized_term() {
        when(repository.existsByNormalizedTerm("api gateway")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessDictionaryEntry result = service.create("API Gateway", "A server-side entry point");

        assertThat(result.getNormalizedTerm()).isEqualTo("api gateway");
        assertThat(result.getTerm()).isEqualTo("API Gateway");
    }

    // ── findByTerm ───────────────────────────────────────────────────────────

    @Test
    void find_returns_entry_when_term_exists() {
        var entry = new BusinessDictionaryEntry("Cloud", "cloud", "Cloud computing");
        when(repository.findByNormalizedTerm("cloud")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("cloud");

        assertThat(result.getTerm()).isEqualTo("Cloud");
        assertThat(result.getDefinition()).isEqualTo("Cloud computing");
    }

    @Test
    void find_is_case_insensitive() {
        var entry = new BusinessDictionaryEntry("Cloud", "cloud", "Cloud computing");
        when(repository.findByNormalizedTerm("cloud")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("CLOUD");

        assertThat(result.getTerm()).isEqualTo("Cloud");
    }

    @Test
    void find_throws_term_not_found_exception_when_term_does_not_exist() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_updates_definition_and_returns_entry() {
        var entry = new BusinessDictionaryEntry("Agile", "agile", "Original definition");
        when(repository.findByNormalizedTerm("agile")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessDictionaryEntry result = service.update("Agile", "Updated definition");

        assertThat(result.getDefinition()).isEqualTo("Updated definition");
        verify(repository).save(entry);
    }

    @Test
    void update_is_case_insensitive() {
        var entry = new BusinessDictionaryEntry("Agile", "agile", "Original definition");
        when(repository.findByNormalizedTerm("agile")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessDictionaryEntry result = service.update("AGILE", "Updated definition");

        assertThat(result.getDefinition()).isEqualTo("Updated definition");
    }

    @Test
    void update_throws_term_not_found_exception_when_term_does_not_exist() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("unknown", "Some definition"))
                .isInstanceOf(TermNotFoundException.class);
    }
}
