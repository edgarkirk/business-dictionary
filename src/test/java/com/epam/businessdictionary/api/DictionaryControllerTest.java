package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.DuplicateTermException;
import com.epam.businessdictionary.application.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
class DictionaryControllerTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService service;

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "A boundary.");
        when(service.createTerm("Bounded Context", "A boundary.")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("Bounded Context", "A boundary."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A boundary."));
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(service.createTerm(eq("Bounded Context"), eq("A boundary.")))
                .thenThrow(new DuplicateTermException("Bounded Context"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("Bounded Context", "A boundary."))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        DictionaryEntry entry = entry("Bounded Context", "bounded context", "A boundary.");
        when(service.getTerm("Bounded Context")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/Bounded Context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A boundary."));
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
        DictionaryEntry updated = entry("Bounded Context", "bounded context", "Updated definition.");
        when(service.updateDefinition("Bounded Context", "Updated definition.")).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/Bounded Context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateDefinitionRequest("Updated definition."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition."));
    }

    @Test
    void returns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("", "Some definition."))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private DictionaryEntry entry(String term, String normalized, String definition) {
        Instant now = Instant.now();
        return new DictionaryEntry(term, normalized, definition, now, now);
    }
}
