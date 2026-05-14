package com.restapi;

import com.restapi.model.User;
import com.restapi.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String name, String email) {
        User user = User.builder().name(name).email(email).build();
        user.onCreate();
        return user;
    }

    @Nested
    class Create {

        @Test
        void testSaveUser() {
            User user = createUser("Alice", "alice@test.com");

            User saved = userRepository.save(user);

            assertNotNull(saved.getId());
            assertEquals("Alice", saved.getName());
            assertEquals("alice@test.com", saved.getEmail());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        void testSaveMultipleUsers() {
            userRepository.save(createUser("Alice", "alice@test.com"));
            userRepository.save(createUser("Bob", "bob@test.com"));

            assertEquals(2, userRepository.count());
        }
    }

    @Nested
    class Read {

        @Test
        void testFindById() {
            User saved = userRepository.save(createUser("Alice", "alice@test.com"));

            Optional<User> found = userRepository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("Alice", found.get().getName());
        }

        @Test
        void testFindById_NotFound() {
            assertFalse(userRepository.findById(999L).isPresent());
        }

        @Test
        void testFindAll() {
            userRepository.save(createUser("Alice", "a@t.com"));
            userRepository.save(createUser("Bob", "b@t.com"));

            assertEquals(2, userRepository.findAll().size());
        }

        @Test
        void testFindAll_Empty() {
            assertTrue(userRepository.findAll().isEmpty());
        }
    }

    @Nested
    class Update {

        @Test
        void testUpdateUser() {
            User saved = userRepository.save(createUser("Alice", "alice@test.com"));

            saved.setName("Alice Updated");
            saved.setEmail("alice.new@test.com");
            userRepository.save(saved);

            User updated = userRepository.findById(saved.getId()).get();
            assertEquals("Alice Updated", updated.getName());
            assertEquals("alice.new@test.com", updated.getEmail());
        }

        @Test
        void testUpdateDoesNotChangeCreatedAt() throws InterruptedException {
            User saved = userRepository.save(createUser("Alice", "alice@test.com"));
            Thread.sleep(10);

            saved.setName("Alice Updated");
            userRepository.save(saved);

            User updated = userRepository.findById(saved.getId()).get();
            assertEquals(saved.getCreatedAt(), updated.getCreatedAt());
        }
    }

    @Nested
    class Delete {

        @Test
        void testDeleteById() {
            User saved = userRepository.save(createUser("Alice", "alice@test.com"));

            userRepository.deleteById(saved.getId());

            assertFalse(userRepository.findById(saved.getId()).isPresent());
        }

        @Test
        void testDeleteAll() {
            userRepository.save(createUser("Alice", "a@t.com"));
            userRepository.save(createUser("Bob", "b@t.com"));

            userRepository.deleteAll();

            assertEquals(0, userRepository.count());
        }
    }

    @Nested
    class Pagination {

        @Test
        void testPagination() {
            for (int i = 0; i < 25; i++) {
                userRepository.save(createUser("User" + i, "user" + i + "@test.com"));
            }

            Page<User> page1 = userRepository.findAll(PageRequest.of(0, 10));
            Page<User> page2 = userRepository.findAll(PageRequest.of(1, 10));
            Page<User> page3 = userRepository.findAll(PageRequest.of(2, 10));

            assertEquals(10, page1.getContent().size());
            assertEquals(10, page2.getContent().size());
            assertEquals(5, page3.getContent().size());
            assertEquals(25, page1.getTotalElements());
            assertEquals(3, page1.getTotalPages());
        }

        @Test
        void testPagination_EmptyPage() {
            Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

            assertTrue(page.isEmpty());
        }
    }
}
