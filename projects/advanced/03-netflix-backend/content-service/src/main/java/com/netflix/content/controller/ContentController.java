package com.netflix.content.controller;

import com.netflix.content.dto.ContentDTO;
import com.netflix.content.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService service;

    public ContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContentDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ContentDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/genre/{genre}")
    public List<ContentDTO> getByGenre(@PathVariable String genre) {
        return service.findByGenre(genre);
    }

    @GetMapping("/type/{type}")
    public List<ContentDTO> getByType(@PathVariable String type) {
        return service.findByType(type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDTO create(@Valid @RequestBody ContentDTO dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
