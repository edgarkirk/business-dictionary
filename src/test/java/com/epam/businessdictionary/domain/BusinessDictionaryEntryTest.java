package com.epam.businessdictionary.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessDictionaryEntryTest {

    // --- constructor ---

    @Test
    void constructor_stores_term_and_definition() {
        var entry = new BusinessDictionaryEntry("API", "Application Programming Interface");

        assertThat(entry.getTerm()).isEqualTo("API");
        assertThat(entry.getDefinition()).isEqualTo("Application Programming Interface");
    }

    @Test
    void constructor_normalizes_uppercase_term_to_lowercase() {
        var entry = new BusinessDictionaryEntry("API", "Application Programming Interface");

        assertThat(entry.getNormalizedTerm()).isEqualTo("api");
    }

    @Test
    void constructor_normalizes_mixed_case_term() {
        var entry = new BusinessDictionaryEntry("BusinessTerm", "Some definition");

        assertThat(entry.getNormalizedTerm()).isEqualTo("businessterm");
    }

    // --- updateDefinition ---

    @Test
    void update_definition_replaces_definition() {
        var entry = new BusinessDictionaryEntry("ORM", "Object Relational Mapping");

        entry.updateDefinition("Object Relational Mapping — revised");

        assertThat(entry.getDefinition()).isEqualTo("Object Relational Mapping — revised");
    }
}
