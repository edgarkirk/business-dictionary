package com.epam.businessdictionary.api;

import com.epam.businessdictionary.application.DictionaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {

    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    public DictionaryTermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        return service.createTerm(request);
    }

    @GetMapping("/terms/{term}")
    public DictionaryTermResponse getByTerm(@PathVariable String term) {
        return service.getByTerm(term);
    }

    @PutMapping("/terms/{term}")
    public DictionaryTermResponse updateDefinition(
            @PathVariable String term,
            @Valid @RequestBody UpdateDefinitionRequest request) {
        return service.updateDefinition(term, request);
    }
}
