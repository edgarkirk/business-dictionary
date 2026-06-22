package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
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

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping("/terms")
    public ResponseEntity<DictionaryTermResponse> createTerm(@Valid @RequestBody CreateTermRequest request) {
        DictionaryTermResponse response = dictionaryService.createTerm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/terms/{term}")
    public ResponseEntity<DictionaryTermResponse> getTerm(@PathVariable String term) {
        DictionaryTermResponse response = dictionaryService.getTerm(term);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/terms/{term}")
    public ResponseEntity<DictionaryTermResponse> updateDefinition(
            @PathVariable String term,
            @Valid @RequestBody UpdateDefinitionRequest request) {
        DictionaryTermResponse response = dictionaryService.updateDefinition(term, request);
        return ResponseEntity.ok(response);
    }
}
