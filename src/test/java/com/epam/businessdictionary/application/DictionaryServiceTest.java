package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.CreateTermRequest;
import com.epam.businessdictionary.api.DictionaryTermResponse;
import com.epam.businessdictionary.api.UpdateDefinitionRequest;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryRepository repository;

    @InjectMocks
    private DictionaryServiceImpl dictionaryService;

    @Test
    void createsTermSuccessfully() {
        CreateTermRequest request = createRequest("Bounded Context", "A DDD concept.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> populateEntry(inv.getArgument(0)));

        DictionaryTermResponse response = dictionaryService.createTerm(request);

        assertThat(response.getTerm()).isEqualTo("Bounded Context");
        assertThat(response.getDefinition()).isEqualTo("A DDD concept.");
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void normalizesTermToLowercaseBeforePersisting() {
        CreateTermRequest request = createRequest("BOUNDED CONTEXT", "A DDD concept.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> populateEntry(inv.getArgument(0)));

        dictionaryService.createTerm(request);

        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNormalizedTerm()).isEqualTo("bounded context");
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        CreateTermRequest request = createRequest("Bounded Context", "A DDD concept.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(new DictionaryEntry()));

        assertThatThrownBy(() -> dictionaryService.createTerm(request))
            .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "A DDD concept.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));

        DictionaryTermResponse response = dictionaryService.getTerm("Bounded Context");

        assertThat(response.getTerm()).isEqualTo("Bounded Context");
        assertThat(response.getDefinition()).isEqualTo("A DDD concept.");
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry entry = buildEntry("Bounded Context", "bounded context", "Old definition.");
        when(repository.findByNormalizedTerm("bounded context")).thenReturn(Optional.of(entry));
        when(repository.save(any())).thenAnswer(inv -> populateEntry(inv.getArgument(0)));

        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("New definition.");

        DictionaryTermResponse response = dictionaryService.updateDefinition("Bounded Context", request);

        assertThat(response.getDefinition()).isEqualTo("New definition.");
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dictionaryService.getTerm("unknown"))
            .isInstanceOf(TermNotFoundException.class);
    }

    // ---- helpers ----

    private CreateTermRequest createRequest(String term, String definition) {
        CreateTermRequest req = new CreateTermRequest();
        req.setTerm(term);
        req.setDefinition(definition);
        return req;
    }

    private DictionaryEntry buildEntry(String term, String normalizedTerm, String definition) {
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(term);
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(definition);
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        return entry;
    }

    private DictionaryEntry populateEntry(DictionaryEntry entry) {
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        return entry;
    }
}
