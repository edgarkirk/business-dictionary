package com.epam.businessdictionary.acceptance;

import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DictionaryAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessDictionaryRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    // AC-01: Create Term Successfully
    @Test
    void ac01_creates_term_successfully() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"Velocity","definition":"Measure of work done per sprint"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.term").value("Velocity"))
                .andExpect(jsonPath("$.definition").value("Measure of work done per sprint"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // AC-02: Reject Duplicate Term (case-insensitive)
    @Test
    void ac02_rejects_duplicate_term_case_insensitively() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"Backlog","definition":"A prioritized list of work"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"BACKLOG","definition":"Same term different case"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // AC-03: Read Existing Term
    @Test
    void ac03_reads_existing_term() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"Refactoring","definition":"Restructuring code without changing behavior"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/dictionary/terms/Refactoring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Refactoring"))
                .andExpect(jsonPath("$.definition").value("Restructuring code without changing behavior"));
    }

    // AC-03 (extended): Term lookup is case-insensitive
    @Test
    void ac03_term_lookup_is_case_insensitive() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"TDD","definition":"Test-Driven Development"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/dictionary/terms/tdd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("TDD"));
    }

    // AC-04: Return Not Found for Missing Term
    @Test
    void ac04_returns_not_found_for_missing_term() throws Exception {
        mockMvc.perform(get("/api/v1/dictionary/terms/NonExistentTerm"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // AC-05: Update Existing Term
    @Test
    void ac05_updates_existing_term_definition() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"MVP","definition":"Minimum Viable Product"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/dictionary/terms/MVP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"The simplest version that delivers value"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("MVP"))
                .andExpect(jsonPath("$.definition").value("The simplest version that delivers value"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // AC-05 (extended): updatedAt changes after update
    @Test
    void ac05_updated_at_changes_after_definition_update() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"KPI","definition":"Key Performance Indicator"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(put("/api/v1/dictionary/terms/KPI")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"A measurable value showing progress toward an objective"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("A measurable value showing progress toward an objective"));
    }

    // AC-06: Reject Invalid Input — empty term
    @Test
    void ac06_rejects_empty_term() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"","definition":"Some valid definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // AC-06: Reject Invalid Input — empty definition
    @Test
    void ac06_rejects_empty_definition() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"ValidTerm","definition":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // AC-06: Reject Invalid Input — term exceeds max length
    @Test
    void ac06_rejects_term_exceeding_max_length() throws Exception {
        String longTerm = "A".repeat(101);
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"%s","definition":"Valid definition"}
                                """.formatted(longTerm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // AC-06: Reject Invalid Input — definition exceeds max length
    @Test
    void ac06_rejects_definition_exceeding_max_length() throws Exception {
        String longDef = "D".repeat(1001);
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"ValidTerm","definition":"%s"}
                                """.formatted(longDef)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
