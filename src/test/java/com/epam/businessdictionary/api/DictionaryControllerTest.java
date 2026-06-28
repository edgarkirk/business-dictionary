package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
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

import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
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

    @MockBean
    private DictionaryService dictionaryService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- POST /terms ---

    @Test
    void creates_term_successfully_returns_201() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "Application Programming Interface");
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        when(dictionaryService.create("API", "Application Programming Interface")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void rejects_duplicate_term_with_409() throws Exception {
        when(dictionaryService.create(anyString(), anyString()))
                .thenThrow(new TermAlreadyExistsException("API"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejects_empty_term_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejects_blank_term_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "   ", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejects_empty_definition_on_create_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejects_blank_definition_on_create_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejects_term_exceeding_max_length_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "a".repeat(101), "definition", "Some definition"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejects_definition_exceeding_max_length_on_create_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "a".repeat(1001)))))
                .andExpect(status().isBadRequest());
    }

    // --- GET /terms/{term} ---

    @Test
    void reads_existing_term_returns_200() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "Application Programming Interface");
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        when(dictionaryService.findByTerm("API")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void returns_not_found_for_missing_term() throws Exception {
        when(dictionaryService.findByTerm("Unknown"))
                .thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- PUT /terms/{term} ---

    @Test
    void updates_existing_term_returns_200() throws Exception {
        BusinessDictionaryEntry updated = new BusinessDictionaryEntry("DevOps", "Updated definition");
        when(dictionaryService.updateDefinition("DevOps", "Updated definition")).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/DevOps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("DevOps"))
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void updates_nonexistent_term_returns_404() throws Exception {
        when(dictionaryService.updateDefinition(anyString(), anyString()))
                .thenThrow(new TermNotFoundException("NonExistent"));

        mockMvc.perform(put(BASE_URL + "/NonExistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Some definition"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void update_rejects_empty_definition_with_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_rejects_blank_definition_with_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_rejects_definition_exceeding_max_length_with_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "a".repeat(1001)))))
                .andExpect(status().isBadRequest());
    }
}
