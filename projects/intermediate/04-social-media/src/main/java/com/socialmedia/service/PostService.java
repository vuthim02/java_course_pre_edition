package com.socialmedia.service;

import com.socialmedia.model.*;
import com.socialmedia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;

    public Post createPost(Long authorId, String content, String imageUrl) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = Post.builder()
                .author(author)
                .content(content)
                .imageUrl(imageUrl)
                .build();
        return postRepository.save(post);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public Page<Post> getUserPosts(Long userId, int page, int size) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new RuntimeException("Already liked this post");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Like like = Like.builder().post(post).user(user).build();
        likeRepository.save(like);

        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.sendNotification(post.getAuthor().getId(), "LIKE",
                    user.getUsername() + " liked your post", postId.toString());
        }
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        likeRepository.delete(like);
    }

    @Transactional
    public Comment addComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = Comment.builder().post(post).author(user).content(content).build();
        comment = commentRepository.save(comment);

        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.sendNotification(post.getAuthor().getId(), "COMMENT",
                    user.getUsername() + " commented on your post", postId.toString());
        }
        return comment;
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    public Page<Post> getNewsFeed(Long userId, int page, int size) {
        List<Friendship> friendships = friendshipRepository.findFriends(userId);
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getAddressee().getId() : f.getRequester().getId())
                .toList();
        friendIds.add(userId);
        return postRepository.findNewsFeed(friendIds,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }
}
