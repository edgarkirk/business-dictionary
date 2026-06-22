package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
@MockBean(JpaMetamodelMappingContext.class)
class DictionaryControllerTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService dictionaryService;

    @Test
    void returns201ForValidCreateRequest() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Bounded Context");
        request.setDefinition("A DDD concept.");

        DictionaryTermResponse response = buildResponse("Bounded Context", "A DDD concept.");
        when(dictionaryService.createTerm(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.term").value("Bounded Context"))
            .andExpect(jsonPath("$.definition").value("A DDD concept."))
            .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void returns409ForDuplicateTerm() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Bounded Context");
        request.setDefinition("A DDD concept.");

        when(dictionaryService.createTerm(any()))
            .thenThrow(new DuplicateTermException("Term already exists: Bounded Context"));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200ForExistingTermLookup() throws Exception {
        DictionaryTermResponse response = buildResponse("Bounded Context", "A DDD concept.");
        when(dictionaryService.getTerm("Bounded Context")).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/Bounded Context"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.term").value("Bounded Context"));
    }

    @Test
    void returns404ForMissingTermLookup() throws Exception {
        when(dictionaryService.getTerm("Unknown"))
            .thenThrow(new TermNotFoundException("Term not found: Unknown"));

        mockMvc.perform(get(BASE_URL + "/Unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200ForValidUpdateRequest() throws Exception {
        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("Updated definition.");

        DictionaryTermResponse response = buildResponse("Bounded Context", "Updated definition.");
        when(dictionaryService.updateDefinition(eq("Bounded Context"), any())).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/Bounded Context")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.definition").value("Updated definition."));
    }

    @Test
    void returns400ForBlankTerm() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("");
        request.setDefinition("A definition.");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returns400ForBlankDefinition() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Some Term");
        request.setDefinition("");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ---- helpers ----

    private DictionaryTermResponse buildResponse(String term, String definition) {
        return new DictionaryTermResponse(UUID.randomUUID(), term, definition, Instant.now(), Instant.now());
    }
}
