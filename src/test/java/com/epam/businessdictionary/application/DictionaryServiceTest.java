package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private BusinessDictionaryRepository repository;

    @InjectMocks
    private DictionaryServiceImpl service;

    @Test
    void creates_term_successfully() {
        var entry = new BusinessDictionaryEntry("API", "Application Programming Interface");
        when(repository.existsByNormalizedTerm("api")).thenReturn(false);
        when(repository.save(any(BusinessDictionaryEntry.class))).thenReturn(entry);

        BusinessDictionaryEntry result = service.createTerm("API", "Application Programming Interface");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
        verify(repository).save(any(BusinessDictionaryEntry.class));
    }

    @Test
    void create_term_throws_term_already_exists_exception_when_term_is_duplicate() {
        when(repository.existsByNormalizedTerm("api")).thenReturn(true);

        assertThatThrownBy(() -> service.createTerm("API", "Application Programming Interface"))
                .isInstanceOf(TermAlreadyExistsException.class);
    }

    @Test
    void get_term_returns_entry_for_existing_term() {
        var entry = new BusinessDictionaryEntry("REST", "Representational State Transfer");
        when(repository.findByNormalizedTerm("rest")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.getTerm("REST");

        assertThat(result.getTerm()).isEqualTo("REST");
        assertThat(result.getDefinition()).isEqualTo("Representational State Transfer");
    }

    @Test
    void get_term_throws_term_not_found_exception_for_missing_term() {
        when(repository.findByNormalizedTerm("rest")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("REST"))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void updates_definition_successfully() {
        var entry = new BusinessDictionaryEntry("JPA", "Java Persistence API");
        when(repository.findByNormalizedTerm("jpa")).thenReturn(Optional.of(entry));
        when(repository.save(any(BusinessDictionaryEntry.class))).thenReturn(entry);

        BusinessDictionaryEntry result = service.updateDefinition("JPA", "Jakarta Persistence API");

        assertThat(result.getDefinition()).isEqualTo("Jakarta Persistence API");
        verify(repository).save(entry);
    }

    @Test
    void update_definition_throws_term_not_found_exception_for_missing_term() {
        when(repository.findByNormalizedTerm("jpa")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefinition("JPA", "Jakarta Persistence API"))
                .isInstanceOf(TermNotFoundException.class);
    }
}
