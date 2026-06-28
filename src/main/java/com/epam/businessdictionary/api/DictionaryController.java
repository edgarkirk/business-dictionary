package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.DictionaryService;
import com.epam.businessdictionary.domain.BusinessTerm;
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
@RequestMapping("/api/v1/dictionary")
@Validated
@Tag(name = "Business Dictionary", description = "Business term management API")
class DictionaryController {

    private final DictionaryService dictionaryService;

    DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping("/terms")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new business term")
    public TermResponse createTerm(@RequestBody @Valid CreateTermRequest request) {
        BusinessTerm term = dictionaryService.create(request.term(), request.definition());
        return toResponse(term);
    }

    @GetMapping("/terms/{term}")
    @Operation(summary = "Get a business term by name")
    public TermResponse getTerm(@PathVariable String term) {
        return toResponse(dictionaryService.getByTerm(term));
    }

    @PutMapping("/terms/{term}")
    @Operation(summary = "Update the definition of a business term")
    public TermResponse updateTerm(@PathVariable String term,
                                   @RequestBody @Valid UpdateTermRequest request) {
        return toResponse(dictionaryService.updateDefinition(term, request.definition()));
    }

    private TermResponse toResponse(BusinessTerm term) {
        return new TermResponse(
                term.getId(),
                term.getTerm(),
                term.getDefinition(),
                term.getCreatedAt(),
                term.getUpdatedAt());
    }
}
