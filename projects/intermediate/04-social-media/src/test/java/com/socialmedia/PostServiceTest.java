package com.socialmedia;

import com.socialmedia.model.*;
import com.socialmedia.repository.*;
import com.socialmedia.service.NotificationService;
import com.socialmedia.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<Like> likeCaptor;

    private User author;
    private User otherUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).username("author").build();
        otherUser = User.builder().id(2L).username("other").build();
        testPost = Post.builder().id(1L).author(author).content("Original post").build();
    }

    @Nested
    class CreatePost {

        @Test
        void testCreatePost_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post p = invocation.getArgument(0);
                p.setId(1L);
                p.onCreate();
                return p;
            });

            Post post = postService.createPost(1L, "Hello World", null);

            assertNotNull(post);
            assertEquals("Hello World", post.getContent());
            assertEquals(author, post.getAuthor());
            assertNull(post.getImageUrl());
        }

        @Test
        void testCreatePost_WithImage() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            Post post = postService.createPost(1L, "Post with image", "http://example.com/img.jpg");

            assertEquals("http://example.com/img.jpg", post.getImageUrl());
        }

        @Test
        void testCreatePost_UserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.createPost(999L, "content", null));
        }

        @Test
        void testCreatePost_EmptyContent() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post p = invocation.getArgument(0);
                p.setId(2L);
                return p;
            });

            Post post = postService.createPost(1L, "", null);

            assertEquals("", post.getContent());
        }
    }

    @Nested
    class LikePost {

        @Test
        void testLikePost_Success() {
            when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(likeRepository.save(any(Like.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertDoesNotThrow(() -> postService.likePost(1L, 2L));

            verify(likeRepository).save(any(Like.class));
            verify(notificationService).sendNotification(eq(1L), eq("LIKE"), anyString(), eq("1"));
        }

        @Test
        void testLikePost_AlreadyLiked() {
            when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);

            assertThrows(RuntimeException.class, () -> postService.likePost(1L, 2L));
        }

        @Test
        void testLikePost_OwnPost() {
            when(likeRepository.existsByPostIdAndUserId(1L, 1L)).thenReturn(false);
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(likeRepository.save(any(Like.class))).thenAnswer(invocation -> invocation.getArgument(0));

            postService.likePost(1L, 1L);

            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        void testLikePost_PostNotFound() {
            when(likeRepository.existsByPostIdAndUserId(999L, 1L)).thenReturn(false);
            when(postRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.likePost(999L, 1L));
        }
    }

    @Nested
    class UnlikePost {

        @Test
        void testUnlikePost_Success() {
            Like like = Like.builder().id(1L).post(testPost).user(otherUser).build();
            when(likeRepository.findByPostIdAndUserId(1L, 2L)).thenReturn(Optional.of(like));

            postService.unlikePost(1L, 2L);

            verify(likeRepository).delete(like);
        }

        @Test
        void testUnlikePost_NotLiked() {
            when(likeRepository.findByPostIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.unlikePost(1L, 2L));
        }
    }

    @Nested
    class AddComment {

        @Test
        void testAddComment_Success() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            Comment comment = postService.addComment(1L, 2L, "Great post!");

            assertEquals("Great post!", comment.getContent());
            assertEquals(otherUser, comment.getAuthor());
            assertEquals(testPost, comment.getPost());
            verify(notificationService).sendNotification(eq(1L), eq("COMMENT"), anyString(), eq("1"));
        }

        @Test
        void testAddComment_OnOwnPost() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(1L)).thenReturn(Optional.of(author));
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            postService.addComment(1L, 1L, "My own comment");

            verify(notificationService, never()).sendNotification(any(), any(), any(), any());
        }

        @Test
        void testAddComment_PostNotFound() {
            when(postRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.addComment(999L, 1L, "content"));
        }

        @Test
        void testAddComment_UserNotFound() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.addComment(1L, 999L, "content"));
        }
    }

    @Nested
    class DeletePost {

        @Test
        void testDeletePost_AsAuthor() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

            postService.deletePost(1L, 1L);

            verify(postRepository).delete(testPost);
        }

        @Test
        void testDeletePost_NotAuthor() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

            assertThrows(RuntimeException.class, () -> postService.deletePost(1L, 2L));
        }

        @Test
        void testDeletePost_NotFound() {
            when(postRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.deletePost(999L, 1L));
        }
    }

    @Nested
    class FeedGeneration {

        @Test
        void testGetUserPosts() {
            when(postRepository.findByAuthorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testPost)));

            Page<Post> posts = postService.getUserPosts(1L, 0, 10);

            assertEquals(1, posts.getContent().size());
        }

        @Test
        void testGetUserPosts_Empty() {
            when(postRepository.findByAuthorIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<Post> posts = postService.getUserPosts(1L, 0, 10);

            assertTrue(posts.isEmpty());
        }

        @Test
        void testGetNewsFeed() {
            Friendship friendship = Friendship.builder().requester(author).addressee(otherUser).status("ACCEPTED").build();
            when(friendshipRepository.findFriends(1L)).thenReturn(List.of(friendship));
            when(postRepository.findNewsFeed(anyList(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testPost)));

            Page<Post> feed = postService.getNewsFeed(1L, 0, 10);

            assertEquals(1, feed.getContent().size());
        }

        @Test
        void testGetNewsFeed_NoFriends() {
            when(friendshipRepository.findFriends(1L)).thenReturn(List.of());
            when(postRepository.findNewsFeed(anyList(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            Page<Post> feed = postService.getNewsFeed(1L, 0, 10);

            assertTrue(feed.isEmpty());
        }
    }
}
