package com.socialmedia.controller;

import com.socialmedia.model.*;
import com.socialmedia.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestAttribute Long userId,
                                            @RequestParam String content,
                                            @RequestParam(required = false) String imageUrl) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(userId, content, imageUrl));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Post>> getUserPosts(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getUserPosts(userId, page, size));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestAttribute Long userId) {
        postService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestAttribute Long userId) {
        postService.unlikePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId,
                                               @RequestAttribute Long userId,
                                               @RequestParam String content) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.addComment(postId, userId, content));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @RequestAttribute Long userId) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
