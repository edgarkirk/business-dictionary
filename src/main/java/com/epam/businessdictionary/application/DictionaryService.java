package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;

public interface DictionaryService {

    TermResponse createTerm(CreateTermRequest request);

    TermResponse getTerm(String term);

    TermResponse updateTerm(String term, UpdateTermRequest request);
}
