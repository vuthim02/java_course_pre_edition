package com.auth;

import com.auth.config.SecurityConfig;
import com.auth.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testAuthEndpointsPermitAll() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUsersEndpoint_RequiresAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpoints_RequiresAuth() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAnyOtherEndpoint_RequiresAuth() throws Exception {
        mockMvc.perform(get("/api/some-other"))
                .andExpect(status().isForbidden());
    }
}
