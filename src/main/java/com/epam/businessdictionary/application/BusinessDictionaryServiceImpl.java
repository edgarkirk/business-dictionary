package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class BusinessDictionaryServiceImpl implements BusinessDictionaryService {

    private final BusinessDictionaryRepository repository;

    BusinessDictionaryServiceImpl(BusinessDictionaryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry create(String term, String definition) {
        String normalizedTerm = term.toLowerCase();
        if (repository.existsByNormalizedTerm(normalizedTerm)) {
            throw new DuplicateTermException(term);
        }
        return repository.save(BusinessDictionaryEntry.of(term, definition));
    }

    @Override
    public BusinessDictionaryEntry findByTerm(String term) {
        return repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry updateDefinition(String term, String definition) {
        BusinessDictionaryEntry entry = repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
        entry.updateDefinition(definition);
        return repository.save(entry);
    }
}
