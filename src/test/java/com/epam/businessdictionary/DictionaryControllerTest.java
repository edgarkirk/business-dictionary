package com.epam.businessdictionary;

import com.epam.businessdictionary.api.DictionaryController;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.config.RestExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DictionaryController.class, RestExceptionHandler.class})
class DictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DictionaryService dictionaryService;

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    private DictionaryTermResponse sampleResponse() {
        return new DictionaryTermResponse(
                UUID.randomUUID(),
                "api",
                "Application Programming Interface",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    @Test
    void returns201ForValidCreate() throws Exception {
        when(dictionaryService.createTerm(any())).thenReturn(sampleResponse());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "api", "definition": "Application Programming Interface"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("api"))
                .andExpect(jsonPath("$.definition").value("Application Programming Interface"));
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        when(dictionaryService.createTerm(any())).thenThrow(new DuplicateTermException("api"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "api", "definition": "Some definition"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingLookup() throws Exception {
        when(dictionaryService.getTerm("api")).thenReturn(sampleResponse());

        mockMvc.perform(get(BASE_URL + "/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("api"));
    }

    @Test
    void returns404ForMissingLookup() throws Exception {
        when(dictionaryService.getTerm("unknown")).thenThrow(new TermNotFoundException("unknown"));

        mockMvc.perform(get(BASE_URL + "/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdate() throws Exception {
        DictionaryTermResponse updated = new DictionaryTermResponse(
                UUID.randomUUID(), "api", "Updated definition", OffsetDateTime.now(), OffsetDateTime.now());
        when(dictionaryService.updateDefinition(eq("api"), any())).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"definition": "Updated definition"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition"));
    }

    @Test
    void returns400ForBlankTerm() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "", "definition": "Some definition"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returns400ForBlankDefinition() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"term": "api", "definition": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
