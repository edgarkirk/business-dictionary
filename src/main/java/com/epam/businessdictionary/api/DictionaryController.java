package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.BusinessDictionaryService;
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
@Tag(name = "Dictionary", description = "Business Dictionary API")
@Validated
class DictionaryController {

    private final BusinessDictionaryService service;

    DictionaryController(BusinessDictionaryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new business term")
    TermResponse createTerm(@Valid @RequestBody CreateTermRequest request) {
        return TermResponse.from(service.createTerm(request.term(), request.definition()));
    }

    @GetMapping("/{term}")
    @Operation(summary = "Retrieve a business term by name")
    TermResponse getTerm(@PathVariable String term) {
        return TermResponse.from(service.getTerm(term));
    }

    @PutMapping("/{term}")
    @Operation(summary = "Update the definition of a business term")
    TermResponse updateTerm(@PathVariable String term, @Valid @RequestBody UpdateTermRequest request) {
        return TermResponse.from(service.updateTerm(term, request.definition()));
    }
}
