package com.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restapi.controller.UserController;
import com.restapi.dto.UserRequest;
import com.restapi.dto.UserResponse;
import com.restapi.exception.ResourceNotFoundException;
import com.restapi.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse createUserResponse(Long id, String name, String email) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class CreateUser {

        @Test
        void testCreateUser_Success() throws Exception {
            UserRequest request = UserRequest.builder().name("Alice").email("alice@test.com").build();
            UserResponse response = createUserResponse(1L, "Alice", "alice@test.com");
            when(userService.createUser(any(UserRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Alice"))
                    .andExpect(jsonPath("$.email").value("alice@test.com"));
        }

        @Test
        void testCreateUser_ValidationFails_BlankName() throws Exception {
            UserRequest request = UserRequest.builder().name("").email("alice@test.com").build();

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testCreateUser_ValidationFails_InvalidEmail() throws Exception {
            UserRequest request = UserRequest.builder().name("Alice").email("not-an-email").build();

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetAllUsers {

        @Test
        void testGetAllUsers_ReturnsPage() throws Exception {
            Page<UserResponse> page = new PageImpl<>(
                    List.of(createUserResponse(1L, "Alice", "alice@test.com")),
                    PageRequest.of(0, 10), 1);
            when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/users")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Alice"));
        }

        @Test
        void testGetAllUsers_EmptyPage() throws Exception {
            Page<UserResponse> page = Page.empty();
            when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    class GetUserById {

        @Test
        void testGetUserById_Success() throws Exception {
            UserResponse response = createUserResponse(1L, "Alice", "alice@test.com");
            when(userService.getUserById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Alice"));
        }

        @Test
        void testGetUserById_NotFound() throws Exception {
            when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User", 999L));

            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void testUpdateUser_Success() throws Exception {
            UserRequest request = UserRequest.builder().name("Alice Updated").email("alice@test.com").build();
            UserResponse response = createUserResponse(1L, "Alice Updated", "alice@test.com");
            when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Alice Updated"));
        }

        @Test
        void testUpdateUser_NotFound() throws Exception {
            UserRequest request = UserRequest.builder().name("Ghost").email("ghost@test.com").build();
            when(userService.updateUser(eq(999L), any(UserRequest.class)))
                    .thenThrow(new ResourceNotFoundException("User", 999L));

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testUpdateUser_ValidationFails() throws Exception {
            UserRequest request = UserRequest.builder().name("A").email("invalid").build();

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void testDeleteUser_Success() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void testDeleteUser_NotFound() throws Exception {
            doThrow(new ResourceNotFoundException("User", 999L)).when(userService).deleteUser(999L);

            mockMvc.perform(delete("/api/users/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
