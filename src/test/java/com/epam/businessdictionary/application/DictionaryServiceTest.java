package com.epam.businessdictionary.application;

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

import java.util.Optional;

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
    void creates_term_successfully() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        when(repository.saveAndFlush(any())).thenReturn(entry);

        BusinessDictionaryEntry result = service.createTerm("API", "Application Programming Interface");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void rejects_duplicate_term() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.createTerm("API", "Application Programming Interface"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("API");
    }

    @Test
    void gets_existing_term() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.getTerm("API");

        assertThat(result.getTerm()).isEqualTo("API");
    }

    @Test
    void returns_not_found_for_missing_term() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("Unknown"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("Unknown");
    }

    @Test
    void updates_existing_term() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("SLA", "sla", "Service Level Agreement");
        when(repository.findByNormalizedTerm("sla")).thenReturn(Optional.of(entry));
        when(repository.saveAndFlush(entry)).thenReturn(entry);

        BusinessDictionaryEntry result = service.updateTerm("SLA", "Service-Level Agreement");

        assertThat(result.getDefinition()).isEqualTo("Service-Level Agreement");
    }

    @Test
    void throws_not_found_when_updating_missing_term() {
        when(repository.findByNormalizedTerm("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTerm("Missing", "Some definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("Missing");
    }
}
