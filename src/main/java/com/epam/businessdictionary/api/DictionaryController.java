package com.epam.businessdictionary.api;

import com.epam.businessdictionary.api.request.CreateTermRequest;
import com.epam.businessdictionary.api.request.UpdateTermRequest;
import com.epam.businessdictionary.api.response.TermResponse;
import com.epam.businessdictionary.application.DictionaryService;
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
@RequestMapping("/api/v1/dictionary/terms")
@Validated
@Tag(name = "Dictionary", description = "Business dictionary management")
class DictionaryController {

    private final DictionaryService dictionaryService;

    DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    @Operation(summary = "Create a new business term")
    ResponseEntity<TermResponse> createTerm(@Valid @RequestBody CreateTermRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TermResponse.from(dictionaryService.createTerm(request.term(), request.definition())));
    }

    @GetMapping("/{term}")
    @Operation(summary = "Retrieve a business term by name")
    ResponseEntity<TermResponse> getTerm(@PathVariable String term) {
        return ResponseEntity.ok(TermResponse.from(dictionaryService.getTerm(term)));
    }

    @PutMapping("/{term}")
    @Operation(summary = "Update the definition of an existing term")
    ResponseEntity<TermResponse> updateTerm(@PathVariable String term,
                                            @Valid @RequestBody UpdateTermRequest request) {
        return ResponseEntity.ok(TermResponse.from(dictionaryService.updateTerm(term, request.definition())));
    }
}
