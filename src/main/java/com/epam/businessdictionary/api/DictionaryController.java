package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.domain.BusinessDictionaryEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dictionary")
@Validated
@Tag(name = "Dictionary", description = "Business Dictionary API for managing corporate terminology")
class DictionaryController {

    private final DictionaryService dictionaryService;

    DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping("/terms")
    @Operation(summary = "Create a new business term")
    ResponseEntity<TermResponse> createTerm(@Valid @RequestBody CreateTermRequest request) {
        BusinessDictionaryEntry entry = dictionaryService.createTerm(request.term(), request.definition());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
    }

    @GetMapping("/terms/{term}")
    @Operation(summary = "Retrieve a business term by name (case-insensitive)")
    ResponseEntity<TermResponse> getTerm(@PathVariable String term) {
        BusinessDictionaryEntry entry = dictionaryService.getTerm(term);
        return ResponseEntity.ok(toResponse(entry));
    }

    @PutMapping("/terms/{term}")
    @Operation(summary = "Update the definition of an existing business term")
    ResponseEntity<TermResponse> updateDefinition(
            @PathVariable String term,
            @Valid @RequestBody UpdateDefinitionRequest request) {
        BusinessDictionaryEntry entry = dictionaryService.updateDefinition(term, request.definition());
        return ResponseEntity.ok(toResponse(entry));
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
