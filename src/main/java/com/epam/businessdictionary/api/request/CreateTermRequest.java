package com.epam.businessdictionary.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTermRequest(
        @NotBlank(message = "Term must not be blank")
        @Size(max = 100, message = "Term must not exceed 100 characters")
        String term,

        @NotBlank(message = "Definition must not be blank")
        @Size(max = 1000, message = "Definition must not exceed 1000 characters")
        String definition) {
}
