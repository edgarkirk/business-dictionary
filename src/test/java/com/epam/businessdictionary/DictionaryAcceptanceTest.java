package com.epam.businessdictionary;

import com.epam.businessdictionary.api.response.ErrorResponse;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DictionaryAcceptanceTest {

    private static final String BASE_URL = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessDictionaryRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void ac01_creates_term_successfully() {
        var body = Map.of("term", "API", "definition", "Application Programming Interface");

        ResponseEntity<TermResponse> response = restTemplate.postForEntity(BASE_URL, body, TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("API");
        assertThat(response.getBody().definition()).isEqualTo("Application Programming Interface");
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().createdAt()).isNotNull();
        assertThat(response.getBody().updatedAt()).isNotNull();
    }

    @Test
    void ac02_rejects_duplicate_term_case_insensitively() {
        restTemplate.postForEntity(BASE_URL, Map.of("term", "API", "definition", "Application Programming Interface"), TermResponse.class);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL,
                Map.of("term", "api", "definition", "Duplicate"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
    }

    @Test
    void ac03_reads_existing_term() {
        restTemplate.postForEntity(BASE_URL, Map.of("term", "SLA", "definition", "Service Level Agreement"), TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE_URL + "/SLA", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("SLA");
    }

    @Test
    void ac03_reads_term_case_insensitively() {
        restTemplate.postForEntity(BASE_URL, Map.of("term", "SLA", "definition", "Service Level Agreement"), TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE_URL + "/sla", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("SLA");
    }

    @Test
    void ac04_returns_not_found_for_missing_term() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(BASE_URL + "/nonexistent", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    void ac05_updates_existing_term() {
        restTemplate.postForEntity(BASE_URL, Map.of("term", "KPI", "definition", "Key Performance Indicator"), TermResponse.class);

        var updateBody = Map.of("definition", "Key Performance Indicators used to evaluate success");
        ResponseEntity<TermResponse> response = restTemplate.exchange(
                BASE_URL + "/KPI", HttpMethod.PUT, new HttpEntity<>(updateBody), TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().definition()).isEqualTo("Key Performance Indicators used to evaluate success");
        assertThat(response.getBody().updatedAt()).isNotNull();
    }

    @Test
    void ac06_rejects_blank_term_with_400() {
        var body = Map.of("term", "", "definition", "Some definition");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, body, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void ac06_rejects_blank_definition_with_400() {
        var body = Map.of("term", "API", "definition", "");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(BASE_URL, body, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }
}
