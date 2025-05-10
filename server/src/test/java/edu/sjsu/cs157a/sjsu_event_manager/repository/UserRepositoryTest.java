package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.config.TestConfig;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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
    
    private User adminUser;
    private User organizerUser;
    private User regularUser;

    @BeforeEach
    public void clearDatabase() {
        // Clear all tables before each test to avoid conflicts
        jdbcTemplate.execute("DELETE FROM registrations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM users");
    }
    
    private void createTestUsers() {
        // Create test users
        adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setEmail("admin@test.com");
        adminUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        adminUser.setFirstName("Test");
        adminUser.setLastName("Admin");
        adminUser.setRole(User.Role.ADMIN);
        adminUser = userRepository.save(adminUser);
        
        organizerUser = new User();
        organizerUser.setUsername("testorganizer");
        organizerUser.setEmail("organizer@test.com");
        organizerUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        organizerUser.setFirstName("Test");
        organizerUser.setLastName("Organizer");
        organizerUser.setRole(User.Role.ORGANIZER);
        organizerUser = userRepository.save(organizerUser);
        
        regularUser = new User();
        regularUser.setUsername("testuser");
        regularUser.setEmail("user@test.com");
        regularUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        regularUser.setFirstName("Test");
        regularUser.setLastName("User");
        regularUser.setRole(User.Role.USER);
        regularUser = userRepository.save(regularUser);
    }

    @Test
    public void testFindById() {
        createTestUsers();
        // Test finding a user by ID
        Optional<User> user = userRepository.findById(adminUser.getUserId());
        assertTrue(user.isPresent());
        assertEquals("testadmin", user.get().getUsername());
        assertEquals("admin@test.com", user.get().getEmail());
        assertEquals(User.Role.ADMIN, user.get().getRole());
    }

    @Test
    public void testFindByUsername() {
        createTestUsers();
        // Test finding a user by username
        Optional<User> user = userRepository.findByUsername("testorganizer");
        assertTrue(user.isPresent());
        assertEquals(organizerUser.getUserId(), user.get().getUserId());
        assertEquals("organizer@test.com", user.get().getEmail());
        assertEquals(User.Role.ORGANIZER, user.get().getRole());
    }

    @Test
    public void testFindByEmail() {
        createTestUsers();
        // Test finding a user by email
        Optional<User> user = userRepository.findByEmail("user@test.com");
        assertTrue(user.isPresent());
        assertEquals(regularUser.getUserId(), user.get().getUserId());
        assertEquals("testuser", user.get().getUsername());
        assertEquals(User.Role.USER, user.get().getRole());
    }

    @Test
    public void testFindByNonExistentUsername() {
        createTestUsers();
        // Test finding a user with a username that doesn't exist
        Optional<User> user = userRepository.findByUsername("nonexistentuser");
        assertFalse(user.isPresent());
    }

    @Test
    public void testFindAll() {
        createTestUsers();
        // Test retrieving all users
        List<User> users = userRepository.findAll();
        assertNotNull(users);
        assertEquals(3, users.size()); // We have 3 users in test data
    }

    @Test
    public void testExistsByUsername() {
        createTestUsers();
        // Test checking if a user exists by username
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistentuser"));
    }

    @Test
    public void testExistsByEmail() {
        createTestUsers();
        // Test checking if a user exists by email
        assertTrue(userRepository.existsByEmail("admin@test.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }

    @Test
    public void testSaveNew() {
        // Test saving a new user
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@test.com");
        newUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(User.Role.USER);

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser.getUserId());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("new@test.com", savedUser.getEmail());

        // Verify the user was actually saved
        Optional<User> retrievedUser = userRepository.findById(savedUser.getUserId());
        assertTrue(retrievedUser.isPresent());
    }

    @Test
    public void testSaveUpdate() {
        createTestUsers();
        // Test updating an existing user
        regularUser.setFirstName("Updated");
        regularUser.setLastName("Name");
        
        User updatedUser = userRepository.save(regularUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        
        // Verify the update was actually saved
        Optional<User> retrievedUser = userRepository.findById(regularUser.getUserId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("Updated", retrievedUser.get().getFirstName());
        assertEquals("Name", retrievedUser.get().getLastName());
    }

    @Test
    public void testDeleteById() {
        // Create a user to delete
        User userToDelete = new User();
        userToDelete.setUsername("deleteuser");
        userToDelete.setEmail("delete@test.com");
        userToDelete.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        userToDelete.setFirstName("Delete");
        userToDelete.setLastName("User");
        userToDelete.setRole(User.Role.USER);

        User savedUser = userRepository.save(userToDelete);
        assertNotNull(savedUser.getUserId());
        
        // Delete the user
        userRepository.deleteById(savedUser.getUserId());
        
        // Verify the user was deleted
        Optional<User> retrievedUser = userRepository.findById(savedUser.getUserId());
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    public void testUniqueUsername() {
        // Create a user first
        User firstUser = new User();
        firstUser.setUsername("testuser");
        firstUser.setEmail("first@test.com");
        firstUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        firstUser.setFirstName("Test");
        firstUser.setLastName("User");
        firstUser.setRole(User.Role.USER);
        userRepository.save(firstUser);
        
        // Test that we can't save a user with a duplicate username
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // Already exists
        duplicateUser.setEmail("unique@test.com"); 
        duplicateUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(User.Role.USER);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
        });
    }

    @Test
    public void testUniqueEmail() {
        // Create a user first
        User firstUser = new User();
        firstUser.setUsername("firstuser");
        firstUser.setEmail("user@test.com");
        firstUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        firstUser.setFirstName("First");
        firstUser.setLastName("User");
        firstUser.setRole(User.Role.USER);
        userRepository.save(firstUser);
        
        // Test that we can't save a user with a duplicate email
        User duplicateUser = new User();
        duplicateUser.setUsername("uniqueuser");
        duplicateUser.setEmail("user@test.com"); // Already exists
        duplicateUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        duplicateUser.setFirstName("Unique");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(User.Role.USER);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
        });
    }
} 