package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DictionaryServiceImpl implements DictionaryService {

    private final BusinessDictionaryRepository repository;

    public DictionaryServiceImpl(BusinessDictionaryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry create(String term, String definition) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public BusinessDictionaryEntry findByTerm(String term) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry updateDefinition(String term, String definition) {
        throw new UnsupportedOperationException("not implemented");
    }
}
