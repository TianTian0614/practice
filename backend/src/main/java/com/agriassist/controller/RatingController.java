package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.Rating;
import com.agriassist.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @PostMapping
    public Result<Rating> create(@RequestBody Rating rating) {
        return Result.success(service.create(rating, currentUid()));
    }

    @GetMapping("/user/{toUserId}")
    public Result<Page<Rating>> list(@PathVariable Long toUserId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.listByUser(toUserId, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}")
    public Result<Rating> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer score = body.get("score") != null ? ((Number) body.get("score")).intValue() : null;
        String comment = (String) body.get("comment");
        return Result.success(service.update(id, score, comment, currentUid()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id, currentUid());
        return Result.success();
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
