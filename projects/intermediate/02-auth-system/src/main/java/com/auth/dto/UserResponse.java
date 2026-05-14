package com.auth.dto;

import com.auth.model.Role;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
