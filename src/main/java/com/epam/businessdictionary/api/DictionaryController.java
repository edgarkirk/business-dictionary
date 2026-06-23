package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.DictionaryTermResponse;
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

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    public DictionaryTermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        return dictionaryService.createTerm(request);
    }

    @GetMapping("/terms/{term}")
    public DictionaryTermResponse readTerm(@PathVariable String term) {
        return dictionaryService.readTerm(term);
    }

    @PutMapping("/terms/{term}")
    public DictionaryTermResponse updateDefinition(
            @PathVariable String term,
            @Valid @RequestBody UpdateDefinitionRequest request) {
        return dictionaryService.updateDefinition(term, request);
    }
}
