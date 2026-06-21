package com.epam.businessdictionary.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTermRequest(
        @NotBlank @Size(max = 100) String term,
        @NotBlank @Size(max = 1000) String definition
) {}
