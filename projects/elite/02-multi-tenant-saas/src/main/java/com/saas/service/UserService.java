package com.saas.service;

import com.saas.model.User;
import com.saas.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsersByTenant(UUID tenantId) {
        return userRepository.findByTenantId(tenantId);
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User updateUser(UUID id, User updated) {
        User user = getUser(id);
        user.setFullName(updated.getFullName());
        user.setEmail(updated.getEmail());
        user.setRole(updated.getRole());
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
