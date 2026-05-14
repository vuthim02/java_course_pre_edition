package com.socialmedia.service;

import com.socialmedia.model.*;
import com.socialmedia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }
        var existing = friendshipRepository.findFriendshipBetweenUsers(requesterId, addresseeId);
        if (existing.isPresent()) {
            throw new RuntimeException("Friend request already exists");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status("PENDING")
                .build();
        friendship = friendshipRepository.save(friendship);

        notificationService.sendNotification(addresseeId, "FRIEND_REQUEST",
                requester.getUsername() + " sent you a friend request", friendship.getId().toString());
        return friendship;
    }

    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to accept this request");
        }
        if (!friendship.getStatus().equals("PENDING")) {
            throw new RuntimeException("Friend request is not pending");
        }

        friendship.setStatus("ACCEPTED");
        friendship = friendshipRepository.save(friendship);

        notificationService.sendNotification(friendship.getRequester().getId(), "FRIEND_ACCEPT",
                friendship.getAddressee().getUsername() + " accepted your friend request",
                friendship.getId().toString());
        return friendship;
    }

    @Transactional
    public void rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to reject this request");
        }
        friendship.setStatus("REJECTED");
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void removeFriend(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));

        if (!friendship.getRequester().getId().equals(userId) &&
            !friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        friendshipRepository.delete(friendship);
    }

    public List<Friendship> getFriends(Long userId) {
        return friendshipRepository.findFriends(userId);
    }

    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findPendingRequests(userId);
    }
}
