package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessTerm;
import com.epam.businessdictionary.persistence.BusinessTermRepository;
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
    private BusinessTermRepository repository;

    @InjectMocks
    private DictionaryServiceImpl service;

    @Test
    void creates_term_successfully() {
        BusinessTerm saved = new BusinessTerm("API", "Application Programming Interface");
        when(repository.saveAndFlush(any(BusinessTerm.class))).thenReturn(saved);

        BusinessTerm result = service.create("API", "Application Programming Interface");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
        assertThat(result.getNormalizedTerm()).isEqualTo("api");
    }

    @Test
    void throws_duplicate_exception_on_data_integrity_violation() {
        when(repository.saveAndFlush(any(BusinessTerm.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint violation"));

        assertThatThrownBy(() -> service.create("API", "Some definition"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("API");
    }

    @Test
    void gets_existing_term() {
        BusinessTerm existing = new BusinessTerm("SLA", "Service Level Agreement");
        when(repository.findByNormalizedTerm("sla")).thenReturn(Optional.of(existing));

        BusinessTerm result = service.getByTerm("SLA");

        assertThat(result.getTerm()).isEqualTo("SLA");
        assertThat(result.getDefinition()).isEqualTo("Service Level Agreement");
    }

    @Test
    void get_is_case_insensitive() {
        BusinessTerm existing = new BusinessTerm("SLA", "Service Level Agreement");
        when(repository.findByNormalizedTerm("sla")).thenReturn(Optional.of(existing));

        BusinessTerm result = service.getByTerm("SLA");

        assertThat(result).isNotNull();
    }

    @Test
    void throws_not_found_for_missing_term() {
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByTerm("UNKNOWN"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void updates_term_definition() {
        BusinessTerm existing = new BusinessTerm("KPI", "Key Performance Indicator");
        when(repository.findByNormalizedTerm("kpi")).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any(BusinessTerm.class))).thenAnswer(inv -> inv.getArgument(0));

        BusinessTerm result = service.updateDefinition("KPI", "Key Performance Index");

        assertThat(result.getDefinition()).isEqualTo("Key Performance Index");
    }

    @Test
    void throws_not_found_on_update_for_missing_term() {
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDefinition("UNKNOWN", "New definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }
}
