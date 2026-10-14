package com.vasav.ecom.searchservice.controller;

import com.vasav.ecom.searchservice.dto.SearchFilter;
import com.vasav.ecom.searchservice.dto.SearchResult;
import com.vasav.ecom.searchservice.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<PagedModel<SearchResult>> search(@Valid @ModelAttribute SearchFilter filter, Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(searchService.search(filter, pageable)));
    }
}
