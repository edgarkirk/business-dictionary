package com.epam.businessdictionary.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessDictionaryTermTest {

    @Test
    void constructor_sets_term() {
        var entity = new BusinessDictionaryTerm("API Gateway", "A server-side API management solution");

        assertThat(entity.getTerm()).isEqualTo("API Gateway");
    }

    @Test
    void constructor_normalizes_term_to_lowercase() {
        var entity = new BusinessDictionaryTerm("API Gateway", "A server-side API management solution");

        assertThat(entity.getNormalizedTerm()).isEqualTo("api gateway");
    }

    @Test
    void constructor_sets_definition() {
        var entity = new BusinessDictionaryTerm("API Gateway", "A server-side API management solution");

        assertThat(entity.getDefinition()).isEqualTo("A server-side API management solution");
    }

    @Test
    void id_is_null_before_persistence() {
        var entity = new BusinessDictionaryTerm("API Gateway", "A server-side API management solution");

        assertThat(entity.getId()).isNull();
    }

    @Test
    void update_definition_changes_definition() {
        var entity = new BusinessDictionaryTerm("API Gateway", "Old definition");

        entity.updateDefinition("New definition");

        assertThat(entity.getDefinition()).isEqualTo("New definition");
    }
}
