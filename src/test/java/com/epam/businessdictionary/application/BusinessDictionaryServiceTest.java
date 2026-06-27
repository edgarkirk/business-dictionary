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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BusinessDictionaryServiceTest {

    @Mock
    private BusinessDictionaryRepository repository;

    @InjectMocks
    private BusinessDictionaryServiceImpl service;

    // --- create ---

    @Test
    void creates_term_successfully() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("Microservice", "A small independently deployable service");
        given(repository.existsByNormalizedTerm("microservice")).willReturn(false);
        given(repository.save(any(BusinessDictionaryEntry.class))).willReturn(entry);

        BusinessDictionaryEntry result = service.create("Microservice", "A small independently deployable service");

        assertThat(result).isNotNull();
        assertThat(result.getTerm()).isEqualTo("Microservice");
        assertThat(result.getDefinition()).isEqualTo("A small independently deployable service");
        verify(repository).save(any(BusinessDictionaryEntry.class));
    }

    @Test
    void throws_duplicate_term_exception_when_term_already_exists() {
        given(repository.existsByNormalizedTerm("kpi")).willReturn(true);

        assertThatThrownBy(() -> service.create("KPI", "Key Performance Indicator"))
                .isInstanceOf(DuplicateTermException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_normalizes_term_for_duplicate_check() {
        given(repository.existsByNormalizedTerm("roi")).willReturn(true);

        assertThatThrownBy(() -> service.create("ROI", "Return on Investment"))
                .isInstanceOf(DuplicateTermException.class);
    }

    // --- findByTerm ---

    @Test
    void returns_entry_when_term_found() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("ROI", "Return on Investment");
        given(repository.findByNormalizedTerm("roi")).willReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("ROI");

        assertThat(result.getTerm()).isEqualTo("ROI");
        assertThat(result.getDefinition()).isEqualTo("Return on Investment");
    }

    @Test
    void finds_term_case_insensitively() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("EBITDA", "Earnings Before Interest, Taxes, Depreciation, Amortization");
        given(repository.findByNormalizedTerm("ebitda")).willReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.findByTerm("ebitda");

        assertThat(result.getTerm()).isEqualTo("EBITDA");
    }

    @Test
    void throws_term_not_found_exception_when_term_does_not_exist_on_find() {
        given(repository.findByNormalizedTerm("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByTerm("nonexistent"))
                .isInstanceOf(TermNotFoundException.class);
    }

    // --- updateDefinition ---

    @Test
    void updates_definition_successfully() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("SLA", "Service Level Agreement");
        given(repository.findByNormalizedTerm("sla")).willReturn(Optional.of(entry));
        given(repository.save(entry)).willReturn(entry);

        BusinessDictionaryEntry result = service.updateDefinition("SLA", "Service Level Agreement (updated)");

        assertThat(result.getDefinition()).isEqualTo("Service Level Agreement (updated)");
        verify(repository).save(entry);
    }

    @Test
    void throws_term_not_found_exception_when_term_does_not_exist_on_update() {
        given(repository.findByNormalizedTerm("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefinition("nonexistent", "some definition"))
                .isInstanceOf(TermNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_normalizes_term_for_lookup() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("API", "Application Programming Interface");
        given(repository.findByNormalizedTerm("api")).willReturn(Optional.of(entry));
        given(repository.save(entry)).willReturn(entry);

        BusinessDictionaryEntry result = service.updateDefinition("API", "Application Program Interface");

        assertThat(result.getDefinition()).isEqualTo("Application Program Interface");
    }
}
