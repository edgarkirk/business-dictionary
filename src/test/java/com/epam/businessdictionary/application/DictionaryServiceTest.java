package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryRepository repository;

    @InjectMocks
    private DictionaryService service;

    @Test
    void createsTermSuccessfully() {
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(false);
        DictionaryEntry saved = entry("Bounded Context", "bounded context", "A boundary within a domain model.");
        when(repository.save(any(DictionaryEntry.class))).thenReturn(saved);

        DictionaryEntry result = service.createTerm("Bounded Context", "A boundary within a domain model.");

        assertThat(result.getTerm()).isEqualTo("Bounded Context");
        assertThat(result.getDefinition()).isEqualTo("A boundary within a domain model.");
        verify(repository).save(argThat(e -> "bounded context".equals(e.getNormalizedTerm())));
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(true);

        assertThatThrownBy(() -> service.createTerm("BOUNDED CONTEXT", "Any definition."))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "A boundary.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryEntry result = service.getTerm("Bounded Context");

        assertThat(result.getTerm()).isEqualTo("Bounded Context");
        assertThat(result.getDefinition()).isEqualTo("A boundary.");
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any(DictionaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        DictionaryEntry result = service.updateDefinition("bounded context", "New definition.");

        assertThat(result.getDefinition()).isEqualTo("New definition.");
        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDefinition()).isEqualTo("New definition.");
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown term")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown term"))
                .isInstanceOf(TermNotFoundException.class);
    }

    private DictionaryEntry entry(String term, String normalized, String definition) {
        Instant now = Instant.now();
        return new DictionaryEntry(term, normalized, definition, now, now);
    }
}
