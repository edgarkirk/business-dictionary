package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DictionaryServiceImpl implements DictionaryService {

    private final BusinessDictionaryRepository repository;

    DictionaryServiceImpl(BusinessDictionaryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry createTerm(String term, String definition) {
        String normalized = term.toLowerCase();
        try {
            return repository.saveAndFlush(new BusinessDictionaryEntry(term, normalized, definition));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTermException(term);
        }
    }

    @Override
    public BusinessDictionaryEntry getTerm(String term) {
        String normalized = term.toLowerCase();
        return repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry updateTerm(String term, String newDefinition) {
        String normalized = term.toLowerCase();
        BusinessDictionaryEntry entry = repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> new TermNotFoundException(term));
        entry.updateDefinition(newDefinition);
        return repository.saveAndFlush(entry);
    }
}
