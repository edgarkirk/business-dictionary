package com.epam.businessdictionary;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.DuplicateTermException;
import com.epam.businessdictionary.application.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryRepository repository;

    private DictionaryService service;

    @BeforeEach
    void setUp() {
        service = new DictionaryService(repository);
    }

    @Test
    void createsTermSuccessfully() {
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(false);
        when(repository.save(any(DictionaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        DictionaryEntry result = service.createTerm("Bounded Context", "A domain boundary.");

        assertThat(result.getTerm()).isEqualTo("Bounded Context");
        assertThat(result.getNormalizedTerm()).isEqualTo("bounded context");
        assertThat(result.getDefinition()).isEqualTo("A domain boundary.");
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNormalizedTerm()).isEqualTo("bounded context");
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(true);

        assertThatThrownBy(() -> service.createTerm("BOUNDED CONTEXT", "A domain boundary."))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = sampleEntry("Bounded Context", "A domain boundary.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryEntry result = service.getTerm("Bounded Context");

        assertThat(result.getTerm()).isEqualTo("Bounded Context");
        assertThat(result.getDefinition()).isEqualTo("A domain boundary.");
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown term")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("Unknown Term"))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = sampleEntry("Bounded Context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any(DictionaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        DictionaryEntry result = service.updateDefinition("Bounded Context", "New definition.");

        assertThat(result.getDefinition()).isEqualTo("New definition.");
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(entry.getCreatedAt());
        verify(repository).save(entry);
    }

    private DictionaryEntry sampleEntry(String term, String definition) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DictionaryEntry(UUID.randomUUID(), term, term.toLowerCase(), definition, now, now);
    }
}
