package com.epam.businessdictionary.acceptance;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance tests for the Business Dictionary Service API.
 *
 * Covers every acceptance criterion from REQUIREMENTS.md:
 *   AC-01 – create term returns 201
 *   AC-02 – reject duplicate term (case-insensitive) with 409
 *   AC-03 – retrieve existing term returns 200 (case-insensitive lookup)
 *   AC-04 – return 404 for missing term
 *   AC-05 – update existing term returns 200
 *   AC-06 – reject invalid input with 400
 *
 * Tests WILL fail until production code is implemented — this is the expected RED state.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("unchecked")
class BusinessDictionaryAcceptanceTest {

    private static final String BASE = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    // -----------------------------------------------------------------------
    // AC-01: Create Term Successfully
    // -----------------------------------------------------------------------

    @Test
    void creates_term_returns_201() {
        var request = Map.of("term", "Agile", "definition", "A software development methodology.");

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .containsKeys("id", "term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("term")).isEqualTo("Agile");
        assertThat(response.getBody().get("definition")).isEqualTo("A software development methodology.");
    }

    @Test
    void created_term_has_non_null_timestamps() {
        var request = Map.of("term", "Iteration", "definition", "A fixed-length development cycle.");

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("createdAt")).isNotNull();
        assertThat(response.getBody().get("updatedAt")).isNotNull();
    }

    // -----------------------------------------------------------------------
    // AC-02: Reject Duplicate Term (case-insensitive)
    // -----------------------------------------------------------------------

    @Test
    void rejects_duplicate_term_with_409() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Scrum", "definition", "An agile framework."), Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "SCRUM", "definition", "Duplicate attempt."), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // -----------------------------------------------------------------------
    // AC-03: Read Existing Term (including case-insensitive lookup)
    // -----------------------------------------------------------------------

    @Test
    void retrieves_existing_term_returns_200() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Kanban", "definition", "A visual workflow management method."), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/Kanban", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsKeys("id", "term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("definition")).isEqualTo("A visual workflow management method.");
    }

    @Test
    void retrieves_term_case_insensitively() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Retrospective", "definition", "A meeting to reflect on the past sprint."), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/RETROSPECTIVE", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("term")).isEqualTo("Retrospective");
    }

    // -----------------------------------------------------------------------
    // AC-04: Return Not Found for Missing Term
    // -----------------------------------------------------------------------

    @Test
    void returns_404_for_missing_term() {
        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/NonExistentTermXyz", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -----------------------------------------------------------------------
    // AC-05: Update Existing Term
    // -----------------------------------------------------------------------

    @Test
    void updates_existing_term_returns_200() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Sprint", "definition", "A time box."), Map.class);
        var updateRequest = Map.of("definition", "A fixed-length iteration in Scrum.");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/Sprint", HttpMethod.PUT, new HttpEntity<>(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsKeys("id", "term", "definition", "createdAt", "updatedAt");
        assertThat(response.getBody().get("definition")).isEqualTo("A fixed-length iteration in Scrum.");
    }

    @Test
    void returns_404_when_updating_nonexistent_term() {
        var updateRequest = Map.of("definition", "Definition for a non-existent term.");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/GhostTerm", HttpMethod.PUT, new HttpEntity<>(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -----------------------------------------------------------------------
    // AC-06: Reject Invalid Input — create
    // -----------------------------------------------------------------------

    @Test
    void rejects_blank_term_with_400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "", "definition", "Some valid definition."), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejects_blank_definition_with_400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "Backlog", "definition", ""), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejects_term_exceeding_max_length_with_400() {
        String oversizedTerm = "T".repeat(101);
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", oversizedTerm, "definition", "Valid definition."), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejects_definition_exceeding_max_length_with_400() {
        String oversizedDefinition = "D".repeat(1001);
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "Velocity", "definition", oversizedDefinition), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -----------------------------------------------------------------------
    // AC-06: Reject Invalid Input — update
    // -----------------------------------------------------------------------

    @Test
    void rejects_blank_definition_on_update_with_400() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Burndown", "definition", "A chart showing remaining work."), Map.class);
        var updateRequest = Map.of("definition", "");

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/Burndown", HttpMethod.PUT, new HttpEntity<>(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejects_definition_exceeding_max_length_on_update_with_400() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Impediment", "definition", "An obstacle to progress."), Map.class);
        var updateRequest = Map.of("definition", "D".repeat(1001));

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/Impediment", HttpMethod.PUT, new HttpEntity<>(updateRequest), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
