package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.config.TestConfig;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private User testUser;

    @BeforeEach
    public void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM registrations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM users");
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("user@test.com");
        testUser.setPasswordHash("hash");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    public void testFindById() {
        Optional<User> user = userRepository.findById(testUser.getUserId());
        assertTrue(user.isPresent());
    }

    @Test
    public void testFindByUsername() {
        Optional<User> user = userRepository.findByUsername("testuser");
        assertTrue(user.isPresent());
    }

    @Test
    public void testFindByEmail() {
        Optional<User> user = userRepository.findByEmail("user@test.com");
        assertTrue(user.isPresent());
    }

    @Test
    public void testFindAll() {
        List<User> users = userRepository.findAll();
        assertNotNull(users);
    }

    @Test
    public void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    public void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("user@test.com"));
    }

    @Test
    public void testSaveUpdate() {
        testUser.setFirstName("Updated");
        User updatedUser = userRepository.save(testUser);
        assertEquals("Updated", updatedUser.getFirstName());
    }

    @Test
    public void testDeleteById() {
        userRepository.deleteById(testUser.getUserId());
        Optional<User> user = userRepository.findById(testUser.getUserId());
        assertFalse(user.isPresent());
    }
} 