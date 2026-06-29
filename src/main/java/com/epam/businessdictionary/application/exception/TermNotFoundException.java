package com.epam.businessdictionary.application.exception;

public class TermNotFoundException extends RuntimeException {

    public TermNotFoundException(String term) {
        super("Term not found: " + term);
    }
}
