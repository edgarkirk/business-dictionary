package com.epam.businessdictionary.api.response;

public record ErrorResponse(String error, String message, int status) {}
