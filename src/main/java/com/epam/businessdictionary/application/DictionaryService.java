package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.BusinessTerm;

public interface DictionaryService {

    BusinessTerm create(String term, String definition);

    BusinessTerm getByTerm(String term);

    BusinessTerm updateDefinition(String term, String newDefinition);
}
