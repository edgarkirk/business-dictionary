package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
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

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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

    private BusinessDictionaryEntry sampleEntry;

    @BeforeEach
    void setUp() throws Exception {
        sampleEntry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        // Use reflection to set id, createdAt, updatedAt since they are normally set by JPA
        var idField = BusinessDictionaryEntry.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(sampleEntry, UUID.randomUUID());

        var createdAtField = BusinessDictionaryEntry.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(sampleEntry, Instant.now());

        var updatedAtField = BusinessDictionaryEntry.class.getDeclaredField("updatedAt");
        updatedAtField.setAccessible(true);
        updatedAtField.set(sampleEntry, Instant.now());
    }

    @Test
    void post_creates_term_and_returns_201() throws Exception {
        when(dictionaryService.createTerm("API", "Application Programming Interface"))
                .thenReturn(sampleEntry);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void post_returns_409_on_duplicate_term() throws Exception {
        when(dictionaryService.createTerm("API", "Some definition"))
                .thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", "Some definition"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void post_returns_400_when_term_is_blank() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "", "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void post_returns_400_when_definition_is_blank() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "API", "definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void get_returns_term_and_200() throws Exception {
        when(dictionaryService.getTerm("API")).thenReturn(sampleEntry);

        mockMvc.perform(get("/api/v1/dictionary/terms/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void get_returns_404_when_term_not_found() throws Exception {
        when(dictionaryService.getTerm("missing")).thenThrow(new TermNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/dictionary/terms/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void put_updates_definition_and_returns_200() throws Exception {
        when(dictionaryService.updateTerm("API", "Updated definition")).thenReturn(sampleEntry);

        mockMvc.perform(put("/api/v1/dictionary/terms/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "Updated definition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"));
    }

    @Test
    void put_returns_404_when_term_not_found() throws Exception {
        when(dictionaryService.updateTerm("missing", "Updated definition"))
                .thenThrow(new TermNotFoundException("missing"));

        mockMvc.perform(put("/api/v1/dictionary/terms/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "Updated definition"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void put_returns_400_when_definition_is_blank() throws Exception {
        mockMvc.perform(put("/api/v1/dictionary/terms/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
