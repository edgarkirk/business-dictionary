package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;

public interface DictionaryService {

    /**
     * Creates a new term with the given definition.
     *
     * @throws TermAlreadyExistsException if a term with the same name (case-insensitively) already exists
     */
    BusinessDictionaryEntry create(String term, String definition);

    /**
     * Retrieves an existing term by name (case-insensitive).
     *
     * @throws TermNotFoundException if no term with the given name exists
     */
    BusinessDictionaryEntry findByTerm(String term);

    /**
     * Updates the definition of an existing term (case-insensitive lookup).
     *
     * @throws TermNotFoundException if no term with the given name exists
     */
    BusinessDictionaryEntry updateDefinition(String term, String definition);
}
