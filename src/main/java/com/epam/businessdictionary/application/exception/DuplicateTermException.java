package com.epam.businessdictionary.application.exception;

public class DuplicateTermException extends RuntimeException {

    public DuplicateTermException(String message) {
        super(message);
    }
}
