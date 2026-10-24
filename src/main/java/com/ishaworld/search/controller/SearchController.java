package com.ishaworld.search.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.search.dto.SearchResultResponse;
import com.ishaworld.search.service.SearchService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    ApiResponse<List<SearchResultResponse>> search(@RequestParam(name = "q", required = false) String query) {
        return ApiResponse.success(searchService.search(query));
    }
}

