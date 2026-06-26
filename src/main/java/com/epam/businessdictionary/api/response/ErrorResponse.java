package com.epam.businessdictionary.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Error code identifying the type of failure")
        String code,

        @Schema(description = "Human-readable description of the error")
        String message) {
}
