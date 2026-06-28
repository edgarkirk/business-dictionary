package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.BusinessDictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermController.class)
class TermControllerTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessDictionaryService service;

    private BusinessDictionaryEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new BusinessDictionaryEntry("API", "Application Programming Interface");
    }

    @Test
    void create_term_returns_201_with_body() throws Exception {
        when(service.createTerm("API", "Application Programming Interface")).thenReturn(sampleEntry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void create_term_with_blank_term_returns_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "", "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_term_with_oversized_term_returns_400() throws Exception {
        String oversized = "a".repeat(101);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", oversized, "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_duplicate_term_returns_409() throws Exception {
        when(service.createTerm(anyString(), anyString())).thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Another definition"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void get_term_returns_200_with_body() throws Exception {
        when(service.getTerm("API")).thenReturn(sampleEntry);

        mockMvc.perform(get(BASE_URL + "/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void get_missing_term_returns_404() throws Exception {
        when(service.getTerm(anyString())).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void update_term_returns_200_with_updated_definition() throws Exception {
        BusinessDictionaryEntry updated = new BusinessDictionaryEntry("API", "Updated definition");
        when(service.updateTerm("API", "Updated definition")).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("definition", "Updated definition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void update_missing_term_returns_404() throws Exception {
        when(service.updateTerm(anyString(), anyString())).thenThrow(new TermNotFoundException("Missing"));

        mockMvc.perform(put(BASE_URL + "/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("definition", "Some definition"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void update_term_with_blank_definition_returns_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/Term")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
