package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class DictionaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryService dictionaryService;

    @Test
    void returns201_forValidCreateRequest() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Bounded Context");
        request.setDefinition("A boundary within a domain.");

        DictionaryTermResponse response = termResponse("Bounded Context", "A boundary within a domain.");
        when(dictionaryService.createTerm(any(CreateTermRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term").value("Bounded Context"))
                .andExpect(jsonPath("$.definition").value("A boundary within a domain."));
    }

    @Test
    void returns409_forDuplicateTerm() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Bounded Context");
        request.setDefinition("A boundary within a domain.");

        when(dictionaryService.createTerm(any(CreateTermRequest.class)))
                .thenThrow(new DuplicateTermException("Bounded Context"));

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TERM"));
    }

    @Test
    void returns200_forExistingTermLookup() throws Exception {
        DictionaryTermResponse response = termResponse("Bounded Context", "A boundary within a domain.");
        when(dictionaryService.getTerm("Bounded Context")).thenReturn(response);

        mockMvc.perform(get("/api/v1/dictionary/terms/Bounded Context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Bounded Context"));
    }

    @Test
    void returns404_forMissingTermLookup() throws Exception {
        when(dictionaryService.getTerm("Unknown")).thenThrow(new TermNotFoundException("Unknown"));

        mockMvc.perform(get("/api/v1/dictionary/terms/Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TERM_NOT_FOUND"));
    }

    @Test
    void returns200_forValidUpdateRequest() throws Exception {
        UpdateDefinitionRequest request = new UpdateDefinitionRequest();
        request.setDefinition("Updated definition.");

        DictionaryTermResponse response = termResponse("Bounded Context", "Updated definition.");
        when(dictionaryService.updateDefinition(eq("Bounded Context"), any(UpdateDefinitionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/dictionary/terms/Bounded Context")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition").value("Updated definition."));
    }

    @Test
    void returns400_forBlankTerm() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("");
        request.setDefinition("A valid definition.");

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returns400_forBlankDefinition() throws Exception {
        CreateTermRequest request = new CreateTermRequest();
        request.setTerm("Valid Term");
        request.setDefinition("");

        mockMvc.perform(post("/api/v1/dictionary/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private DictionaryTermResponse termResponse(String term, String definition) {
        return new DictionaryTermResponse(UUID.randomUUID(), term, definition, Instant.now(), Instant.now());
    }
}
