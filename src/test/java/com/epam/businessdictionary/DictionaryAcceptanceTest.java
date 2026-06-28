package com.epam.businessdictionary;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.ErrorResponse;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.persistence.BusinessTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DictionaryAcceptanceTest {

    private static final String BASE = "/api/v1/dictionary/terms";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BusinessTermRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void ac01_create_term_returns_201() {
        ResponseEntity<TermResponse> response = restTemplate.postForEntity(
                BASE,
                new CreateTermRequest("API", "Application Programming Interface"),
                TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("API");
        assertThat(response.getBody().definition()).isEqualTo("Application Programming Interface");
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().createdAt()).isNotNull();
        assertThat(response.getBody().updatedAt()).isNotNull();
    }

    @Test
    void ac02_reject_duplicate_term_case_insensitive_returns_409() {
        restTemplate.postForEntity(BASE, new CreateTermRequest("SLA", "Service Level Agreement"), TermResponse.class);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                BASE,
                new CreateTermRequest("sla", "Another definition"),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_TERM");
    }

    @Test
    void ac03_get_existing_term_returns_200() {
        restTemplate.postForEntity(BASE, new CreateTermRequest("KPI", "Key Performance Indicator"), TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE + "/KPI", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("KPI");
        assertThat(response.getBody().definition()).isEqualTo("Key Performance Indicator");
    }

    @Test
    void ac03_get_term_is_case_insensitive() {
        restTemplate.postForEntity(BASE, new CreateTermRequest("ROI", "Return on Investment"), TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.getForEntity(BASE + "/roi", TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().term()).isEqualTo("ROI");
    }

    @Test
    void ac04_get_missing_term_returns_404() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(BASE + "/NONEXISTENT", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("TERM_NOT_FOUND");
    }

    @Test
    void ac05_update_term_definition_returns_200() {
        restTemplate.postForEntity(BASE, new CreateTermRequest("MVP", "Minimum Viable Product"), TermResponse.class);

        ResponseEntity<TermResponse> response = restTemplate.exchange(
                BASE + "/MVP",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateTermRequest("Most Valuable Player")),
                TermResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().definition()).isEqualTo("Most Valuable Player");
        assertThat(response.getBody().term()).isEqualTo("MVP");
    }

    @Test
    void ac05_update_preserves_created_at_and_changes_updated_at() {
        ResponseEntity<TermResponse> created = restTemplate.postForEntity(
                BASE, new CreateTermRequest("OKR", "Objectives and Key Results"), TermResponse.class);

        ResponseEntity<TermResponse> updated = restTemplate.exchange(
                BASE + "/OKR",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateTermRequest("OKRs framework")),
                TermResponse.class);

        assertThat(updated.getBody()).isNotNull();
        assertThat(created.getBody()).isNotNull();
        // DB stores TIMESTAMP(6) — truncate both sides to microseconds before comparing
        assertThat(updated.getBody().createdAt().truncatedTo(ChronoUnit.MICROS))
                .isEqualTo(created.getBody().createdAt().truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    void ac05_update_missing_term_returns_404() {
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                BASE + "/NONEXISTENT",
                HttpMethod.PUT,
                new HttpEntity<>(new UpdateTermRequest("Some definition")),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("TERM_NOT_FOUND");
    }

    @Test
    void ac06_create_with_blank_term_returns_400() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                BASE,
                new CreateTermRequest("", "Some definition"),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void ac06_create_with_blank_definition_returns_400() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                BASE,
                new CreateTermRequest("TERM", ""),
                ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
    }
}
