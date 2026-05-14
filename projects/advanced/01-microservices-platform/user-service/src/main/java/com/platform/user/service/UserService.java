package com.platform.user.service;

import com.platform.user.dto.UserDTO;
import com.platform.user.model.User;
import com.platform.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<UserDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public UserDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public UserDTO create(UserDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists: " + dto.email());
        }
        var user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        return toDto(repository.save(user));
    }

    public UserDTO update(Long id, UserDTO dto) {
        var user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setName(dto.name());
        user.setEmail(dto.email());
        return toDto(repository.save(user));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("User not found: " + id);
        }
        repository.deleteById(id);
    }

    private UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
