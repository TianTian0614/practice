package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService service;
    public SearchController(SearchService service) { this.service = service; }

    @GetMapping
    public Result<Map<String, Object>> search(@RequestParam String q) {
        return Result.success(service.search(q));
    }
}
