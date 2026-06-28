package com.epam.businessdictionary.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DictionaryApiAcceptanceTests {

    private static final String BASE = "/api/v1/dictionary/terms";
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate restTemplate;

    // AC-01: Create Term Successfully — POST returns 201 with full term representation
    @Test
    void creates_term_successfully_returns_201() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "Agile", "definition", "An iterative approach to project management."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsKeys("id", "term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody()).containsEntry("term", "Agile");
        assertThat(response.getBody()).containsEntry("definition", "An iterative approach to project management.");
    }

    // AC-02: Reject Duplicate Term — same term in different casing returns 409
    @Test
    void rejects_duplicate_term_with_409() {
        post(Map.of("term", "Scrum", "definition", "A framework for agile development."));

        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "SCRUM", "definition", "Another definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // AC-02 (additional): lowercase duplicate also rejected
    @Test
    void rejects_duplicate_term_in_lowercase_with_409() {
        post(Map.of("term", "Backlog", "definition", "Ordered list of work items."));

        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "backlog", "definition", "Duplicate definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // AC-03: Read Existing Term — GET returns 200 with term and definition
    @Test
    void reads_existing_term_returns_200() {
        post(Map.of("term", "Kanban", "definition", "A visual workflow management method."));

        ResponseEntity<Map<String, Object>> response = get("Kanban");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("term", "Kanban");
        assertThat(response.getBody()).containsEntry("definition", "A visual workflow management method.");
        assertThat(response.getBody()).containsKeys("id", "createdAt", "updatedAt");
    }

    // AC-03 (additional): term lookup is case-insensitive
    @Test
    void reads_existing_term_case_insensitively() {
        post(Map.of("term", "Sprint", "definition", "A time-boxed iteration."));

        ResponseEntity<Map<String, Object>> response = get("SPRINT");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("term", "Sprint");
    }

    // AC-04: Return Not Found For Missing Term — GET returns 404 with error body
    @Test
    void returns_not_found_for_missing_term_returns_404() {
        ResponseEntity<Map<String, Object>> response = get("nonexistent-term-xyz");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKeys("code", "message");
    }

    // AC-05: Update Existing Term — PUT returns 200 with updated definition
    @Test
    void updates_existing_term_returns_200() {
        post(Map.of("term", "DevOps", "definition", "Original definition."));

        ResponseEntity<Map<String, Object>> response = put("DevOps",
                Map.of("definition", "Practices uniting development and operations."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("definition", "Practices uniting development and operations.");
        assertThat(response.getBody()).containsKeys("id", "term", "createdAt", "updatedAt");
    }

    // AC-05 (additional): updatedAt changes after update, createdAt stays the same
    @Test
    void updates_existing_term_changes_updatedAt_but_not_createdAt() throws InterruptedException {
        ResponseEntity<Map<String, Object>> created = post(
                Map.of("term", "CI", "definition", "Continuous Integration."));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Object originalCreatedAt = created.getBody().get("createdAt");
        Object originalUpdatedAt = created.getBody().get("updatedAt");

        Thread.sleep(50);

        ResponseEntity<Map<String, Object>> updated = put("CI",
                Map.of("definition", "Continuous Integration: automated build and test on every commit."));

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("createdAt")).isEqualTo(originalCreatedAt);
        assertThat(updated.getBody().get("updatedAt")).isNotEqualTo(originalUpdatedAt);
    }

    // AC-05 (error path): Update non-existent term returns 404 with error body
    @Test
    void updates_nonexistent_term_returns_404() {
        ResponseEntity<Map<String, Object>> response = put("TermThatDoesNotExist",
                Map.of("definition", "Some valid definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKeys("code", "message");
    }

    // AC-06: Reject empty term on create
    @Test
    void rejects_empty_term_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "", "definition", "Some definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject blank (whitespace-only) term on create
    @Test
    void rejects_blank_term_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "   ", "definition", "Some definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject empty definition on create
    @Test
    void rejects_empty_definition_on_create_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "ValidTermA", "definition", ""));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject blank (whitespace-only) definition on create
    @Test
    void rejects_blank_definition_on_create_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "ValidTermB", "definition", "   "));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject term exceeding 100 characters
    @Test
    void rejects_term_exceeding_max_length_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "a".repeat(101), "definition", "Some definition."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject definition exceeding 1000 characters
    @Test
    void rejects_definition_exceeding_max_length_with_400() {
        ResponseEntity<Map<String, Object>> response = post(
                Map.of("term", "ValidTermC", "definition", "a".repeat(1001)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject empty definition on update — validation fires before term lookup
    @Test
    void update_rejects_empty_definition_with_400() {
        ResponseEntity<Map<String, Object>> response = put("AnyTerm",
                Map.of("definition", ""));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject blank definition on update
    @Test
    void update_rejects_blank_definition_with_400() {
        ResponseEntity<Map<String, Object>> response = put("AnyOtherTerm",
                Map.of("definition", "   "));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject definition exceeding 1000 characters on update
    @Test
    void update_rejects_definition_exceeding_max_length_with_400() {
        ResponseEntity<Map<String, Object>> response = put("AnyTerm2",
                Map.of("definition", "a".repeat(1001)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- helpers ---

    private ResponseEntity<Map<String, Object>> post(Map<String, String> body) {
        return restTemplate.exchange(BASE, HttpMethod.POST, jsonEntity(body), RESPONSE_TYPE);
    }

    private ResponseEntity<Map<String, Object>> get(String term) {
        return restTemplate.exchange(BASE + "/" + term, HttpMethod.GET, HttpEntity.EMPTY, RESPONSE_TYPE);
    }

    private ResponseEntity<Map<String, Object>> put(String term, Map<String, String> body) {
        return restTemplate.exchange(BASE + "/" + term, HttpMethod.PUT, jsonEntity(body), RESPONSE_TYPE);
    }

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
