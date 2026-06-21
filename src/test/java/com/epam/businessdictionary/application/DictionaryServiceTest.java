package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.CreateTermRequest;
import com.epam.businessdictionary.api.DictionaryTermResponse;
import com.epam.businessdictionary.api.UpdateDefinitionRequest;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        CreateTermRequest request = new CreateTermRequest("Bounded Context", "A boundary within a domain model.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> {
            DictionaryEntry entry = inv.getArgument(0);
            entry.setId(UUID.randomUUID());
            return entry;
        });

        DictionaryTermResponse response = service.createTerm(request);

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A boundary within a domain model.");
        assertThat(response.id()).isNotNull();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        verify(repository).save(any(DictionaryEntry.class));
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        CreateTermRequest request = new CreateTermRequest("BOUNDED CONTEXT", "A boundary.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(new DictionaryEntry()));

        assertThrows(DuplicateTermException.class, () -> service.createTerm(request));
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A boundary.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryTermResponse response = service.getByTerm("Bounded Context");

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A boundary.");
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DictionaryTermResponse response = service.updateDefinition("Bounded Context",
                new UpdateDefinitionRequest("Updated definition."));

        assertThat(response.definition()).isEqualTo("Updated definition.");
        assertThat(response.updatedAt()).isAfterOrEqualTo(entry.getCreatedAt());
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThrows(TermNotFoundException.class, () -> service.getByTerm("unknown"));
    }

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setId(UUID.randomUUID());
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        entry.setCreatedAt(OffsetDateTime.now());
        entry.setUpdatedAt(OffsetDateTime.now());
        return entry;
    }
}
