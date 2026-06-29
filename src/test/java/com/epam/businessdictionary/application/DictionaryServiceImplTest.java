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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceImplTest {

    @Mock
    private BusinessDictionaryRepository repository;

    private DictionaryService service;

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(repository);
    }

    @Test
    void create_returns_saved_entry() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "Application Programming Interface");
        when(repository.saveAndFlush(any())).thenReturn(entry);

        BusinessDictionaryEntry result = service.create("API", "Application Programming Interface");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void create_throws_duplicate_exception_on_constraint_violation() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.create("API", "Definition"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("API");
    }

    @Test
    void find_by_term_returns_entry_case_insensitively() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("DevOps", "Dev and Ops collaboration");
        when(repository.findByNormalizedTerm("devops")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("DevOps");

        assertThat(result.getTerm()).isEqualTo("DevOps");
    }

    @Test
    void find_by_term_throws_not_found_for_missing_term() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void update_definition_returns_updated_entry() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("CI", "Continuous Integration");
        when(repository.findByNormalizedTerm("ci")).thenReturn(Optional.of(entry));
        when(repository.saveAndFlush(entry)).thenReturn(entry);

        BusinessDictionaryEntry result = service.updateDefinition("CI", "Automated build and test process");

        assertThat(result.getDefinition()).isEqualTo("Automated build and test process");
    }

    @Test
    void update_definition_throws_not_found_for_missing_term() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefinition("unknown", "New definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
