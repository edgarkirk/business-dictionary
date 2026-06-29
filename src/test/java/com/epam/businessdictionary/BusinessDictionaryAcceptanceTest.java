package com.epam.businessdictionary;

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
class BusinessDictionaryAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusinessDictionaryRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    // AC-01: Create Term Successfully
    @Test
    void creates_term_and_returns_201() throws Exception {
        String body = """
                {"term": "Agile", "definition": "Iterative software development approach"}
                """;

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.term").value("Agile"))
                .andExpect(jsonPath("$.definition").value("Iterative software development approach"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // AC-02: Reject Duplicate Term
    @Test
    void rejects_duplicate_term_case_insensitively() throws Exception {
        String body = """
                {"term": "Scrum", "definition": "Agile framework"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = """
                {"term": "SCRUM", "definition": "Another definition"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // AC-03: Read Existing Term
    @Test
    void retrieves_existing_term_by_name() throws Exception {
        String createBody = """
                {"term": "Kanban", "definition": "Lean workflow management"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/dictionary/terms/Kanban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Kanban"))
                .andExpect(jsonPath("$.definition").value("Lean workflow management"));
    }

    // AC-03 (case-insensitive lookup)
    @Test
    void retrieves_term_case_insensitively() throws Exception {
        String createBody = """
                {"term": "DevOps", "definition": "Development and Operations practices"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/dictionary/terms/devops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("DevOps"));
    }

    // AC-04: Return Not Found For Missing Term
    @Test
    void returns_404_for_missing_term() throws Exception {
        mockMvc.perform(get("/api/v1/dictionary/terms/NonExistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // AC-05: Update Existing Term
    @Test
    void updates_definition_and_returns_200() throws Exception {
        String createBody = """
                {"term": "Sprint", "definition": "Old definition"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        String updateBody = """
                {"definition": "A time-boxed period for completing work"}
                """;
        mockMvc.perform(put("/api/v1/dictionary/terms/Sprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Sprint"))
                .andExpect(jsonPath("$.definition").value("A time-boxed period for completing work"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // AC-06: Reject Invalid Input
    @Test
    void returns_400_for_blank_term() throws Exception {
        String body = """
                {"term": "", "definition": "Some definition"}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_400_for_blank_definition() throws Exception {
        String body = """
                {"term": "ValidTerm", "definition": ""}
                """;
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_400_for_term_exceeding_max_length() throws Exception {
        String longTerm = "A".repeat(101);
        String body = "{\"term\": \"" + longTerm + "\", \"definition\": \"Some definition\"}";

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_404_when_updating_nonexistent_term() throws Exception {
        String updateBody = """
                {"definition": "Some definition"}
                """;
        mockMvc.perform(put("/api/v1/dictionary/terms/NonExistentTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
