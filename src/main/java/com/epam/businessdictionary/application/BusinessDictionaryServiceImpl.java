package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryTerm;
import com.epam.businessdictionary.persistence.BusinessDictionaryTermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class BusinessDictionaryServiceImpl implements BusinessDictionaryService {

    private final BusinessDictionaryTermRepository repository;

    BusinessDictionaryServiceImpl(BusinessDictionaryTermRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusinessDictionaryTerm createTerm(String term, String definition) {
        String normalized = term.toLowerCase();
        if (repository.findByNormalizedTerm(normalized).isPresent()) {
            throw new DuplicateTermException(term);
        }
        return repository.save(new BusinessDictionaryTerm(term, definition));
    }

    @Override
    public BusinessDictionaryTerm getTerm(String term) {
        return repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public BusinessDictionaryTerm updateTerm(String term, String definition) {
        BusinessDictionaryTerm entity = repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
        entity.updateDefinition(definition);
        return repository.save(entity);
    }
}
