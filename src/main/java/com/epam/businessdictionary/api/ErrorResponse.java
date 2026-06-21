package com.epam.businessdictionary.api;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> details
) {

    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }

    public record FieldError(String field, String message) {}
}
