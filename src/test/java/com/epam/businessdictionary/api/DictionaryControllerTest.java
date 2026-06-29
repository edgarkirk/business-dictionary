package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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

    @Test
    void creates_term_successfully() throws Exception {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        given(dictionaryService.createTerm("Agile", "Iterative development")).willReturn(entry);

        String body = """
                {"term": "Agile", "definition": "Iterative development"}
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Agile"))
                .andExpect(jsonPath("$.definition").value("Iterative development"));
    }

    @Test
    void returns_409_for_duplicate_term() throws Exception {
        // Arrange
        given(dictionaryService.createTerm(anyString(), anyString()))
                .willThrow(new DuplicateTermException("Agile"));

        String body = """
                {"term": "Agile", "definition": "Iterative development"}
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void returns_400_for_blank_term() throws Exception {
        // Arrange
        String body = """
                {"term": "", "definition": "Some definition"}
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_400_for_blank_definition() throws Exception {
        // Arrange
        String body = """
                {"term": "Agile", "definition": ""}
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_400_for_term_exceeding_max_length() throws Exception {
        // Arrange
        String longTerm = "A".repeat(101);
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<String, String>() {{
                    put("term", longTerm);
                    put("definition", "Some definition");
                }});

        // Act & Assert
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void gets_term_successfully() throws Exception {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Iterative development");
        given(dictionaryService.getTerm("Agile")).willReturn(entry);

        // Act & Assert
        mockMvc.perform(get("/api/v1/dictionary/terms/Agile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Agile"))
                .andExpect(jsonPath("$.definition").value("Iterative development"));
    }

    @Test
    void returns_404_for_missing_term() throws Exception {
        // Arrange
        given(dictionaryService.getTerm("Unknown")).willThrow(new TermNotFoundException("Unknown"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/dictionary/terms/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updates_term_successfully() throws Exception {
        // Arrange
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Agile", "agile", "Updated definition");
        given(dictionaryService.updateTerm("Agile", "Updated definition")).willReturn(entry);

        String body = """
                {"definition": "Updated definition"}
                """;

        // Act & Assert
        mockMvc.perform(put("/api/v1/dictionary/terms/Agile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Agile"))
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void returns_404_when_updating_nonexistent_term() throws Exception {
        // Arrange
        given(dictionaryService.updateTerm(anyString(), anyString()))
                .willThrow(new TermNotFoundException("Unknown"));

        String body = """
                {"definition": "Some definition"}
                """;

        // Act & Assert
        mockMvc.perform(put("/api/v1/dictionary/terms/Unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns_400_for_blank_definition_on_update() throws Exception {
        // Arrange
        String body = """
                {"definition": ""}
                """;

        // Act & Assert
        mockMvc.perform(put("/api/v1/dictionary/terms/Agile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
