package com.epam.businessdictionary.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateDefinitionRequest {

    @NotBlank
    @Size(max = 1000)
    private String definition;

    public String getDefinition() { return definition; }

    public void setDefinition(String definition) { this.definition = definition; }
}
