package com.saas.controller;

import com.saas.model.User;
import com.saas.service.UserService;
import com.saas.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return ResponseEntity.ok(userService.getUsersByTenant(UUID.fromString(tenantId)));
        }
        return ResponseEntity.ok(userService.getUsersByTenant(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
