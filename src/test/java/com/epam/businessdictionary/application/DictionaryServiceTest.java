package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryRepository repository;

    @InjectMocks
    private DictionaryServiceImpl service;

    @Test
    void createsTermSuccessfully() {
        var request = new CreateTermRequest("API", "Application Programming Interface");
        var entry = new DictionaryEntry("API", "api", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(entry);

        var response = service.createTerm(request);

        assertThat(response.term()).isEqualTo("API");
        assertThat(response.definition()).isEqualTo("Application Programming Interface");
        verify(repository).save(any(DictionaryEntry.class));
    }

    @Test
    void normalizesTermToLowercaseBeforePersisting() {
        var request = new CreateTermRequest("Bounded Context", "A boundary within a domain model.");
        var entry = new DictionaryEntry("Bounded Context", "bounded context", "A boundary within a domain model.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(entry);

        service.createTerm(request);

        verify(repository).findByNormalizedTerm("bounded context");
        verify(repository).save(argThat(e -> "bounded context".equals(e.getNormalizedTerm())));
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        var request = new CreateTermRequest("API", "Application Programming Interface");
        var existing = new DictionaryEntry("api", "api", "Old definition");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createTerm(request))
                .isInstanceOf(DuplicateTermException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void readsExistingTerm() {
        var entry = new DictionaryEntry("API", "api", "Application Programming Interface");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));

        var response = service.readTerm("API");

        assertThat(response.term()).isEqualTo("API");
        assertThat(response.definition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void updatesDefinition() {
        var entry = new DictionaryEntry("API", "api", "Old definition");
        var request = new UpdateDefinitionRequest("New definition");
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(entry));
        when(repository.save(entry)).thenReturn(entry);

        var response = service.updateDefinition("API", request);

        assertThat(response.definition()).isEqualTo("New definition");
        verify(repository).save(entry);
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.readTerm("UNKNOWN"))
                .isInstanceOf(TermNotFoundException.class);
    }
}
