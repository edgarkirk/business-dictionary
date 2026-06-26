package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.TermAlreadyExistsException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import com.epam.businessdictionary.persistence.BusinessDictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

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
        String normalized = term.toLowerCase(Locale.ROOT);
        if (repository.existsByNormalizedTerm(normalized)) {
            throw new TermAlreadyExistsException(term);
        }
        return repository.save(new BusinessDictionaryEntry(term, definition));
    }

    @Override
    public BusinessDictionaryEntry getTerm(String term) {
        String normalized = term.toLowerCase(Locale.ROOT);
        return repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public BusinessDictionaryEntry updateDefinition(String term, String definition) {
        String normalized = term.toLowerCase(Locale.ROOT);
        BusinessDictionaryEntry entry = repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> new TermNotFoundException(term));
        entry.updateDefinition(definition);
        return repository.save(entry);
    }
}
