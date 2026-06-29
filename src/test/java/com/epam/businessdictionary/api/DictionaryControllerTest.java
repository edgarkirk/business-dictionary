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

import static org.mockito.ArgumentMatchers.eq;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService dictionaryService;

    @Test
    void creates_term_successfully() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        when(dictionaryService.createTerm("API", "Application Programming Interface")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"API","definition":"Application Programming Interface"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void rejects_duplicate_term_with_409() throws Exception {
        when(dictionaryService.createTerm(eq("API"), eq("Application Programming Interface")))
                .thenThrow(new DuplicateTermException("API"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"API","definition":"Application Programming Interface"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejects_invalid_input_on_create_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"","definition":"Some definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejects_missing_definition_on_create_with_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term":"API","definition":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns_existing_term() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Application Programming Interface");
        when(dictionaryService.getTerm("API")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("API"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns_not_found_for_missing_term() throws Exception {
        when(dictionaryService.getTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updates_existing_term() throws Exception {
        BusinessDictionaryEntry entry = new BusinessDictionaryEntry("API", "api", "Updated definition");
        when(dictionaryService.updateTerm("API", "Updated definition")).thenReturn(entry);

        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"Updated definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void returns_not_found_when_updating_missing_term() throws Exception {
        when(dictionaryService.updateTerm(eq("Missing"), eq("Some definition")))
                .thenThrow(new TermNotFoundException("Missing"));

        mockMvc.perform(put(BASE_URL + "/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":"Some definition"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejects_invalid_input_on_update_with_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/API")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
