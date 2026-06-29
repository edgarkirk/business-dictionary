package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
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
class DictionaryServiceImplTest {

    @Mock
    private BusinessDictionaryRepository repository;

    @InjectMocks
    private DictionaryServiceImpl service;

    private BusinessDictionaryEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
    }

    @Test
    void create_term_successfully() {
        when(repository.saveAndFlush(any())).thenReturn(sampleEntry);

        BusinessDictionaryEntry result = service.createTerm("API", "Application Programming Interface");

        assertThat(result.getTerm()).isEqualTo("API");
        assertThat(result.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void create_term_throws_duplicate_exception_on_constraint_violation() {
        when(repository.saveAndFlush(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service.createTerm("API", "Application Programming Interface"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("API");
    }

    @Test
    void create_term_normalizes_to_lowercase_for_lookup() {
        when(repository.saveAndFlush(any(BusinessDictionaryEntry.class))).thenAnswer(inv -> {
            BusinessDictionaryEntry e = inv.getArgument(0);
            assertThat(e.getNormalizedTerm()).isEqualTo("api");
            return e;
        });

        service.createTerm("API", "Application Programming Interface");
    }

    @Test
    void get_term_successfully() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));

        BusinessDictionaryEntry result = service.getTerm("API");

        assertThat(result.getTerm()).isEqualTo("API");
    }

    @Test
    void get_term_is_case_insensitive() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));

        BusinessDictionaryEntry result = service.getTerm("Api");

        assertThat(result).isEqualTo(sampleEntry);
    }

    @Test
    void get_term_throws_not_found_exception() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void update_term_successfully() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));
        when(repository.saveAndFlush(sampleEntry)).thenReturn(sampleEntry);

        BusinessDictionaryEntry result = service.updateTerm("API", "Updated definition");

        assertThat(result.getDefinition()).isEqualTo("Updated definition");
    }

    @Test
    void update_term_throws_not_found_exception() {
        when(repository.findByNormalizedTerm("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTerm("missing", "some definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
