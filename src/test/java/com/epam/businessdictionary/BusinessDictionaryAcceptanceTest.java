package com.epam.businessdictionary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BusinessDictionaryAcceptanceTest {

    private static final String TERMS_URL = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    // AC-01: Create Term Successfully
    @Test
    void creates_term_returns_201() {
        HttpEntity<String> request = new HttpEntity<>(
                """
                {"term": "Microservice", "definition": "A small independently deployable service"}
                """,
                jsonHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsEntry("term", "Microservice");
        assertThat(response.getBody()).containsEntry("definition", "A small independently deployable service");
        assertThat(response.getBody()).containsKey("createdAt");
        assertThat(response.getBody()).containsKey("updatedAt");
    }

    // AC-02: Reject Duplicate Term (case-insensitive match)
    @Test
    void rejects_duplicate_term_with_409() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "KPI", "definition": "Key Performance Indicator"}
                        """,
                        jsonHeaders()),
                Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "kpi", "definition": "Key Performance Indicator (lowercase duplicate)"}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // AC-03: Read Existing Term
    @Test
    void reads_existing_term_returns_200() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "ROI", "definition": "Return on Investment"}
                        """,
                        jsonHeaders()),
                Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(TERMS_URL + "/ROI", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("term", "ROI");
        assertThat(response.getBody()).containsEntry("definition", "Return on Investment");
    }

    // AC-04: Return Not Found For Missing Term
    @Test
    void returns_404_for_missing_term() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                TERMS_URL + "/ThisTermAbsolutelyDoesNotExist", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // AC-05: Update Existing Term
    @Test
    void updates_existing_term_returns_200() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "SLA", "definition": "Service Level Agreement (original)"}
                        """,
                        jsonHeaders()),
                Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL + "/SLA", HttpMethod.PUT,
                new HttpEntity<>(
                        """
                        {"definition": "Service Level Agreement (updated)"}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("definition", "Service Level Agreement (updated)");
        assertThat(response.getBody()).containsKey("updatedAt");
    }

    // AC-06: Reject blank term on create
    @Test
    void rejects_create_with_blank_term_with_400() {
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "", "definition": "Some valid definition"}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject blank definition on create
    @Test
    void rejects_create_with_blank_definition_with_400() {
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "ValidTerm", "definition": ""}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject term exceeding max length (100 characters)
    @Test
    void rejects_create_with_term_exceeding_max_length_with_400() {
        String oversizedTerm = "A".repeat(101);
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        "{\"term\": \"" + oversizedTerm + "\", \"definition\": \"Some definition\"}",
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject definition exceeding max length (1000 characters)
    @Test
    void rejects_create_with_definition_exceeding_max_length_with_400() {
        String oversizedDefinition = "D".repeat(1001);
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        "{\"term\": \"ValidTerm2\", \"definition\": \"" + oversizedDefinition + "\"}",
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject blank definition on update
    @Test
    void rejects_update_with_blank_definition_with_400() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "MVP", "definition": "Minimum Viable Product"}
                        """,
                        jsonHeaders()),
                Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL + "/MVP", HttpMethod.PUT,
                new HttpEntity<>(
                        """
                        {"definition": ""}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject update definition exceeding max length (1000 characters)
    @Test
    void rejects_update_with_definition_exceeding_max_length_with_400() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "OKR", "definition": "Objectives and Key Results"}
                        """,
                        jsonHeaders()),
                Map.class);

        String oversizedDefinition = "D".repeat(1001);
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL + "/OKR", HttpMethod.PUT,
                new HttpEntity<>(
                        "{\"definition\": \"" + oversizedDefinition + "\"}",
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // REQ-03 error path: 404 when updating a non-existent term
    @Test
    void returns_404_when_updating_nonexistent_term() {
        ResponseEntity<Map> response = restTemplate.exchange(
                TERMS_URL + "/TermThatNeverExisted99999", HttpMethod.PUT,
                new HttpEntity<>(
                        """
                        {"definition": "Definition for non-existent term"}
                        """,
                        jsonHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // REQ-02 business rule: term lookup is case-insensitive
    @Test
    void reads_term_case_insensitively_returns_200() {
        restTemplate.exchange(TERMS_URL, HttpMethod.POST,
                new HttpEntity<>(
                        """
                        {"term": "EBITDA", "definition": "Earnings Before Interest, Taxes, Depreciation, Amortization"}
                        """,
                        jsonHeaders()),
                Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(TERMS_URL + "/ebitda", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("term", "EBITDA");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
