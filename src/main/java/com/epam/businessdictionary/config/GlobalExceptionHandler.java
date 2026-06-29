package com.epam.businessdictionary.config;

import com.epam.businessdictionary.api.response.ErrorResponse;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TermNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleTermNotFound(TermNotFoundException ex) {
        return new ErrorResponse("Not Found", ex.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(DuplicateTermException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleDuplicateTerm(DuplicateTermException ex) {
        return new ErrorResponse("Conflict", ex.getMessage(), HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidationFailure(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return new ErrorResponse("Bad Request", message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleUnexpected(Exception ex) {
        return new ErrorResponse("Internal Server Error", ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
