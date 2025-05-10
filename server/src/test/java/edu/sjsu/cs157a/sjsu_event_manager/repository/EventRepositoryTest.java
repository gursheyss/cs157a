package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.config.TestConfig;
import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
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
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private User organizerUser;
    private Event testEvent;

    @BeforeEach
    public void clearDatabase() {
        // Clear all tables before each test to avoid conflicts
        jdbcTemplate.execute("DELETE FROM registrations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM users");
        organizerUser = new User();
        organizerUser.setUsername("organizer");
        organizerUser.setEmail("org@test.com");
        organizerUser.setPasswordHash("hash");
        organizerUser.setFirstName("Org");
        organizerUser.setLastName("User");
        organizerUser.setRole(User.Role.ORGANIZER);
        organizerUser = userRepository.save(organizerUser);
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("desc");
        testEvent.setLocation("loc");
        testEvent.setStartTime(LocalDateTime.now());
        testEvent.setEndTime(LocalDateTime.now().plusHours(1));
        testEvent.setCategory("Category");
        testEvent.setOrganizer(organizerUser);
        testEvent.setMaxAttendees(10);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    public void testFindById() {
        Optional<Event> event = eventRepository.findById(testEvent.getEventId());
        assertTrue(event.isPresent());
    }

    @Test
    public void testFindByOrganizerId() {
        List<Event> events = eventRepository.findByOrganizerId(organizerUser.getUserId());
        assertNotNull(events);
    }

    @Test
    public void testFindByCategoryIgnoreCase() {
        List<Event> events = eventRepository.findByCategoryIgnoreCase("category");
        assertNotNull(events);
    }

    @Test
    public void testFindAll() {
        List<Event> events = eventRepository.findAll();
        assertNotNull(events);
    }

    @Test
    public void testSaveUpdate() {
        testEvent.setTitle("Updated");
        Event updatedEvent = eventRepository.save(testEvent);
        assertEquals("Updated", updatedEvent.getTitle());
    }

    @Test
    public void testDeleteById() {
        eventRepository.deleteById(testEvent.getEventId());
        Optional<Event> event = eventRepository.findById(testEvent.getEventId());
        assertFalse(event.isPresent());
    }

    @Test
    public void testExistsById() {
        assertTrue(eventRepository.existsById(testEvent.getEventId()));
    }
} 