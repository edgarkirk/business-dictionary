package com.epam.businessdictionary.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> details
) {
    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }

    public record FieldError(String field, String message) {
    }
}
