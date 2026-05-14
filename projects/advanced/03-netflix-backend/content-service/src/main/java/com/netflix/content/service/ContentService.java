package com.netflix.content.service;

import com.netflix.content.dto.ContentDTO;
import com.netflix.content.model.Content;
import com.netflix.content.repository.ContentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {
    private final ContentRepository repository;

    public ContentService(ContentRepository repository) {
        this.repository = repository;
    }

    public List<ContentDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public ContentDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Content not found: " + id));
    }

    public List<ContentDTO> findByGenre(String genre) {
        return repository.findByGenre(genre).stream().map(this::toDto).toList();
    }

    public List<ContentDTO> findByType(String type) {
        return repository.findByContentType(type).stream().map(this::toDto).toList();
    }

    public ContentDTO create(ContentDTO dto) {
        var content = new Content();
        content.setTitle(dto.title());
        content.setDescription(dto.description());
        content.setGenre(dto.genre());
        content.setContentType(dto.contentType());
        content.setReleaseYear(dto.releaseYear());
        content.setRating(dto.rating() != null ? dto.rating() : 0.0);
        return toDto(repository.save(content));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ContentDTO toDto(Content content) {
        return new ContentDTO(content.getId(), content.getTitle(), content.getDescription(),
                content.getGenre(), content.getContentType(), content.getReleaseYear(), content.getRating());
    }
}
