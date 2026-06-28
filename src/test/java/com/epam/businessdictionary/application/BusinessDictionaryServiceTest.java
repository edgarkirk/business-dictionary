package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import com.epam.businessdictionary.persistence.BusinessDictionaryTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessDictionaryServiceTest {

    @Mock
    private BusinessDictionaryTermRepository repository;

    private BusinessDictionaryService service;

    @BeforeEach
    void setUp() {
        service = new BusinessDictionaryServiceImpl(repository);
    }

    @Test
    void creates_term_successfully() {
        var entity = new BusinessDictionaryTerm("Agile", "An iterative approach to software delivery");
        when(repository.findByNormalizedTerm("agile")).thenReturn(Optional.empty());
        when(repository.save(any(BusinessDictionaryTerm.class))).thenReturn(entity);

        var result = service.createTerm("Agile", "An iterative approach to software delivery");

        assertThat(result.getTerm()).isEqualTo("Agile");
        assertThat(result.getDefinition()).isEqualTo("An iterative approach to software delivery");
    }

    @Test
    void create_term_throws_duplicate_term_exception_when_normalized_term_exists() {
        var existing = new BusinessDictionaryTerm("Agile", "An iterative approach to software delivery");
        when(repository.findByNormalizedTerm("agile")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createTerm("AGILE", "Another definition"))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void gets_existing_term_by_name() {
        var existing = new BusinessDictionaryTerm("Kanban", "A visual workflow management method");
        when(repository.findByNormalizedTerm("kanban")).thenReturn(Optional.of(existing));

        var result = service.getTerm("Kanban");

        assertThat(result.getTerm()).isEqualTo("Kanban");
        assertThat(result.getDefinition()).isEqualTo("A visual workflow management method");
    }

    @Test
    void get_term_is_case_insensitive() {
        var existing = new BusinessDictionaryTerm("Kanban", "A visual workflow management method");
        when(repository.findByNormalizedTerm("kanban")).thenReturn(Optional.of(existing));

        var result = service.getTerm("KANBAN");

        assertThat(result.getTerm()).isEqualTo("Kanban");
    }

    @Test
    void get_term_throws_term_not_found_exception_when_term_missing() {
        when(repository.findByNormalizedTerm("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("Nonexistent"))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void updates_existing_term_definition() {
        var existing = new BusinessDictionaryTerm("Sprint", "A time box");
        when(repository.findByNormalizedTerm("sprint")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var result = service.updateTerm("Sprint", "A fixed-length iteration in Scrum");

        assertThat(result.getDefinition()).isEqualTo("A fixed-length iteration in Scrum");
    }

    @Test
    void update_term_is_case_insensitive() {
        var existing = new BusinessDictionaryTerm("Sprint", "A time box");
        when(repository.findByNormalizedTerm("sprint")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        var result = service.updateTerm("SPRINT", "A fixed-length iteration in Scrum");

        assertThat(result.getTerm()).isEqualTo("Sprint");
    }

    @Test
    void update_term_throws_term_not_found_exception_when_term_missing() {
        when(repository.findByNormalizedTerm("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTerm("Ghost", "Some definition"))
                .isInstanceOf(TermNotFoundException.class);
    }
}
