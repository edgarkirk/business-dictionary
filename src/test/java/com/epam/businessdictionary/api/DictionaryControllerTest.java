package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.BusinessDictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
class DictionaryControllerTest {

    private static final String BASE = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessDictionaryService service;

    // --- POST /terms ---

    @Test
    void create_term_returns_201() throws Exception {
        var term = new BusinessDictionaryTerm("API", "Application Programming Interface");
        when(service.createTerm("API", "Application Programming Interface")).thenReturn(term);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void create_term_returns_409_for_duplicate() throws Exception {
        when(service.createTerm("API", "Duplicate")).thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Duplicate"))))
                .andExpect(status().isConflict());
    }

    @Test
    void create_term_returns_400_for_blank_term() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "", "definition", "Valid definition."))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_blank_definition() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "SomeTerm", "definition", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_term_exceeding_max_length() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "T".repeat(101), "definition", "Valid definition."))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_definition_exceeding_max_length() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "ValidTerm", "definition", "D".repeat(1001)))))
                .andExpect(status().isBadRequest());
    }

    // --- GET /terms/{term} ---

    @Test
    void get_term_returns_200() throws Exception {
        var term = new BusinessDictionaryTerm("Scrum", "An agile framework.");
        when(service.getTerm("Scrum")).thenReturn(term);

        mockMvc.perform(get(BASE + "/Scrum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Scrum"))
                .andExpect(jsonPath("$.definition").value("An agile framework."));
    }

    @Test
    void get_term_returns_404_for_missing() throws Exception {
        when(service.getTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE + "/Unknown"))
                .andExpect(status().isNotFound());
    }

    // --- PUT /terms/{term} ---

    @Test
    void update_term_returns_200() throws Exception {
        var updated = new BusinessDictionaryTerm("Sprint", "A fixed-length iteration.");
        when(service.updateTerm("Sprint", "A fixed-length iteration.")).thenReturn(updated);

        mockMvc.perform(put(BASE + "/Sprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "A fixed-length iteration."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("A fixed-length iteration."));
    }

    @Test
    void update_term_returns_404_for_missing() throws Exception {
        when(service.updateTerm("Ghost", "New def")).thenThrow(new TermNotFoundException("Ghost"));

        mockMvc.perform(put(BASE + "/Ghost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "New def"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_term_returns_400_for_blank_definition() throws Exception {
        mockMvc.perform(put(BASE + "/Sprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_term_returns_400_for_definition_exceeding_max_length() throws Exception {
        mockMvc.perform(put(BASE + "/Sprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "D".repeat(1001)))))
                .andExpect(status().isBadRequest());
    }
}
