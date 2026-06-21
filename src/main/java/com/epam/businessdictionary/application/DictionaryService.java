package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.CreateTermRequest;
import com.epam.businessdictionary.api.DictionaryTermResponse;
import com.epam.businessdictionary.api.UpdateDefinitionRequest;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);

    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    public DictionaryTermResponse createTerm(CreateTermRequest request) {
        log.info("Term creation started: {}", request.term());
        String normalizedTerm = normalize(request.term());
        if (repository.findByNormalizedTerm(normalizedTerm).isPresent()) {
            log.warn("Duplicate term rejected: {}", request.term());
            throw new DuplicateTermException(request.term());
        }
        OffsetDateTime now = OffsetDateTime.now();
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(request.term());
        entry.setNormalizedTerm(normalizedTerm);
        entry.setDefinition(request.definition());
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        DictionaryEntry saved = repository.save(entry);
        log.info("Term created: {}", saved.getTerm());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DictionaryTermResponse getByTerm(String term) {
        String normalizedTerm = normalize(term);
        return repository.findByNormalizedTerm(normalizedTerm)
                .map(entry -> {
                    log.info("Term lookup succeeded: {}", term);
                    return toResponse(entry);
                })
                .orElseThrow(() -> {
                    log.warn("Requested term not found: {}", term);
                    return new TermNotFoundException(term);
                });
    }

    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        String normalizedTerm = normalize(term);
        DictionaryEntry entry = repository.findByNormalizedTerm(normalizedTerm)
                .orElseThrow(() -> {
                    log.warn("Requested term not found for update: {}", term);
                    return new TermNotFoundException(term);
                });
        entry.setDefinition(request.definition());
        entry.setUpdatedAt(OffsetDateTime.now());
        DictionaryEntry saved = repository.save(entry);
        log.info("Definition updated for term: {}", term);
        return toResponse(saved);
    }

    private String normalize(String term) {
        return term.trim().toLowerCase();
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
