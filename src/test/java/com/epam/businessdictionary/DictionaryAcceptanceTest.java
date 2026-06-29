package com.epam.businessdictionary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DictionaryAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Test
    void AC01_creates_term_and_returns_201() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "REST", "definition": "Representational State Transfer"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.term").value("REST"))
                .andExpect(jsonPath("$.definition").value("Representational State Transfer"))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void AC02_rejects_duplicate_term_case_insensitively() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "REST", "definition": "First definition"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "rest", "definition": "Second definition"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void AC03_reads_existing_term_case_insensitively() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "API", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void AC04_returns_404_for_missing_term() throws Exception {
        mockMvc.perform(get(BASE_URL + "/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void AC05_updates_definition_and_returns_updated_term() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "SLA", "definition": "Original definition"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put(BASE_URL + "/SLA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated Service Level Agreement definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("SLA"))
                .andExpect(jsonPath("$.definition").value("Updated Service Level Agreement definition"))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void AC06_rejects_invalid_input_empty_term() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void AC06_rejects_invalid_input_empty_definition() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "TERM", "definition": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void AC06_rejects_term_exceeding_max_length() throws Exception {
        String longTerm = "A".repeat(101);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"" + longTerm + "\", \"definition\": \"Some definition\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void update_returns_404_for_missing_term() throws Exception {
        mockMvc.perform(put(BASE_URL + "/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Some definition"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
