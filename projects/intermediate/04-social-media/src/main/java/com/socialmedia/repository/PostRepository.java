package com.socialmedia.repository;

import com.socialmedia.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author.id IN :friendIds ORDER BY p.createdAt DESC")
    Page<Post> findNewsFeed(@Param("friendIds") List<Long> friendIds, Pageable pageable);
}
