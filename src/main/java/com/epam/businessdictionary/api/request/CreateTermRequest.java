package com.epam.businessdictionary.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTermRequest {

    @NotBlank
    @Size(max = 100)
    private String term;

    @NotBlank
    @Size(max = 1000)
    private String definition;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
