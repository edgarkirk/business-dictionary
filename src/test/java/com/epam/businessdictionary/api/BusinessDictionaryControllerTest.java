package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.BusinessDictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BusinessDictionaryController.class)
@Import(JpaAuditingConfig.class)
class BusinessDictionaryControllerTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessDictionaryService service;

    @Test
    void creates_term_returns_201() throws Exception {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("Microservice", "A small independent service");
        when(service.create("Microservice", "A small independent service")).thenReturn(entry);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "Microservice", "definition": "A small independent service"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Microservice"))
                .andExpect(jsonPath("$.definition").value("A small independent service"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void rejects_duplicate_term_with_409() throws Exception {
        when(service.create(anyString(), anyString())).thenThrow(new DuplicateTermException("Microservice"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "Microservice", "definition": "A small independent service"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void rejects_create_with_blank_term_returns_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejects_create_with_blank_definition_returns_400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "ValidTerm", "definition": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejects_create_with_term_exceeding_max_length_returns_400() throws Exception {
        String oversizedTerm = "A".repeat(101);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"" + oversizedTerm + "\", \"definition\": \"Some definition\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejects_create_with_definition_exceeding_max_length_returns_400() throws Exception {
        String oversizedDefinition = "D".repeat(1001);
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"term\": \"ValidTerm\", \"definition\": \"" + oversizedDefinition + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void reads_existing_term_returns_200() throws Exception {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("ROI", "Return on Investment");
        when(service.findByTerm("ROI")).thenReturn(entry);

        mockMvc.perform(get(BASE_URL + "/ROI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("ROI"))
                .andExpect(jsonPath("$.definition").value("Return on Investment"));
    }

    @Test
    void returns_404_for_missing_term() throws Exception {
        when(service.findByTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void updates_term_definition_returns_200() throws Exception {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("SLA", "Service Level Agreement (updated)");
        when(service.updateDefinition("SLA", "Service Level Agreement (updated)")).thenReturn(entry);

        mockMvc.perform(put(BASE_URL + "/SLA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Service Level Agreement (updated)"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Service Level Agreement (updated)"));
    }

    @Test
    void returns_404_when_updating_nonexistent_term() throws Exception {
        when(service.updateDefinition(anyString(), anyString())).thenThrow(new TermNotFoundException("Missing"));

        mockMvc.perform(put(BASE_URL + "/Missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Some definition"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void rejects_update_with_blank_definition_returns_400() throws Exception {
        mockMvc.perform(put(BASE_URL + "/SomeTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejects_update_with_definition_exceeding_max_length_returns_400() throws Exception {
        String oversizedDefinition = "D".repeat(1001);
        mockMvc.perform(put(BASE_URL + "/SomeTerm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"definition\": \"" + oversizedDefinition + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
