package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateDefinitionRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.BusinessDictionaryService;
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
@RequestMapping("/api/v1/dictionary/terms")
@Validated
@Tag(name = "Business Dictionary", description = "Manage business dictionary terms")
class TermController {

    private final BusinessDictionaryService service;

    TermController(BusinessDictionaryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new dictionary term")
    TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        return toResponse(service.createTerm(request.term(), request.definition()));
    }

    @GetMapping("/{term}")
    @Operation(summary = "Retrieve a term by name (case-insensitive)")
    TermResponse getTerm(@PathVariable String term) {
        return toResponse(service.getTerm(term));
    }

    @PutMapping("/{term}")
    @Operation(summary = "Update the definition of an existing term")
    TermResponse updateTerm(@PathVariable String term, @Valid @RequestBody UpdateDefinitionRequest request) {
        return toResponse(service.updateTerm(term, request.definition()));
    }

    private TermResponse toResponse(BusinessDictionaryEntry entry) {
        return new TermResponse(
                entry.getId(),
                entry.getTerm(),
                entry.getDefinition(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
