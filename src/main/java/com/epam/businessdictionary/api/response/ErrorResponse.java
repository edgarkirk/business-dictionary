package com.epam.businessdictionary.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Error code") String code,
        @Schema(description = "Error message") String message
) {}
