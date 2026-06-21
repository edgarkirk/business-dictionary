package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.DuplicateTermException;
import com.epam.businessdictionary.application.TermNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
class DictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService dictionaryService;

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        DictionaryTermResponse response = new DictionaryTermResponse(
                UUID.randomUUID(), "Bounded Context", "A boundary definition.", Instant.now(), Instant.now());
        when(dictionaryService.createTerm(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("Bounded Context", "A boundary definition."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A boundary definition."));
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(dictionaryService.createTerm(any())).thenThrow(new DuplicateTermException("Bounded Context"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("Bounded Context", "A boundary definition."))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        DictionaryTermResponse response = new DictionaryTermResponse(
                UUID.randomUUID(), "Bounded Context", "A boundary definition.", Instant.now(), Instant.now());
        when(dictionaryService.getTerm("Bounded Context")).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{term}", "Bounded Context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Bounded Context"));
    }

    @Test
    void returns404ForMissingTermLookup() throws Exception {
        when(dictionaryService.getTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/{term}", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdateRequest() throws Exception {
        DictionaryTermResponse response = new DictionaryTermResponse(
                UUID.randomUUID(), "Bounded Context", "Updated definition.", Instant.now(), Instant.now());
        when(dictionaryService.updateDefinition(eq("Bounded Context"), any())).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{term}", "Bounded Context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateDefinitionRequest("Updated definition."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition."));
    }

    @Test
    void returns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
