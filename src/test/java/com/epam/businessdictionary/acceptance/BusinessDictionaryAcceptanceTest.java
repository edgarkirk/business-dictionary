package com.epam.businessdictionary.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BusinessDictionaryAcceptanceTest {

    private static final String BASE_PATH = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    // ── AC-01: POST valid input → 201 Created + full representation ─────────

    @Test
    @SuppressWarnings("unchecked")
    void creates_term_returns_201_with_term_representation() {
        var body = Map.of("term", "Microservice", "definition", "A small independent service");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH, HttpMethod.POST, jsonRequest(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("term")).isEqualTo("Microservice");
        assertThat(response.getBody().get("definition")).isEqualTo("A small independent service");
    }

    // ── AC-02: POST duplicate term (different casing) → 409 Conflict ────────

    @Test
    @SuppressWarnings("unchecked")
    void creates_duplicate_term_different_casing_returns_409() {
        restTemplate.exchange(BASE_PATH, HttpMethod.POST,
                jsonRequest(Map.of("term", "API Gateway", "definition", "A server-side entry point")),
                Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(BASE_PATH, HttpMethod.POST,
                jsonRequest(Map.of("term", "api gateway", "definition", "Another definition")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // ── AC-03: GET existing term → 200 OK + full representation ─────────────

    @Test
    @SuppressWarnings("unchecked")
    void reads_existing_term_returns_200_with_term_representation() {
        restTemplate.exchange(BASE_PATH, HttpMethod.POST,
                jsonRequest(Map.of("term", "Domain Model", "definition", "A conceptual model of the domain")),
                Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE_PATH + "/Domain Model", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("term")).isEqualTo("Domain Model");
        assertThat(response.getBody().get("definition")).isEqualTo("A conceptual model of the domain");
    }

    // ── AC-04: GET missing term → 404 Not Found ──────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void reads_missing_term_returns_404() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                BASE_PATH + "/TermThatDefinitelyDoesNotExist", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── AC-05a: PUT existing term → 200 OK + updated representation ──────────

    @Test
    @SuppressWarnings("unchecked")
    void updates_term_definition_returns_200_with_updated_representation() {
        restTemplate.exchange(BASE_PATH, HttpMethod.POST,
                jsonRequest(Map.of("term", "Idempotency", "definition", "Original definition")),
                Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH + "/Idempotency", HttpMethod.PUT,
                jsonRequest(Map.of("definition", "Updated definition for idempotency")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("definition")).isEqualTo("Updated definition for idempotency");
    }

    // ── AC-05b: PUT missing term → 404 Not Found ─────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void updates_nonexistent_term_returns_404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH + "/TermThatDoesNotExistForUpdate", HttpMethod.PUT,
                jsonRequest(Map.of("definition", "Some definition")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── AC-06a: POST blank term → 400 Bad Request ────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void creates_term_with_blank_term_returns_400() {
        var body = Map.of("term", "", "definition", "Some definition");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH, HttpMethod.POST, jsonRequest(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── AC-06b: POST oversized term (>100 chars) → 400 Bad Request ───────────

    @Test
    @SuppressWarnings("unchecked")
    void creates_term_with_oversized_term_returns_400() {
        var body = Map.of("term", "A".repeat(101), "definition", "Some definition");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH, HttpMethod.POST, jsonRequest(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── AC-06c: POST blank definition → 400 Bad Request ─────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void creates_term_with_blank_definition_returns_400() {
        var body = Map.of("term", "ValidTermName", "definition", "");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH, HttpMethod.POST, jsonRequest(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── AC-06d: POST oversized definition (>1000 chars) → 400 Bad Request ────

    @Test
    @SuppressWarnings("unchecked")
    void creates_term_with_oversized_definition_returns_400() {
        var body = Map.of("term", "AnotherValidTerm", "definition", "A".repeat(1001));

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH, HttpMethod.POST, jsonRequest(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── AC-06e: PUT blank definition → 400 Bad Request ───────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void updates_term_with_blank_definition_returns_400() {
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH + "/SomeTerm", HttpMethod.PUT,
                jsonRequest(Map.of("definition", "")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── AC-06f: PUT oversized definition (>1000 chars) → 400 Bad Request ─────

    @Test
    @SuppressWarnings("unchecked")
    void updates_term_with_oversized_definition_returns_400() {
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_PATH + "/SomeTerm", HttpMethod.PUT,
                jsonRequest(Map.of("definition", "A".repeat(1001))),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private HttpEntity<Map<String, String>> jsonRequest(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
