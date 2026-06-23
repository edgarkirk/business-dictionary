package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
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

    @MockBean
    private DictionaryService dictionaryService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DictionaryTermResponse SAMPLE_RESPONSE = new DictionaryTermResponse(
            UUID.fromString("2e41580a-4369-4f7d-9a20-d4d8f0b87a68"),
            "API",
            "Application Programming Interface",
            Instant.parse("2026-06-20T12:00:00Z"),
            Instant.parse("2026-06-20T12:00:00Z")
    );

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        when(dictionaryService.createTerm(any())).thenReturn(SAMPLE_RESPONSE);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("API", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(dictionaryService.createTerm(any())).thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("API", "Application Programming Interface"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        when(dictionaryService.readTerm("API")).thenReturn(SAMPLE_RESPONSE);

        mockMvc.perform(get("/api/v1/dictionary/terms/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns404ForMissingTermLookup() throws Exception {
        when(dictionaryService.readTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get("/api/v1/dictionary/terms/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdateRequest() throws Exception {
        when(dictionaryService.updateDefinition(eq("API"), any())).thenReturn(SAMPLE_RESPONSE);

        mockMvc.perform(put("/api/v1/dictionary/terms/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateDefinitionRequest("Updated definition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"));
    }

    @Test
    void returns400ForBlankTerm() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"\", \"definition\": \"Some definition\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returns400ForBlankDefinition() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"API\", \"definition\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
