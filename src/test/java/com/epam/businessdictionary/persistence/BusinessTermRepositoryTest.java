package com.epam.businessdictionary.persistence;

import com.epam.businessdictionary.config.JpaAuditingConfig;
import com.epam.businessdictionary.domain.BusinessTerm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaAuditingConfig.class)
class BusinessTermRepositoryTest {

    @Autowired
    private BusinessTermRepository repository;

    @Test
    void saves_and_finds_by_normalized_term() {
        repository.saveAndFlush(new BusinessTerm("API", "Application Programming Interface"));

        Optional<BusinessTerm> found = repository.findByNormalizedTerm("api");

        assertThat(found).isPresent();
        assertThat(found.get().getTerm()).isEqualTo("API");
        assertThat(found.get().getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void find_by_normalized_term_is_case_insensitive_lookup() {
        repository.saveAndFlush(new BusinessTerm("ROI", "Return on Investment"));

        assertThat(repository.findByNormalizedTerm("roi")).isPresent();
    }

    @Test
    void returns_empty_for_non_existent_term() {
        Optional<BusinessTerm> found = repository.findByNormalizedTerm("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void enforces_unique_normalized_term() {
        repository.saveAndFlush(new BusinessTerm("SLA", "Service Level Agreement"));

        assertThatThrownBy(() -> repository.saveAndFlush(new BusinessTerm("sla", "Another definition")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void audit_timestamps_are_set_on_creation() {
        BusinessTerm saved = repository.saveAndFlush(new BusinessTerm("KPI", "Key Performance Indicator"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void created_at_does_not_change_after_update() {
        BusinessTerm saved = repository.saveAndFlush(new BusinessTerm("MVP", "Minimum Viable Product"));
        var originalCreatedAt = saved.getCreatedAt();

        saved.updateDefinition("Most Valuable Player");
        BusinessTerm updated = repository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }
}
