package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.*;
import com.agriassist.service.ContentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContentController {
    private final ContentService service;
    public ContentController(ContentService service) { this.service = service; }

    @PostMapping("/articles")
    public Result<Article> addArticle(@RequestBody Article a) { return Result.success(service.addArticle(a)); }

    @GetMapping("/articles")
    public Result<List<Article>> articles(@RequestParam(required = false) String category) {
        return Result.success(service.listArticles(category));
    }

    @PostMapping("/community/posts")
    public Result<CommunityPost> addPost(@RequestBody CommunityPost p) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.addPost(p, uid));
    }

    @GetMapping("/community/posts")
    public Result<List<CommunityPost>> posts() { return Result.success(service.listPosts()); }

    @GetMapping("/community/posts/{pid}/comments")
    public Result<List<Comment>> comments(@PathVariable Long pid) {
        return Result.success(service.listComments(pid));
    }

    @PostMapping("/community/posts/{pid}/comments")
    public Result<Comment> comment(@PathVariable Long pid, @RequestParam String content) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.addComment(pid, uid, content));
    }

    @PostMapping("/community/posts/{pid}/adopt/{cid}")
    public Result<Void> adopt(@PathVariable Long pid, @PathVariable Long cid) {
        service.adoptComment(pid, cid); return Result.success();
    }
}
