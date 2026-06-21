package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.domain.DictionaryEntry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {

    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @PostMapping("/terms")
    public ResponseEntity<DictionaryTermResponse> createTerm(@Valid @RequestBody CreateTermRequest request) {
        DictionaryEntry entry = service.createTerm(request.term(), request.definition());
        return ResponseEntity.status(HttpStatus.CREATED).body(DictionaryTermResponse.from(entry));
    }

    @GetMapping("/terms/{term}")
    public ResponseEntity<DictionaryTermResponse> getTerm(@PathVariable String term) {
        DictionaryEntry entry = service.getTerm(term);
        return ResponseEntity.ok(DictionaryTermResponse.from(entry));
    }

    @PutMapping("/terms/{term}")
    public ResponseEntity<DictionaryTermResponse> updateDefinition(
            @PathVariable String term,
            @Valid @RequestBody UpdateDefinitionRequest request) {
        DictionaryEntry entry = service.updateDefinition(term, request.definition());
        return ResponseEntity.ok(DictionaryTermResponse.from(entry));
    }
}
