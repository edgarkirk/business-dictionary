package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    @MockBean
    private DictionaryService dictionaryService;

    // Mocking BusinessDictionaryEntry is necessary in this slice test because its id
    // and timestamp fields are JPA-managed (no public constructor args/setters available).
    private BusinessDictionaryEntry stubEntry(String term, String definition) {
        BusinessDictionaryEntry entry = mock(BusinessDictionaryEntry.class);
        when(entry.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(entry.getTerm()).thenReturn(term);
        when(entry.getDefinition()).thenReturn(definition);
        when(entry.getCreatedAt()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        when(entry.getUpdatedAt()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        return entry;
    }

    @Test
    void creates_term_successfully() throws Exception {
        when(dictionaryService.createTerm("API", "Application Programming Interface"))
                .thenReturn(stubEntry("API", "Application Programming Interface"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void create_term_returns_409_for_duplicate_term() throws Exception {
        when(dictionaryService.createTerm(anyString(), anyString()))
                .thenThrow(new TermAlreadyExistsException("API"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void create_term_returns_400_for_blank_term() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_blank_definition() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_term_exceeding_max_length() throws Exception {
        String longTerm = "A".repeat(101);
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"" + longTerm + "\", \"definition\": \"Some definition\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_term_returns_400_for_definition_exceeding_max_length() throws Exception {
        String longDef = "D".repeat(1001);
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"API\", \"definition\": \"" + longDef + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_term_returns_200_with_term_details() throws Exception {
        when(dictionaryService.getTerm("API"))
                .thenReturn(stubEntry("API", "Application Programming Interface"));

        mockMvc.perform(get(BASE + "/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void get_term_returns_404_for_missing_term() throws Exception {
        when(dictionaryService.getTerm("Unknown"))
                .thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE + "/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_definition_returns_200_with_updated_term() throws Exception {
        when(dictionaryService.updateDefinition("API", "Updated Definition"))
                .thenReturn(stubEntry("API", "Updated Definition"));

        mockMvc.perform(put(BASE + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated Definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Updated Definition"));
    }

    @Test
    void update_definition_returns_400_for_blank_definition() throws Exception {
        mockMvc.perform(put(BASE + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_definition_returns_404_for_missing_term() throws Exception {
        when(dictionaryService.updateDefinition(anyString(), anyString()))
                .thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(put(BASE + "/Unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Some definition"}
                                """))
                .andExpect(status().isNotFound());
    }
}
