package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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

    private DictionaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(repository);
    }

    @Test
    void createsTerm_successfully() {
        CreateTermRequest request = createTermRequest("Bounded Context", "A boundary within a domain.");
        DictionaryEntry saved = stubSave("Bounded Context", "bounded context", "A boundary within a domain.");

        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(saved);

        DictionaryTermResponse response = service.createTerm(request);

        assertThat(response.getTerm()).isEqualTo("Bounded Context");
        assertThat(response.getDefinition()).isEqualTo("A boundary within a domain.");
    }

    @Test
    void normalizesTermToLowercase_beforePersisting() {
        CreateTermRequest request = createTermRequest("Bounded Context", "A boundary within a domain.");
        DictionaryEntry saved = stubSave("Bounded Context", "bounded context", "A boundary within a domain.");

        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(saved);

        service.createTerm(request);

        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNormalizedTerm()).isEqualTo("bounded context");
    }

    @Test
    void rejectsDuplicateTerm_caseInsensitively() {
        CreateTermRequest request = createTermRequest("BOUNDED CONTEXT", "Some definition.");
        DictionaryEntry existing = stubSave("bounded context", "bounded context", "Existing definition.");

        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createTerm(request))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = stubSave("Bounded Context", "bounded context", "A boundary.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryTermResponse response = service.getTerm("Bounded Context");

        assertThat(response.getTerm()).isEqualTo("Bounded Context");
        assertThat(response.getDefinition()).isEqualTo("A boundary.");
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = stubSave("Bounded Context", "bounded context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any(DictionaryEntry.class))).thenReturn(entry);

        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("New definition.");

        DictionaryTermResponse response = service.updateDefinition("Bounded Context", request);

        assertThat(response.getDefinition()).isEqualTo("New definition.");
    }

    @Test
    void returnsNotFound_forMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class);
    }

    @Test
    void throwsTermNotFoundException_whenUpdatingNonExistentTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("New definition.");

        assertThatThrownBy(() -> service.updateDefinition("Unknown", request))
                .isInstanceOf(TermNotFoundException.class);
    }

    private CreateTermRequest createTermRequest(String term, String definition) {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm(term);
        request.setDefinition(definition);
        return request;
    }

    private DictionaryEntry stubSave(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry(term, normalizedTerm, definition);
        setField(entry, "id", UUID.randomUUID());
        setField(entry, "createdAt", Instant.now());
        setField(entry, "updatedAt", Instant.now());
        return entry;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
