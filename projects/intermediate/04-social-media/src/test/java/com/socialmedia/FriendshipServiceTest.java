package com.socialmedia;

import com.socialmedia.model.*;
import com.socialmedia.repository.*;
import com.socialmedia.service.FriendshipService;
import com.socialmedia.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FriendshipService friendshipService;

    @Captor
    private ArgumentCaptor<Friendship> friendshipCaptor;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).username("alice").email("alice@test.com").build();
        bob = User.builder().id(2L).username("bob").email("bob@test.com").build();
    }

    @Nested
    class SendFriendRequest {

        @Test
        void testSendFriendRequest_Success() {
            when(friendshipRepository.findFriendshipBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
            when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Friendship friendship = friendshipService.sendFriendRequest(1L, 2L);

            assertEquals("PENDING", friendship.getStatus());
            assertEquals(alice, friendship.getRequester());
            assertEquals(bob, friendship.getAddressee());
            verify(notificationService).sendNotification(eq(2L), eq("FRIEND_REQUEST"), anyString(), anyString());
        }

        @Test
        void testSendFriendRequest_ToSelf() {
            assertThrows(RuntimeException.class, () -> friendshipService.sendFriendRequest(1L, 1L));
        }

        @Test
        void testSendFriendRequest_AlreadyExists() {
            when(friendshipRepository.findFriendshipBetweenUsers(1L, 2L)).thenReturn(Optional.of(new Friendship()));

            assertThrows(RuntimeException.class, () -> friendshipService.sendFriendRequest(1L, 2L));
        }

        @Test
        void testSendFriendRequest_RequesterNotFound() {
            when(friendshipRepository.findFriendshipBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> friendshipService.sendFriendRequest(1L, 2L));
        }

        @Test
        void testSendFriendRequest_AddresseeNotFound() {
            when(friendshipRepository.findFriendshipBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(userRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> friendshipService.sendFriendRequest(1L, 2L));
        }
    }

    @Nested
    class AcceptFriendRequest {

        @Test
        void testAcceptFriendRequest_Success() {
            Friendship pending = Friendship.builder().id(1L).requester(alice).addressee(bob).status("PENDING").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(pending));
            when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Friendship result = friendshipService.acceptFriendRequest(1L, 2L);

            assertEquals("ACCEPTED", result.getStatus());
            verify(notificationService).sendNotification(eq(1L), eq("FRIEND_ACCEPT"), anyString(), anyString());
        }

        @Test
        void testAcceptFriendRequest_NotAddressee() {
            Friendship pending = Friendship.builder().id(1L).requester(alice).addressee(bob).status("PENDING").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(pending));

            assertThrows(RuntimeException.class, () -> friendshipService.acceptFriendRequest(1L, 1L));
        }

        @Test
        void testAcceptFriendRequest_NotPending() {
            Friendship accepted = Friendship.builder().id(1L).requester(alice).addressee(bob).status("ACCEPTED").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(accepted));

            assertThrows(RuntimeException.class, () -> friendshipService.acceptFriendRequest(1L, 2L));
        }

        @Test
        void testAcceptFriendRequest_NotFound() {
            when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> friendshipService.acceptFriendRequest(999L, 1L));
        }
    }

    @Nested
    class RejectFriendRequest {

        @Test
        void testRejectFriendRequest_Success() {
            Friendship pending = Friendship.builder().id(1L).requester(alice).addressee(bob).status("PENDING").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(pending));

            friendshipService.rejectFriendRequest(1L, 2L);

            assertEquals("REJECTED", pending.getStatus());
            verify(friendshipRepository).save(pending);
        }

        @Test
        void testRejectFriendRequest_NotAddressee() {
            Friendship pending = Friendship.builder().id(1L).requester(alice).addressee(bob).status("PENDING").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(pending));

            assertThrows(RuntimeException.class, () -> friendshipService.rejectFriendRequest(1L, 1L));
        }

        @Test
        void testRejectFriendRequest_NotFound() {
            when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> friendshipService.rejectFriendRequest(999L, 1L));
        }
    }

    @Nested
    class Unfriend {

        @Test
        void testRemoveFriend_AsRequester() {
            Friendship friendship = Friendship.builder().id(1L).requester(alice).addressee(bob).status("ACCEPTED").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

            friendshipService.removeFriend(1L, 1L);

            verify(friendshipRepository).delete(friendship);
        }

        @Test
        void testRemoveFriend_AsAddressee() {
            Friendship friendship = Friendship.builder().id(1L).requester(alice).addressee(bob).status("ACCEPTED").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

            friendshipService.removeFriend(1L, 2L);

            verify(friendshipRepository).delete(friendship);
        }

        @Test
        void testRemoveFriend_NotParticipant() {
            Friendship friendship = Friendship.builder().id(1L).requester(alice).addressee(bob).status("ACCEPTED").build();
            when(friendshipRepository.findById(1L)).thenReturn(Optional.of(friendship));

            assertThrows(RuntimeException.class, () -> friendshipService.removeFriend(1L, 3L));
        }

        @Test
        void testRemoveFriend_NotFound() {
            when(friendshipRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> friendshipService.removeFriend(999L, 1L));
        }
    }
}
