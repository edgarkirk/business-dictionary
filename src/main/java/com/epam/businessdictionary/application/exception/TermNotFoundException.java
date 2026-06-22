package com.epam.businessdictionary.application.exception;

public class TermNotFoundException extends RuntimeException {

    public TermNotFoundException(String message) {
        super(message);
    }
}
