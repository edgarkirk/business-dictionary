package com.epam.businessdictionary.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessDictionaryEntryTest {

    @Test
    void of_sets_term_and_definition() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("CRM", "Customer Relationship Management");

        assertThat(entry.getTerm()).isEqualTo("CRM");
        assertThat(entry.getDefinition()).isEqualTo("Customer Relationship Management");
    }

    @Test
    void of_sets_normalized_term_to_lowercase_of_term() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("CRM", "Customer Relationship Management");

        assertThat(entry.getNormalizedTerm()).isEqualTo("crm");
    }

    @Test
    void of_preserves_original_casing_for_term() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("MixedCASE", "some definition");

        assertThat(entry.getTerm()).isEqualTo("MixedCASE");
        assertThat(entry.getNormalizedTerm()).isEqualTo("mixedcase");
    }

    @Test
    void of_assigns_non_null_id() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("ROI", "Return on Investment");

        assertThat(entry.getId()).isNotNull();
    }

    @Test
    void update_definition_changes_definition() {
        BusinessDictionaryEntry entry = BusinessDictionaryEntry.of("KPI", "Key Performance Indicator");

        entry.updateDefinition("Key Performance Index (updated)");

        assertThat(entry.getDefinition()).isEqualTo("Key Performance Index (updated)");
    }
}
