package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryServiceImpl.class);

    private final DictionaryRepository repository;

    public DictionaryServiceImpl(DictionaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public DictionaryTermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term: {}", request.term());
        String normalized = request.term().toLowerCase();
        if (repository.findByNormalizedTerm(normalized).isPresent()) {
            log.warn("Duplicate term rejected: {}", request.term());
            throw new DuplicateTermException(request.term());
        }
        var entry = new DictionaryEntry(request.term(), normalized, request.definition());
        var saved = repository.save(entry);
        log.info("Term created: {}", saved.getTerm());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryTermResponse readTerm(String term) {
        String normalized = term.toLowerCase();
        var entry = repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> {
                    log.warn("Term not found: {}", term);
                    return new TermNotFoundException(term);
                });
        log.info("Term found: {}", entry.getTerm());
        return toResponse(entry);
    }

    @Override
    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        String normalized = term.toLowerCase();
        var entry = repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> {
                    log.warn("Term not found for update: {}", term);
                    return new TermNotFoundException(term);
                });
        entry.setDefinition(request.definition());
        var saved = repository.save(entry);
        log.info("Definition updated for term: {}", saved.getTerm());
        return toResponse(saved);
    }

    private DictionaryTermResponse toResponse(DictionaryEntry entry) {
        return new DictionaryTermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
