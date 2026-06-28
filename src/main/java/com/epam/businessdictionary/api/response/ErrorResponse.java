package com.epam.businessdictionary.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Error code identifying the error category") String code,
        @Schema(description = "Human-readable error message") String message) {
}
