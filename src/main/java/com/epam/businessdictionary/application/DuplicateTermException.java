package com.epam.businessdictionary.application;

public class DuplicateTermException extends RuntimeException {

    public DuplicateTermException(String term) {
        super("Term already exists: " + term);
    }
}
