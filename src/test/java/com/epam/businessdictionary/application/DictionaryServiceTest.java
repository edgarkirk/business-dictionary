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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DictionaryTermResponse response = service.createTerm(request);

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A boundary within which a domain model is consistent.");
        assertThat(response.id()).isNotNull();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        CreateTermRequest request = new CreateTermRequest("BOUNDED CONTEXT", "A definition.");
        when(repository.existsByNormalizedTerm("bounded context")).thenReturn(true);

        assertThatThrownBy(() -> service.createTerm(request))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryTermResponse response = service.getTerm("Bounded Context");

        assertThat(response.term()).isEqualTo("Bounded Context");
        assertThat(response.definition()).isEqualTo("A definition.");
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DictionaryTermResponse response = service.updateDefinition("Bounded Context", new UpdateDefinitionRequest("New definition."));

        assertThat(response.definition()).isEqualTo("New definition.");
        assertThat(response.updatedAt()).isNotNull();
    }

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setId(UUID.randomUUID());
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        return entry;
    }
}
