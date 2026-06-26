package com.epam.businessdictionary.application.exception;

public class TermAlreadyExistsException extends RuntimeException {

    public TermAlreadyExistsException(String term) {
        super("Term already exists: " + term);
    }
}
