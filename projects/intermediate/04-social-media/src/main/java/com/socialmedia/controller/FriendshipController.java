package com.socialmedia.controller;

import com.socialmedia.model.Friendship;
import com.socialmedia.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<Friendship> sendFriendRequest(@RequestAttribute Long userId,
                                                         @RequestParam Long addresseeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendFriendRequest(userId, addresseeId));
    }

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<Friendship> acceptFriendRequest(@PathVariable Long friendshipId,
                                                           @RequestAttribute Long userId) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(friendshipId, userId));
    }

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> rejectFriendRequest(@PathVariable Long friendshipId,
                                                     @RequestAttribute Long userId) {
        friendshipService.rejectFriendRequest(friendshipId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long friendshipId,
                                              @RequestAttribute Long userId) {
        friendshipService.removeFriend(friendshipId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Friendship>> getFriends(@RequestAttribute Long userId) {
        return ResponseEntity.ok(friendshipService.getFriends(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Friendship>> getPendingRequests(@RequestAttribute Long userId) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(userId));
    }
}
