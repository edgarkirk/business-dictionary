package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.BusinessDictionaryTerm;

public interface BusinessDictionaryService {

    BusinessDictionaryTerm createTerm(String term, String definition);

    BusinessDictionaryTerm getTerm(String term);

    BusinessDictionaryTerm updateTerm(String term, String definition);
}
