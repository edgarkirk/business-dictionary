package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;

public interface DictionaryService {

    DictionaryTermResponse createTerm(CreateTermRequest request);

    DictionaryTermResponse getTerm(String term);

    DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request);
}
