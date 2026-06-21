package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.CreateTermRequest;
import com.epam.businessdictionary.api.DictionaryTermResponse;
import com.epam.businessdictionary.api.UpdateDefinitionRequest;
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
        CreateTermRequest request = new CreateTermRequest("Bounded Context", "A boundary within which a domain model is consistent.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());

        DictionaryEntry saved = entry(UUID.randomUUID(), "Bounded Context", "bounded context",
                "A boundary within which a domain model is consistent.", Instant.now(), Instant.now());
        when(repository.save(any())).thenReturn(saved);

        DictionaryTermResponse response = service.createTerm(request);

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A boundary within which a domain model is consistent.");
        assertThat(response.id()).isNotNull();

        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNormalizedTerm()).isEqualTo("bounded context");
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        CreateTermRequest request = new CreateTermRequest("BOUNDED CONTEXT", "Some definition.");
        DictionaryEntry existing = entry(UUID.randomUUID(), "Bounded Context", "bounded context", "Old def.", Instant.now(), Instant.now());
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createTerm(request))
                .isInstanceOf(DuplicateTermException.class)
                .hasMessageContaining("BOUNDED CONTEXT");
    }

    @Test
    void readsExistingTerm() {
        UUID id = UUID.randomUUID();
        DictionaryEntry existing = entry(id, "Bounded Context", "bounded context",
                "A boundary definition.", Instant.now(), Instant.now());
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(existing));

        DictionaryTermResponse response = service.getTerm("Bounded Context");

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A boundary definition.");
        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    void updatesDefinition() {
        UUID id = UUID.randomUUID();
        Instant created = Instant.now().minusSeconds(60);
        DictionaryEntry existing = entry(id, "Bounded Context", "bounded context", "Old definition.", created, created);
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(existing));
        when(repository.save(argThat(e -> "New definition.".equals(e.getDefinition())))).thenReturn(existing);

        UpdateDefinitionRequest request = new UpdateDefinitionRequest("New definition.");
        service.updateDefinition("Bounded Context", request);

        verify(repository).save(argThat(e ->
                "New definition.".equals(e.getDefinition()) &&
                e.getUpdatedAt().isAfter(created)
        ));
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    private DictionaryEntry entry(UUID id, String term, String normalizedTerm, String definition,
                                  Instant createdAt, Instant updatedAt) {
        return new DictionaryEntry(id, term, normalizedTerm, definition, createdAt, updatedAt);
    }
}
