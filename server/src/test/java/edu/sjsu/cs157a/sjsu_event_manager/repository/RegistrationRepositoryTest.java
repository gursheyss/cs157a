package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.config.TestConfig;
import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class RegistrationRepositoryTest {
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private User testUser;
    private Event testEvent;
    private Registration testRegistration;

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
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("desc");
        testEvent.setLocation("loc");
        testEvent.setStartTime(LocalDateTime.now());
        testEvent.setEndTime(LocalDateTime.now().plusHours(1));
        testEvent.setCategory("Category");
        testEvent.setOrganizer(testUser);
        testEvent.setMaxAttendees(10);
        testEvent = eventRepository.save(testEvent);
        testRegistration = new Registration();
        testRegistration.setUser(testUser);
        testRegistration.setEvent(testEvent);
        testRegistration.setRegistrationTime(LocalDateTime.now());
        testRegistration = registrationRepository.save(testRegistration);
    }

    @Test
    public void testFindById() {
        Optional<Registration> reg = registrationRepository.findById(testRegistration.getRegistrationId());
        assertTrue(reg.isPresent());
    }

    @Test
    public void testFindByUserAndEvent() {
        Optional<Registration> reg = registrationRepository.findByUserAndEvent(testUser.getUserId(), testEvent.getEventId());
        assertTrue(reg.isPresent());
    }

    @Test
    public void testFindByUserId() {
        List<Registration> regs = registrationRepository.findByUserId(testUser.getUserId());
        assertNotNull(regs);
    }

    @Test
    public void testFindByEventId() {
        List<Registration> regs = registrationRepository.findByEventId(testEvent.getEventId());
        assertNotNull(regs);
    }

    @Test
    public void testExistsByUserAndEvent() {
        assertTrue(registrationRepository.existsByUserAndEvent(testUser.getUserId(), testEvent.getEventId()));
    }

    @Test
    public void testCountByEventId() {
        long count = registrationRepository.countByEventId(testEvent.getEventId());
        assertTrue(count >= 0);
    }

    @Test
    public void testFindAll() {
        List<Registration> regs = registrationRepository.findAll();
        assertNotNull(regs);
    }

    @Test
    public void testSaveUpdate() {
        testRegistration.setRegistrationTime(LocalDateTime.now().plusDays(1));
        Registration updated = registrationRepository.save(testRegistration);
        assertNotNull(updated);
    }

    @Test
    public void testDeleteById() {
        registrationRepository.deleteById(testRegistration.getRegistrationId());
        Optional<Registration> reg = registrationRepository.findById(testRegistration.getRegistrationId());
        assertFalse(reg.isPresent());
    }
} 