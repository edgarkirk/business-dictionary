package com.epam.businessdictionary.application;

import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.BusinessTerm;
import com.epam.businessdictionary.persistence.BusinessTermRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DictionaryServiceImpl implements DictionaryService {

    private final BusinessTermRepository repository;

    DictionaryServiceImpl(BusinessTermRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BusinessTerm create(String term, String definition) {
        BusinessTerm entity = new BusinessTerm(term, definition);
        try {
            return repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTermException(term);
        }
    }

    @Override
    public BusinessTerm getByTerm(String term) {
        return repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public BusinessTerm updateDefinition(String term, String newDefinition) {
        BusinessTerm entity = repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
        entity.updateDefinition(newDefinition);
        return repository.saveAndFlush(entity);
    }
}
