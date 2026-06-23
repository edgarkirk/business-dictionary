package com.epam.businessdictionary.api.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> details
) {

    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse validationError(List<FieldError> details) {
        return new ErrorResponse("VALIDATION_ERROR", "Request validation failed", details);
    }
}
