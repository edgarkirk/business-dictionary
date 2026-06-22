package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.CreateTermRequest;
import com.epam.businessdictionary.api.DictionaryTermResponse;
import com.epam.businessdictionary.api.UpdateDefinitionRequest;

public interface DictionaryService {

    DictionaryTermResponse createTerm(CreateTermRequest request);

    DictionaryTermResponse getTerm(String term);

    DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request);
}
