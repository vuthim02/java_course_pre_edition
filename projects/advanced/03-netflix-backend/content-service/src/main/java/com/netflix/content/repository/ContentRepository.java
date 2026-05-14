package com.netflix.content.repository;

import com.netflix.content.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByGenre(String genre);
    List<Content> findByContentType(String contentType);
    List<Content> findByReleaseYear(Integer releaseYear);
}
