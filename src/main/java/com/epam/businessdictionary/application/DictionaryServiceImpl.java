package com.epam.businessdictionary.application;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
import com.epam.businessdictionary.application.exception.DuplicateTermException;
import com.epam.businessdictionary.application.exception.TermNotFoundException;
import com.epam.businessdictionary.domain.DictionaryEntry;
import com.epam.businessdictionary.persistence.DictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryRepository repository;

    public DictionaryServiceImpl(DictionaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public DictionaryTermResponse createTerm(CreateTermRequest request) {
        String normalized = request.getTerm().toLowerCase();
        if (repository.findByNormalizedTerm(normalized).isPresent()) {
            throw new DuplicateTermException(request.getTerm());
        }
        DictionaryEntry entry = new DictionaryEntry();
        entry.setTerm(request.getTerm());
        entry.setNormalizedTerm(normalized);
        entry.setDefinition(request.getDefinition());
        return toResponse(repository.save(entry));
    }

    @Override
    @Transactional(readOnly = true)
    public DictionaryTermResponse getTerm(String term) {
        return repository.findByNormalizedTerm(term.toLowerCase())
                .map(this::toResponse)
                .orElseThrow(() -> new TermNotFoundException(term));
    }

    @Override
    public DictionaryTermResponse updateDefinition(String term, UpdateDefinitionRequest request) {
        DictionaryEntry entry = repository.findByNormalizedTerm(term.toLowerCase())
                .orElseThrow(() -> new TermNotFoundException(term));
        entry.setDefinition(request.getDefinition());
        return toResponse(repository.save(entry));
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
