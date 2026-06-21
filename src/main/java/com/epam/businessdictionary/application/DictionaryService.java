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
@Transactional
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);

    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    public DictionaryTermResponse createTerm(CreateTermRequest request) {
        log.info("Creating term: {}", request.term());

        String normalizedTerm = normalize(request.term());

        if (repository.findByNormalizedTerm(normalizedTerm).isPresent()) {
            log.warn("Duplicate term rejected: {}", request.term());
            throw new DuplicateTermException(request.term());
        }

        Instant now = Instant.now();
        DictionaryEntry entry = new DictionaryEntry(
                UUID.randomUUID(),
                request.term(),
                normalizedTerm,
                request.definition(),
                now,
                now
        );
        DictionaryEntry saved = repository.save(entry);

        log.info("Term created: {}", saved.getTerm());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DictionaryTermResponse getTerm(String term) {
        String normalizedTerm = normalize(term);
        DictionaryEntry entry = repository.findByNormalizedTerm(normalizedTerm)
                .orElseThrow(() -> {
                    log.warn("Term not found: {}", term);
                    return new TermNotFoundException(term);
                });

        log.info("Term found: {}", entry.getTerm());
        return toResponse(entry);
    }

    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        String normalizedTerm = normalize(term);
        DictionaryEntry entry = repository.findByNormalizedTerm(normalizedTerm)
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
