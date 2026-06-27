package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

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

    // ── POST /terms ───────────────────────────────────────────────────────────

    @Test
    void creates_term_returns_201_with_term_representation() throws Exception {
        BusinessDictionaryEntry entry = buildEntry("Microservice", "A small independent service");
        when(dictionaryService.create("Microservice", "A small independent service")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "Microservice", "definition": "A small independent service"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Microservice"))
                .andExpect(jsonPath("$.definition").value("A small independent service"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void creates_duplicate_term_returns_409() throws Exception {
        when(dictionaryService.create("API Gateway", "A definition"))
                .thenThrow(new DuplicateTermException("API Gateway"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API Gateway", "definition": "A definition"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void creates_term_with_blank_term_returns_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creates_term_with_oversized_term_returns_400() throws Exception {
        String oversizedTerm = "A".repeat(101);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "%s", "definition": "Some definition"}
                                """.formatted(oversizedTerm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creates_term_with_blank_definition_returns_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "ValidTerm", "definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creates_term_with_oversized_definition_returns_400() throws Exception {
        String oversizedDef = "A".repeat(1001);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "ValidTerm", "definition": "%s"}
                                """.formatted(oversizedDef)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /terms/{term} ─────────────────────────────────────────────────────

    @Test
    void reads_existing_term_returns_200_with_representation() throws Exception {
        BusinessDictionaryEntry entry = buildEntry("Domain Model", "A conceptual model of the domain");
        when(dictionaryService.findByTerm("Domain Model")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/Domain Model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Domain Model"))
                .andExpect(jsonPath("$.definition").value("A conceptual model of the domain"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void reads_missing_term_returns_404() throws Exception {
        when(dictionaryService.findByTerm("MissingTerm"))
                .thenThrow(new TermNotFoundException("MissingTerm"));

        mockMvc.perform(get(BASE_URL + "/MissingTerm"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /terms/{term} ─────────────────────────────────────────────────────

    @Test
    void updates_term_returns_200_with_updated_representation() throws Exception {
        BusinessDictionaryEntry entry = buildEntry("Idempotency", "Updated definition");
        when(dictionaryService.update("Idempotency", "Updated definition")).thenReturn(entry);

        mockMvc.perform(put(BASE_URL + "/Idempotency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Idempotency"))
                .andExpect(jsonPath("$.definition").value("Updated definition"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updates_missing_term_returns_404() throws Exception {
        when(dictionaryService.update("MissingTerm", "Some definition"))
                .thenThrow(new TermNotFoundException("MissingTerm"));

        mockMvc.perform(put(BASE_URL + "/MissingTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Some definition"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updates_term_with_blank_definition_returns_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/SomeTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updates_term_with_oversized_definition_returns_400() throws Exception {
        String oversizedDef = "A".repeat(1001);
        mockMvc.perform(put(BASE_URL + "/SomeTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "%s"}
                                """.formatted(oversizedDef)))
                .andExpect(status().isBadRequest());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private BusinessDictionaryEntry buildEntry(String term, String definition) {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry(term, term.toLowerCase(), definition);
        ReflectionTestUtils.setField(entry, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(entry, "createdAt", Instant.now());
        ReflectionTestUtils.setField(entry, "updatedAt", Instant.now());
        return entry;
    }
}
