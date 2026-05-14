package com.socialmedia.controller;

import com.socialmedia.model.Notification;
import com.socialmedia.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestAttribute Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestAttribute Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestAttribute Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId,
                                            @RequestAttribute Long userId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestAttribute Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
