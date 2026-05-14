package com.platform.user.controller;

import com.platform.user.dto.UserDTO;
import com.platform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
