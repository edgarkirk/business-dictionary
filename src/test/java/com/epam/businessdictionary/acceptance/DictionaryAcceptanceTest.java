package com.epam.businessdictionary.acceptance;

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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class DictionaryAcceptanceTest {

    private static final String BASE = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    // AC-01: Create Term Successfully — POST returns 201 with full term representation
    @Test
    @SuppressWarnings("rawtypes")
    void creates_term_returns_201() {
        Map<String, String> request = Map.of(
                "term", "API",
                "definition", "Application Programming Interface");

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsEntry("term", "API");
        assertThat(response.getBody()).containsEntry("definition", "Application Programming Interface");
        assertThat(response.getBody()).containsKey("createdAt");
        assertThat(response.getBody()).containsKey("updatedAt");
    }

    // AC-02: Reject Duplicate Term — same term with different letter casing returns 409
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_duplicate_term_with_409() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "SDK", "definition", "Software Development Kit"), Map.class);

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "sdk", "definition", "Lowercase duplicate"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    // AC-03: Read Existing Term — GET /terms/{term} returns 200 with term and definition
    @Test
    @SuppressWarnings("rawtypes")
    void reads_existing_term_returns_200() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "REST", "definition", "Representational State Transfer"), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/REST", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("term", "REST");
        assertThat(response.getBody()).containsEntry("definition", "Representational State Transfer");
        assertThat(response.getBody()).containsKey("id");
        assertThat(response.getBody()).containsKey("createdAt");
        assertThat(response.getBody()).containsKey("updatedAt");
    }

    // AC-04: Return Not Found For Missing Term — GET for unknown term returns 404
    @Test
    @SuppressWarnings("rawtypes")
    void returns_not_found_for_missing_term() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                BASE + "/TermThatDoesNotExist", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // AC-05: Update Existing Term — PUT returns 200 with updated definition
    @Test
    @SuppressWarnings("rawtypes")
    void updates_existing_term_returns_200() throws InterruptedException {
        restTemplate.postForEntity(BASE,
                Map.of("term", "JPA", "definition", "Java Persistence API"), Map.class);

        Thread.sleep(10);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/JPA", HttpMethod.PUT,
                jsonEntity(Map.of("definition", "Jakarta Persistence API")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("term", "JPA");
        assertThat(response.getBody()).containsEntry("definition", "Jakarta Persistence API");
        assertThat(response.getBody()).containsKey("updatedAt");
    }

    // AC-05 (extended): createdAt must not change after an update
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void update_preserves_created_at() throws InterruptedException {
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(BASE,
                Map.of("term", "ORM", "definition", "Object Relational Mapping"), Map.class);
        String originalCreatedAt = (String) createResponse.getBody().get("createdAt");

        Thread.sleep(10);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                BASE + "/ORM", HttpMethod.PUT,
                jsonEntity(Map.of("definition", "Object Relational Mapping — revised")),
                Map.class);

        assertThat(updateResponse.getBody().get("createdAt")).isEqualTo(originalCreatedAt);
    }

    // AC-05 (extended): updatedAt must change after an update
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void update_changes_updated_at() throws InterruptedException {
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(BASE,
                Map.of("term", "DTO", "definition", "Data Transfer Object"), Map.class);
        String originalUpdatedAt = (String) createResponse.getBody().get("updatedAt");

        Thread.sleep(10);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                BASE + "/DTO", HttpMethod.PUT,
                jsonEntity(Map.of("definition", "Data Transfer Object — revised")),
                Map.class);

        assertThat(updateResponse.getBody().get("updatedAt")).isNotEqualTo(originalUpdatedAt);
    }

    // AC-06: Reject Invalid Input — blank term returns 400
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_blank_term_with_400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "", "definition", "Some valid definition"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject Invalid Input — blank definition on create returns 400
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_blank_definition_on_create_with_400() {
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "UniqueTermAlpha", "definition", ""), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject Invalid Input — term exceeding 100 characters returns 400
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_term_exceeding_max_length_with_400() {
        String longTerm = "A".repeat(101);

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", longTerm, "definition", "Valid definition"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject Invalid Input — definition exceeding 1000 characters returns 400
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_definition_exceeding_max_length_with_400() {
        String longDefinition = "D".repeat(1001);

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE,
                Map.of("term", "UniqueTermBeta", "definition", longDefinition), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // AC-06: Reject Invalid Input — blank definition on update returns 400
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_blank_definition_on_update_with_400() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "POJO", "definition", "Plain Old Java Object"), Map.class);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/POJO", HttpMethod.PUT,
                jsonEntity(Map.of("definition", "")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // REQ-03 error path: updating a non-existent term returns 404
    @Test
    @SuppressWarnings("rawtypes")
    void rejects_update_for_missing_term_with_404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE + "/TermDoesNotExistAnywhere", HttpMethod.PUT,
                jsonEntity(Map.of("definition", "Some definition")),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // REQ-02 / Business Rule: term lookup must be case-insensitive
    @Test
    @SuppressWarnings("rawtypes")
    void reads_term_case_insensitively() {
        restTemplate.postForEntity(BASE,
                Map.of("term", "Maven", "definition", "Build automation tool for Java"), Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity(BASE + "/MAVEN", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("term", "Maven");
    }

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
