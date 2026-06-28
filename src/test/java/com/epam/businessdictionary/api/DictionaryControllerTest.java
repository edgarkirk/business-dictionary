package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessTerm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void creates_term_successfully() throws Exception {
        BusinessTerm term = termWithId("API", "Application Programming Interface");
        when(dictionaryService.create("API", "Application Programming Interface")).thenReturn(term);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("API", "Application Programming Interface"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void rejects_duplicate_term() throws Exception {
        when(dictionaryService.create(anyString(), anyString())).thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("API", "Some definition"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void rejects_invalid_input_blank_term() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejects_invalid_input_blank_definition() throws Exception {
        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTermRequest("API", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void gets_existing_term() throws Exception {
        BusinessTerm term = termWithId("API", "Application Programming Interface");
        when(dictionaryService.getByTerm("API")).thenReturn(term);

        mockMvc.perform(get("/api/v1/dictionary/terms/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns_not_found_for_missing_term() throws Exception {
        when(dictionaryService.getByTerm(anyString())).thenThrow(new TermNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/v1/dictionary/terms/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void updates_term_definition() throws Exception {
        BusinessTerm term = termWithId("API", "Updated definition");
        when(dictionaryService.updateDefinition("API", "Updated definition")).thenReturn(term);

        mockMvc.perform(put("/api/v1/dictionary/terms/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTermRequest("Updated definition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void returns_not_found_on_update_for_missing_term() throws Exception {
        when(dictionaryService.updateDefinition(anyString(), anyString())).thenThrow(new TermNotFoundException("UNKNOWN"));

        mockMvc.perform(put("/api/v1/dictionary/terms/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTermRequest("Some definition"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void rejects_update_with_blank_definition() throws Exception {
        mockMvc.perform(put("/api/v1/dictionary/terms/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateTermRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static BusinessTerm termWithId(String term, String definition) {
        BusinessTerm bt = new BusinessTerm(term, definition);
        setField(bt, "id", UUID.randomUUID());
        setField(bt, "createdAt", Instant.now());
        setField(bt, "updatedAt", Instant.now());
        return bt;
    }

    private static void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
