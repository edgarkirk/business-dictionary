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
class BusinessDictionaryServiceTest {

    @Mock
    private BusinessDictionaryRepository repository;

    @InjectMocks
    private BusinessDictionaryServiceImpl service;

    @Test
    void create_term_returns_saved_entry() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Term", "A definition");
        when(repository.saveAndFlush(any())).thenReturn(entry);

        BusinessDictionaryEntry result = service.createTerm("Term", "A definition");

        assertThat(result.getTerm()).isEqualTo("Term");
        assertThat(result.getDefinition()).isEqualTo("A definition");
        assertThat(result.getNormalizedTerm()).isEqualTo("term");
    }

    @Test
    void create_term_throws_duplicate_exception_on_integrity_violation() {
        when(repository.saveAndFlush(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service.createTerm("Existing", "A definition"))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("Existing");
    }

    @Test
    void get_term_returns_entry_when_found() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("MyTerm", "A definition");
        when(repository.findByNormalizedTerm("myterm")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.getTerm("MyTerm");

        assertThat(result.getTerm()).isEqualTo("MyTerm");
    }

    @Test
    void get_term_performs_case_insensitive_lookup() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("MyTerm", "A definition");
        when(repository.findByNormalizedTerm("myterm")).thenReturn(Optional.of(entry));

        BusinessDictionaryEntry result = service.getTerm("MYTERM");

        assertThat(result.getTerm()).isEqualTo("MyTerm");
    }

    @Test
    void get_term_throws_not_found_exception_when_missing() {
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("Missing"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("Missing");
    }

    @Test
    void update_term_updates_definition_and_returns_entry() {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Term", "Old definition");
        when(repository.findByNormalizedTerm("term")).thenReturn(Optional.of(entry));
        when(repository.saveAndFlush(entry)).thenReturn(entry);

        BusinessDictionaryEntry result = service.updateTerm("term", "New definition");

        assertThat(result.getDefinition()).isEqualTo("New definition");
    }

    @Test
    void update_term_throws_not_found_exception_when_missing() {
        when(repository.findByNormalizedTerm(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTerm("Missing", "New definition"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("Missing");
    }
}
