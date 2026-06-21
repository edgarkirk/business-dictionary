package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.DuplicateTermException;
import com.epam.businessdictionary.application.TermNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
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

    @MockBean
    private DictionaryService service;

    @Autowired
    private ObjectMapper objectMapper;

    private DictionaryTermResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new DictionaryTermResponse(
                UUID.randomUUID(),
                "BoundedContext",
                "A boundary within which a domain model is consistent.",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        when(service.createTerm(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("BoundedContext", "A boundary within a domain."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("BoundedContext"));
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(service.createTerm(any())).thenThrow(new DuplicateTermException("BoundedContext"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("BoundedContext", "A boundary within a domain."))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        when(service.getByTerm("BoundedContext")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/dictionary/terms/BoundedContext"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("BoundedContext"))
                .andExpect(jsonPath("$.definition").value("A boundary within which a domain model is consistent."));
    }

    @Test
    void returns404ForMissingTermLookup() throws Exception {
        when(service.getByTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get("/api/v1/dictionary/terms/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdateRequest() throws Exception {
        when(service.updateDefinition(eq("BoundedContext"), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/dictionary/terms/BoundedContext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateDefinitionRequest("New definition."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void returns400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateTermRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
