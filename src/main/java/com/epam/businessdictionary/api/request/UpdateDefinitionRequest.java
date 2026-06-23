package com.epam.businessdictionary.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDefinitionRequest(
        @NotBlank
        @Size(max = 1000)
        String definition
) {}
