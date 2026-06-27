package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/dictionary/terms")
@Tag(name = "Dictionary", description = "Business dictionary term management")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new business term")
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        BusinessDictionaryEntry entry = dictionaryService.create(request.term(), request.definition());
        return toResponse(entry);
    }

    @GetMapping("/{term}")
    @Operation(summary = "Retrieve a business term by name")
    public TermResponse getTerm(@PathVariable String term) {
        BusinessDictionaryEntry entry = dictionaryService.findByTerm(term);
        return toResponse(entry);
    }

    @PutMapping("/{term}")
    @Operation(summary = "Update the definition of an existing business term")
    public TermResponse updateTerm(@PathVariable String term,
                                   @Valid @RequestBody UpdateTermRequest request) {
        BusinessDictionaryEntry entry = dictionaryService.update(term, request.definition());
        return toResponse(entry);
    }

    private TermResponse toResponse(BusinessDictionaryEntry entry) {
        return new TermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
