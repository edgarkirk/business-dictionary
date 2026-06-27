package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;

public interface BusinessDictionaryService {

    BusinessDictionaryEntry create(String term, String definition);

    BusinessDictionaryEntry findByTerm(String term);

    BusinessDictionaryEntry updateDefinition(String term, String definition);
}
