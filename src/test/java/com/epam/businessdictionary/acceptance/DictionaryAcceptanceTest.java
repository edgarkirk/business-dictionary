package com.epam.businessdictionary.acceptance;

import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DictionaryAcceptanceTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessDictionaryRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void AC01_creates_term_successfully() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "Microservice", "definition", "A small independently deployable service"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.term").value("Microservice"))
                .andExpect(jsonPath("$.definition").value("A small independently deployable service"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void AC02_rejects_duplicate_term_case_insensitively() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "DevOps", "definition", "Development and Operations practice"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "DEVOPS", "definition", "Duplicate with different casing"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void AC03_reads_existing_term() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "SLA", "definition", "Service Level Agreement"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/SLA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("SLA"))
                .andExpect(jsonPath("$.definition").value("Service Level Agreement"));
    }

    @Test
    void AC03_term_lookup_is_case_insensitive() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "REST", "definition", "Representational State Transfer"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/rest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("REST"));
    }

    @Test
    void AC04_returns_not_found_for_missing_term() throws Exception {
        mockMvc.perform(get(BASE_URL + "/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void AC05_updates_existing_term_definition() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "Kafka", "definition", "Original definition"))))
                .andExpect(status().isCreated());

        mockMvc.perform(put(BASE_URL + "/Kafka")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "A distributed event streaming platform"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.term").value("Kafka"))
                .andExpect(jsonPath("$.definition").value("A distributed event streaming platform"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void AC05_update_returns_404_for_missing_term() throws Exception {
        mockMvc.perform(put(BASE_URL + "/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("definition", "Some definition"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void AC06_rejects_blank_term() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "", "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void AC06_rejects_blank_definition() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "SomeTermXY", "definition", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void AC06_rejects_term_exceeding_max_length() throws Exception {
        String longTerm = "A".repeat(101);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", longTerm, "definition", "Some definition"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void AC06_rejects_definition_exceeding_max_length() throws Exception {
        String longDef = "A".repeat(1001);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("term", "ValidTerm", "definition", longDef))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
