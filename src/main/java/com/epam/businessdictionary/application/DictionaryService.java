package com.epam.businessdictionary.application;

import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.infrastructure.DictionaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@Transactional
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);

    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    public DictionaryEntry createTerm(String term, String definition) {
        log.info("Creating term: {}", term);
        String normalized = normalize(term);
        if (repository.existsByNormalizedTerm(normalized)) {
            log.warn("Duplicate term rejected: {}", term);
            throw new DuplicateTermException(term);
        }
        Instant now = Instant.now();
        DictionaryEntry entry = new DictionaryEntry(term, normalized, definition, now, now);
        DictionaryEntry saved = repository.save(entry);
        log.info("Term created: {}", term);
        return saved;
    }

    @Transactional(readOnly = true)
    public DictionaryEntry getTerm(String term) {
        String normalized = normalize(term);
        return repository.findByNormalizedTerm(normalized).map(entry -> {
            log.info("Term found: {}", term);
            return entry;
        }).orElseThrow(() -> {
            log.warn("Term not found: {}", term);
            return new TermNotFoundException(term);
        });
    }

    public DictionaryEntry updateDefinition(String term, String definition) {
        String normalized = normalize(term);
        DictionaryEntry entry = repository.findByNormalizedTerm(normalized).orElseThrow(() -> {
            log.warn("Term not found for update: {}", term);
            return new TermNotFoundException(term);
        });
        entry.setDefinition(definition);
        entry.setUpdatedAt(Instant.now());
        DictionaryEntry saved = repository.save(entry);
        log.info("Definition updated for term: {}", term);
        return saved;
    }

    private String normalize(String term) {
        return term.toLowerCase(Locale.ROOT).trim();
    }
}
