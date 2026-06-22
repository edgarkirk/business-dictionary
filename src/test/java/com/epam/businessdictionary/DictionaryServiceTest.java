package com.epam.businessdictionary;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.DictionaryServiceImpl;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
    private DictionaryServiceImpl service;

    private DictionaryEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new DictionaryEntry();
        sampleEntry.setId(UUID.randomUUID());
        sampleEntry.setTerm("api");
        sampleEntry.setNormalizedTerm("api");
        sampleEntry.setDefinition("Application Programming Interface");
        sampleEntry.setCreatedAt(OffsetDateTime.now());
        sampleEntry.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void createsTermSuccessfully() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(sampleEntry);

        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("api");
        request.setDefinition("Application Programming Interface");

        DictionaryTermResponse response = service.createTerm(request);

        assertThat(response.getTerm()).isEqualTo("api");
        assertThat(response.getDefinition()).isEqualTo("Application Programming Interface");
        assertThat(response.getId()).isNotNull();
    }

    @Test
    void normalizesTermToLowercaseBeforePersisting() {
        when(repository.findByNormalizedTerm("rest api")).thenReturn(Optional.empty());
        when(repository.save(any(DictionaryEntry.class))).thenReturn(sampleEntry);

        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("REST API");
        request.setDefinition("Representational State Transfer API");

        service.createTerm(request);

        ArgumentCaptor<DictionaryEntry> captor = ArgumentCaptor.forClass(DictionaryEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNormalizedTerm()).isEqualTo("rest api");
        assertThat(captor.getValue().getTerm()).isEqualTo("REST API");
    }

    @Test
    void rejectsDuplicateTermCaseInsensitively() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));

        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("API");
        request.setDefinition("Some definition");

        assertThatThrownBy(() -> service.createTerm(request))
                .isInstanceOf(DuplicateTermException.class);
    }

    @Test
    void readsExistingTerm() {
        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));

        DictionaryTermResponse response = service.getTerm("api");

        assertThat(response.getTerm()).isEqualTo("api");
        assertThat(response.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void updatesDefinition() {
        DictionaryEntry updatedEntry = new DictionaryEntry();
        updatedEntry.setId(sampleEntry.getId());
        updatedEntry.setTerm(sampleEntry.getTerm());
        updatedEntry.setNormalizedTerm(sampleEntry.getNormalizedTerm());
        updatedEntry.setDefinition("Updated definition");
        updatedEntry.setCreatedAt(sampleEntry.getCreatedAt());
        updatedEntry.setUpdatedAt(OffsetDateTime.now());

        when(repository.findByNormalizedTerm("api")).thenReturn(Optional.of(sampleEntry));
        when(repository.save(sampleEntry)).thenReturn(updatedEntry);

        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("Updated definition");

        DictionaryTermResponse response = service.updateDefinition("api", request);

        assertThat(response.getDefinition()).isEqualTo("Updated definition");
    }

    @Test
    void returnsNotFoundForMissingTerm() {
        when(repository.findByNormalizedTerm("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTerm("unknown"))
                .isInstanceOf(TermNotFoundException.class);
    }
}
