package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private DictionaryService dictionaryService;

    @Test
    void create_returns_201_with_term_response() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Microservice", "A small independent service");
        when(dictionaryService.create("Microservice", "A small independent service")).thenReturn(entry);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"Microservice","definition":"A small independent service"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Microservice"))
                .andExpect(jsonPath("$.definition").value("A small independent service"));
    }

    @Test
    void create_returns_400_when_term_is_blank() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"","definition":"Some definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_returns_400_when_definition_is_blank() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"ValidTerm","definition":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_returns_409_when_term_already_exists() throws Exception {
        when(dictionaryService.create(eq("Agile"), eq("Iterative method")))
                .thenThrow(new DuplicateTermException("Agile"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"Agile","definition":"Iterative method"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void find_by_term_returns_200_with_entry() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("Scrum", "Agile framework");
        when(dictionaryService.findByTerm("Scrum")).thenReturn(entry);

        mockMvc.perform(get("/api/v1/dictionary/terms/Scrum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Scrum"))
                .andExpect(jsonPath("$.definition").value("Agile framework"));
    }

    @Test
    void find_by_term_returns_404_when_not_found() throws Exception {
        when(dictionaryService.findByTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get("/api/v1/dictionary/terms/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_definition_returns_200_with_updated_entry() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("DevOps", "Updated definition");
        when(dictionaryService.updateDefinition("DevOps", "Updated definition")).thenReturn(entry);

        mockMvc.perform(put("/api/v1/dictionary/terms/DevOps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"Updated definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("DevOps"))
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void update_definition_returns_404_when_term_not_found() throws Exception {
        when(dictionaryService.updateDefinition(eq("Missing"), eq("New def")))
                .thenThrow(new TermNotFoundException("Missing"));

        mockMvc.perform(put("/api/v1/dictionary/terms/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"New def"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_definition_returns_400_when_definition_is_blank() throws Exception {
        mockMvc.perform(put("/api/v1/dictionary/terms/SomeTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
