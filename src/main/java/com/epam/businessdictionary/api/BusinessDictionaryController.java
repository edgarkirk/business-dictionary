package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
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

@Tag(name = "Business Dictionary", description = "Operations for managing business terms and definitions")
@RestController
@RequestMapping("/api/v1/dictionary")
@Validated
public class BusinessDictionaryController {

    private final BusinessDictionaryService service;

    public BusinessDictionaryController(BusinessDictionaryService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new business term")
    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    public TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        return toResponse(service.create(request.term(), request.definition()));
    }

    @Operation(summary = "Retrieve a business term by name")
    @GetMapping("/terms/{term}")
    public TermResponse getByTerm(@PathVariable String term) {
        return toResponse(service.findByTerm(term));
    }

    @Operation(summary = "Update the definition of an existing business term")
    @PutMapping("/terms/{term}")
    public TermResponse updateTerm(@PathVariable String term,
                                   @Valid @RequestBody UpdateTermRequest request) {
        return toResponse(service.updateDefinition(term, request.definition()));
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
