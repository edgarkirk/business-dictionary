package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.response.TermResponse;
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
import java.util.Map;
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
    void creates_term_successfully() throws Exception {
        // Arrange
        TermResponse response = new TermResponse(UUID.randomUUID(), "API",
                "Application Programming Interface", Instant.now(), Instant.now());
        when(dictionaryService.createTerm(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void rejects_duplicate_term_with_conflict() throws Exception {
        // Arrange
        when(dictionaryService.createTerm(any())).thenThrow(new DuplicateTermException("API"));

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Some definition"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejects_blank_term_with_bad_request() throws Exception {
        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "", "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejects_term_exceeding_max_length_with_bad_request() throws Exception {
        // Arrange
        String longTerm = "A".repeat(101);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", longTerm, "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejects_blank_definition_with_bad_request() throws Exception {
        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void gets_existing_term_successfully() throws Exception {
        // Arrange
        TermResponse response = new TermResponse(UUID.randomUUID(), "API",
                "Application Programming Interface", Instant.now(), Instant.now());
        when(dictionaryService.getTerm("API")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns_not_found_for_missing_term() throws Exception {
        // Arrange
        when(dictionaryService.getTerm("unknown")).thenThrow(new TermNotFoundException("unknown"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updates_existing_term_successfully() throws Exception {
        // Arrange
        TermResponse response = new TermResponse(UUID.randomUUID(), "API",
                "Updated definition", Instant.now(), Instant.now());
        when(dictionaryService.updateTerm(eq("API"), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "Updated definition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void returns_not_found_on_update_for_missing_term() throws Exception {
        // Arrange
        when(dictionaryService.updateTerm(eq("unknown"), any()))
                .thenThrow(new TermNotFoundException("unknown"));

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "Some definition"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejects_blank_definition_on_update_with_bad_request() throws Exception {
        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
