package com.epam.businessdictionary;

import com.epam.businessdictionary.api.DictionaryController;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.DuplicateTermException;
import com.epam.businessdictionary.application.TermNotFoundException;
import com.epam.businessdictionary.config.RestExceptionHandler;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DictionaryController.class, RestExceptionHandler.class})
class DictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService service;

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        DictionaryEntry entry = sampleEntry("Bounded Context", "A domain boundary.");
        when(service.createTerm("Bounded Context", "A domain boundary.")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "Bounded Context", "definition": "A domain boundary."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A domain boundary."))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(service.createTerm(anyString(), anyString()))
                .thenThrow(new DuplicateTermException("Bounded Context"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "Bounded Context", "definition": "A domain boundary."}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        DictionaryEntry entry = sampleEntry("Bounded Context", "A domain boundary.");
        when(service.getTerm("Bounded Context")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/Bounded Context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A domain boundary."));
    }

    @Test
    void returns404ForMissingTermLookup() throws Exception {
        when(service.getTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdateRequest() throws Exception {
        DictionaryEntry entry = sampleEntry("Bounded Context", "Updated definition.");
        when(service.updateDefinition("Bounded Context", "Updated definition.")).thenReturn(entry);

        mockMvc.perform(put(BASE_URL + "/Bounded Context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated definition."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition."));
    }

    @Test
    void returns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private DictionaryEntry sampleEntry(String term, String definition) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DictionaryEntry(UUID.randomUUID(), term, term.toLowerCase(), definition, now, now);
    }
}
