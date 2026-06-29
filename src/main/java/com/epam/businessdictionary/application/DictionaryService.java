package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;

public interface DictionaryService {

    BusinessDictionaryEntry createTerm(String term, String definition);

    BusinessDictionaryEntry getTerm(String term);

    BusinessDictionaryEntry updateTerm(String term, String newDefinition);
}
