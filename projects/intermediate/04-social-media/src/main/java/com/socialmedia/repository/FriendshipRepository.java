package com.socialmedia.repository;

import com.socialmedia.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findFriends(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequests(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId1 AND f.addressee.id = :userId2) OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
