package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
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
    public TermResponse createTerm(CreateTermRequest request) {
        String normalizedTerm = request.term().toLowerCase();
        try {
            BusinessDictionaryEntry entry = new BusinessDictionaryEntry(
                    request.term(), normalizedTerm, request.definition());
            return toResponse(repository.saveAndFlush(entry));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTermException(request.term());
        }
    }

    @Override
    public TermResponse getTerm(String term) {
        return repository.findByNormalizedTerm(term.toLowerCase())
                .map(this::toResponse)
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    @Transactional
    public TermResponse updateTerm(String term, UpdateTermRequest request) {
        BusinessDictionaryEntry entry = repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
        entry.updateDefinition(request.definition());
        return toResponse(repository.saveAndFlush(entry));
    }

    private TermResponse toResponse(BusinessDictionaryEntry entry) {
        return new TermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
