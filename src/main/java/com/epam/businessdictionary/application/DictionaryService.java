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

import java.time.Instant;
import java.util.UUID;

@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);

    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DictionaryTermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term: {}", request.term());

        String normalized = normalize(request.term());

        if (repository.existsByNormalizedTerm(normalized)) {
            log.warn("Duplicate term rejected: {}", request.term());
            throw new DuplicateTermException(request.term());
        }

        Instant now = Instant.now();
        DictionaryEntry entry = new DictionaryEntry();
        entry.setId(UUID.randomUUID());
        entry.setTerm(request.term());
        entry.setNormalizedTerm(normalized);
        entry.setDefinition(request.definition());
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);

        DictionaryEntry saved = repository.save(entry);
        log.info("Term created: {}", saved.getTerm());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DictionaryTermResponse getTerm(String term) {
        String normalized = normalize(term);

        return repository.findByNormalizedTerm(normalized)
                .map(entry -> {
                    log.info("Term lookup succeeded: {}", entry.getTerm());
                    return toResponse(entry);
                })
                .orElseThrow(() -> {
                    log.warn("Term not found: {}", term);
                    return new TermNotFoundException(term);
                });
    }

    @Transactional
    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        String normalized = normalize(term);

        DictionaryEntry entry = repository.findByNormalizedTerm(normalized)
                .orElseThrow(() -> {
                    log.warn("Term not found for update: {}", term);
                    return new TermNotFoundException(term);
                });

        entry.setDefinition(request.definition());
        entry.setUpdatedAt(Instant.now());

        DictionaryEntry saved = repository.save(entry);
        log.info("Definition updated for term: {}", saved.getTerm());

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
