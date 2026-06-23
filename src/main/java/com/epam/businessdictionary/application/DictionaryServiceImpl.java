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
        log.info("Creating term: {}", request.getTerm());

        String normalizedTerm = normalize(request.getTerm());

        if (repository.findByNormalizedTerm(normalizedTerm).isPresent()) {
            log.warn("Duplicate term rejected: {}", request.getTerm());
            throw new DuplicateTermException(request.getTerm());
        }

        DictionaryEntry entry = new DictionaryEntry(request.getTerm(), normalizedTerm, request.getDefinition());
        DictionaryEntry saved = repository.save(entry);

        log.info("Term created: {}", saved.getTerm());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryTermResponse getTerm(String term) {
        String normalizedTerm = normalize(term);

        return repository.findByNormalizedTerm(normalizedTerm)
                .map(entry -> {
                    log.info("Term lookup succeeded: {}", term);
                    return toResponse(entry);
                })
                .orElseThrow(() -> {
                    log.warn("Term not found: {}", term);
                    return new TermNotFoundException(term);
                });
    }

    @Override
    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        String normalizedTerm = normalize(term);

        DictionaryEntry entry = repository.findByNormalizedTerm(normalizedTerm)
                .orElseThrow(() -> {
                    log.warn("Term not found for update: {}", term);
                    return new TermNotFoundException(term);
                });

        entry.setDefinition(request.getDefinition());
        DictionaryEntry saved = repository.save(entry);

        log.info("Definition updated for term: {}", term);
        return toResponse(saved);
    }

    private String normalize(String term) {
        return term.toLowerCase();
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
