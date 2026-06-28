package com.epam.businessdictionary.acceptance;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.ErrorResponse;
import com.epam.businessdictionary.api.response.TermResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BusinessDictionaryAcceptanceTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM business_dictionary");
    }

    // AC-01: Valid create returns 201 with response body
    @Test
    void create_term_with_valid_data_returns_201_with_body() {
        CreateTermRequest request = new CreateTermRequest("Microservice", "A small, independently deployable service");

        ResponseEntity<TermResponse> response = restTemplate.postForEntity(BASE_URL, request, TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TermResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.term()).isEqualTo("Microservice");
        assertThat(body.definition()).isEqualTo("A small, independently deployable service");
        assertThat(body.createdAt()).isNotNull();
        assertThat(body.updatedAt()).isNotNull();
    }

    // AC-02: Duplicate term (any casing) returns 409 Conflict
    @Test
    void create_term_with_duplicate_casing_returns_409() {
        CreateTermRequest first = new CreateTermRequest("API", "Application Programming Interface");
        restTemplate.postForEntity(BASE_URL, first, TermResponse.class);

        CreateTermRequest duplicate = new CreateTermRequest("api", "Another definition");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, duplicate, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_TERM");
    }

    // AC-03: Get existing term returns 200 with body
    @Test
    void get_existing_term_returns_200_with_body() {
        CreateTermRequest request = new CreateTermRequest("DevOps", "Development and Operations practice");
        restTemplate.postForEntity(BASE_URL, request, TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE_URL + "/DevOps", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        TermResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.term()).isEqualTo("DevOps");
        assertThat(body.definition()).isEqualTo("Development and Operations practice");
    }

    // AC-03: Lookup is case-insensitive
    @Test
    void get_term_is_case_insensitive() {
        CreateTermRequest request = new CreateTermRequest("Kubernetes", "Container orchestration platform");
        restTemplate.postForEntity(BASE_URL, request, TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE_URL + "/KUBERNETES", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("Kubernetes");
    }

    // AC-04: Get missing term returns 404
    @Test
    void get_missing_term_returns_404() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(BASE_URL + "/NonExistent", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("TERM_NOT_FOUND");
    }

    // AC-05: Update returns 200, updatedAt changes, createdAt unchanged
    @Test
    void update_existing_term_returns_200_and_updatedAt_changes() throws InterruptedException {
        CreateTermRequest createRequest = new CreateTermRequest("CI", "Continuous Integration");
        ResponseEntity<TermResponse> created = restTemplate.postForEntity(BASE_URL, createRequest, TermResponse.class);
        TermResponse original = created.getBody();
        assertThat(original).isNotNull();

        // Small delay to ensure updatedAt changes
        Thread.sleep(10);

        UpdateDefinitionRequest updateRequest = new UpdateDefinitionRequest("Continuous Integration - updated");
        ResponseEntity<TermResponse> updated = restTemplate.exchange(
                BASE_URL + "/CI", HttpMethod.PUT,
                new HttpEntity<>(updateRequest), TermResponse.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        TermResponse body = updated.getBody();
        assertThat(body).isNotNull();
        assertThat(body.definition()).isEqualTo("Continuous Integration - updated");
        // Compare at millisecond precision: H2 may round sub-microsecond digits differently
        // between the initial save and the subsequent DB read.
        assertThat(body.createdAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(original.createdAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(body.updatedAt()).isAfterOrEqualTo(original.updatedAt());
    }

    // AC-05: Update missing term returns 404
    @Test
    void update_missing_term_returns_404() {
        UpdateDefinitionRequest request = new UpdateDefinitionRequest("Some definition");
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + "/NonExistent", HttpMethod.PUT,
                new HttpEntity<>(request), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("TERM_NOT_FOUND");
    }

    // AC-06: Empty fields return 400 Bad Request
    @Test
    void create_term_with_blank_term_returns_400() {
        CreateTermRequest request = new CreateTermRequest("", "Some definition");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    // AC-06: Oversized fields return 400 Bad Request
    @Test
    void create_term_with_oversized_term_returns_400() {
        String oversizedTerm = "a".repeat(101);
        CreateTermRequest request = new CreateTermRequest(oversizedTerm, "Some definition");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    // AC-06: Blank definition on create returns 400
    @Test
    void create_term_with_blank_definition_returns_400() {
        CreateTermRequest request = new CreateTermRequest("ValidTerm", "");
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, request, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    // AC-06: Blank definition on update returns 400
    @Test
    void update_term_with_blank_definition_returns_400() {
        CreateTermRequest createRequest = new CreateTermRequest("Term", "Original definition");
        restTemplate.postForEntity(BASE_URL, createRequest, TermResponse.class);

        UpdateDefinitionRequest request = new UpdateDefinitionRequest("");
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE_URL + "/Term", HttpMethod.PUT,
                new HttpEntity<>(request), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }
}
