package com.netflix.user.controller;

import com.netflix.user.dto.ProfileDTO;
import com.netflix.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final UserProfileService service;

    public ProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProfileDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProfileDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDTO create(@Valid @RequestBody ProfileDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ProfileDTO update(@PathVariable Long id, @Valid @RequestBody ProfileDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
