package com.restapi;

import com.restapi.dto.UserRequest;
import com.restapi.dto.UserResponse;
import com.restapi.exception.ResourceNotFoundException;
import com.restapi.model.User;
import com.restapi.repository.UserRepository;
import com.restapi.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User createUser(Long id, String name, String email) {
        User user = User.builder().id(id).name(name).email(email).build();
        user.onCreate();
        return user;
    }

    @Nested
    class CreateUser {

        @Test
        void testCreateUser_Success() {
            UserRequest request = UserRequest.builder().name("Alice").email("alice@test.com").build();
            User saved = createUser(1L, "Alice", "alice@test.com");
            when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResponse response = userService.createUser(request);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Alice", response.getName());
            assertEquals("alice@test.com", response.getEmail());
        }

        @Test
        void testCreateUser_MapsFieldsCorrectly() {
            UserRequest request = UserRequest.builder().name("Bob").email("bob@test.com").build();
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.onCreate();
                return User.builder().id(2L).name(u.getName()).email(u.getEmail()).build();
            });

            userService.createUser(request);

            User captured = userCaptor.getValue();
            assertEquals("Bob", captured.getName());
            assertEquals("bob@test.com", captured.getEmail());
        }
    }

    @Nested
    class GetUserById {

        @Test
        void testGetUserById_Success() {
            User user = createUser(1L, "Alice", "alice@test.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponse response = userService.getUserById(1L);

            assertEquals("Alice", response.getName());
            assertEquals("alice@test.com", response.getEmail());
        }

        @Test
        void testGetUserById_NotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
        }

        @Test
        void testGetUserById_NullId() {
            when(userRepository.findById(null)).thenThrow(new IllegalArgumentException("id must not be null"));

            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(null));
        }
    }

    @Nested
    class GetAllUsers {

        @Test
        void testGetAllUsers_ReturnsPagedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(createUser(1L, "Alice", "a@t.com"), createUser(2L, "Bob", "b@t.com")));
            when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

            Page<UserResponse> result = userService.getAllUsers(0, 10);

            assertEquals(2, result.getContent().size());
        }

        @Test
        void testGetAllUsers_EmptyPage() {
            when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            Page<UserResponse> result = userService.getAllUsers(0, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void testUpdateUser_Success() {
            User existing = createUser(1L, "Old", "old@test.com");
            UserRequest request = UserRequest.builder().name("New").email("new@test.com").build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.updateUser(1L, request);

            assertEquals("New", response.getName());
            assertEquals("new@test.com", response.getEmail());
        }

        @Test
        void testUpdateUser_NotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUser(999L, UserRequest.builder().name("X").email("x@t.com").build()));
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void testDeleteUser_Success() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            assertDoesNotThrow(() -> userService.deleteUser(1L));
            verify(userRepository).deleteById(1L);
        }

        @Test
        void testDeleteUser_NotFound() {
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
        }

        @Test
        void testDeleteUser_NullId() {
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(null));
        }
    }
}
