package com.auth;

import com.auth.controller.AuthController;
import com.auth.dto.*;
import com.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private AuthResponse createAuthResponse() {
        return AuthResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .build();
    }

    @Nested
    class Register {

        @Test
        void testRegister_Success() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Alice").email("alice@test.com").password("password123").build();
            when(authService.register(any(RegisterRequest.class))).thenReturn(createAuthResponse());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        void testRegister_ValidationFails_BlankName() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("").email("alice@test.com").password("password123").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_ValidationFails_InvalidEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Alice").email("invalid").password("password123").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_ValidationFails_ShortPassword() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Alice").email("alice@test.com").password("12345").build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testRegister_DuplicateEmail() throws Exception {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Alice").email("existing@test.com").password("password123").build();
            when(authService.register(any(RegisterRequest.class)))
                    .thenThrow(new RuntimeException("Email already registered"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class Login {

        @Test
        void testLogin_Success() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("alice@test.com").password("password123").build();
            when(authService.login(any(LoginRequest.class))).thenReturn(createAuthResponse());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token-123"));
        }

        @Test
        void testLogin_ValidationFails_BlankEmail() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("").password("password123").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testLogin_ValidationFails_BlankPassword() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("alice@test.com").password("").build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testLogin_InvalidCredentials() throws Exception {
            LoginRequest request = LoginRequest.builder()
                    .email("alice@test.com").password("wrong").build();
            when(authService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Invalid email or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class RefreshToken {

        @Test
        void testRefresh_Success() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("valid-refresh-token").build();
            when(authService.refresh(anyString())).thenReturn(createAuthResponse());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"));
        }

        @Test
        void testRefresh_InvalidToken() throws Exception {
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token").build();
            when(authService.refresh(anyString()))
                    .thenThrow(new RuntimeException("Invalid refresh token"));

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
}
