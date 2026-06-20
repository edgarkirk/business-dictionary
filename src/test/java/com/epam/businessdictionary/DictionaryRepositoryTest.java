package com.epam.businessdictionary;

import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@Transactional
class DictionaryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private DictionaryRepository repository;

    @Test
    void persistsDictionaryEntry() {
        DictionaryEntry entry = newEntry("Bounded Context", "bounded context", "A domain boundary.");

        DictionaryEntry saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Bounded Context");
        assertThat(saved.getDefinition()).isEqualTo("A domain boundary.");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findsEntryByNormalizedTerm() {
        DictionaryEntry entry = newEntry("Bounded Context", "bounded context", "A domain boundary.");
        repository.save(entry);

        Optional<DictionaryEntry> found = repository.findByNormalizedTerm("bounded context");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("Bounded Context");
    }

    @Test
    void enforcesUniqueNormalizedTerm() {
        DictionaryEntry first = newEntry("Bounded Context", "bounded context", "First definition.");
        DictionaryEntry duplicate = newEntry("BOUNDED CONTEXT", "bounded context", "Second definition.");
        repository.save(first);

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            repository.flush();
        }).isInstanceOf(Exception.class);
    }

    private DictionaryEntry newEntry(String term, String normalizedTerm, String definition) {
        OffsetDateTime now = OffsetDateTime.now();
        return new DictionaryEntry(UUID.randomUUID(), term, normalizedTerm, definition, now, now);
    }
}
