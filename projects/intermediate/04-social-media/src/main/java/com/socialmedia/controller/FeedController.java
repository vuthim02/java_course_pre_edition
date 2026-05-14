package com.socialmedia.controller;

import com.socialmedia.model.Post;
import com.socialmedia.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<Page<Post>> getFeed(@RequestAttribute Long userId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getNewsFeed(userId, page, size));
    }
}
