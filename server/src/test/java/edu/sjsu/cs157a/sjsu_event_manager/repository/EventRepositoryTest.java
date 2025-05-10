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
    private Event conferenceEvent;
    private Event workshopEvent;
    private Event seminarEvent;

    @BeforeEach
    public void clearDatabase() {
        // Clear all tables before each test to avoid conflicts
        jdbcTemplate.execute("DELETE FROM registrations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM users");
    }

    private void createTestData() {
        // Create a test organizer
        User adminUser = new User();
        adminUser.setUsername("testadmin");
        adminUser.setEmail("admin@test.com");
        adminUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        adminUser.setFirstName("Test");
        adminUser.setLastName("Admin");
        adminUser.setRole(User.Role.ADMIN);
        userRepository.save(adminUser);
        
        organizerUser = new User();
        organizerUser.setUsername("testorganizer");
        organizerUser.setEmail("organizer@test.com");
        organizerUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        organizerUser.setFirstName("Test");
        organizerUser.setLastName("Organizer");
        organizerUser.setRole(User.Role.ORGANIZER);
        organizerUser = userRepository.save(organizerUser);
        
        User regularUser = new User();
        regularUser.setUsername("testuser");
        regularUser.setEmail("user@test.com");
        regularUser.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        regularUser.setFirstName("Test");
        regularUser.setLastName("User");
        regularUser.setRole(User.Role.USER);
        userRepository.save(regularUser);
        
        // Create test events
        LocalDateTime now = LocalDateTime.now();
        
        conferenceEvent = new Event();
        conferenceEvent.setTitle("Test Conference");
        conferenceEvent.setDescription("A test conference for testing");
        conferenceEvent.setLocation("Test Location 1");
        conferenceEvent.setStartTime(now.plusDays(1));
        conferenceEvent.setEndTime(now.plusDays(1).plusHours(8));
        conferenceEvent.setCategory("Conference");
        conferenceEvent.setOrganizer(organizerUser);
        conferenceEvent.setMaxAttendees(100);
        conferenceEvent = eventRepository.save(conferenceEvent);
        
        workshopEvent = new Event();
        workshopEvent.setTitle("Test Workshop");
        workshopEvent.setDescription("A test workshop for testing");
        workshopEvent.setLocation("Test Location 2");
        workshopEvent.setStartTime(now.plusDays(2));
        workshopEvent.setEndTime(now.plusDays(2).plusHours(3));
        workshopEvent.setCategory("Workshop");
        workshopEvent.setOrganizer(organizerUser);
        workshopEvent.setMaxAttendees(50);
        workshopEvent = eventRepository.save(workshopEvent);
        
        seminarEvent = new Event();
        seminarEvent.setTitle("Test Seminar");
        seminarEvent.setDescription("A test seminar for testing");
        seminarEvent.setLocation("Test Location 3");
        seminarEvent.setStartTime(now.plusDays(3));
        seminarEvent.setEndTime(now.plusDays(3).plusHours(2));
        seminarEvent.setCategory("Seminar");
        seminarEvent.setOrganizer(organizerUser);
        seminarEvent.setMaxAttendees(null); // No maximum attendees
        seminarEvent = eventRepository.save(seminarEvent);
    }

    @Test
    public void testFindById() {
        createTestData();
        // Test finding an event by ID
        Optional<Event> event = eventRepository.findById(conferenceEvent.getEventId());
        assertTrue(event.isPresent());
        assertEquals("Test Conference", event.get().getTitle());
        assertEquals("Conference", event.get().getCategory());
        assertEquals(organizerUser.getUserId(), event.get().getOrganizer().getUserId());
    }

    @Test
    public void testFindByNonExistentId() {
        createTestData();
        // Test finding an event with an ID that doesn't exist
        Optional<Event> event = eventRepository.findById(9999);
        assertFalse(event.isPresent());
    }

    @Test
    public void testFindByCategoryIgnoreCase() {
        createTestData();
        // Test finding events by category (case-insensitive)
        List<Event> events = eventRepository.findByCategoryIgnoreCase("workshop");
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("Test Workshop", events.get(0).getTitle());
    }

    @Test
    public void testFindByOrganizerId() {
        createTestData();
        // Test finding events by organizer ID
        List<Event> events = eventRepository.findByOrganizerId(organizerUser.getUserId());
        assertNotNull(events);
        assertEquals(3, events.size()); // All 3 events have the same organizer
    }

    @Test
    public void testFindByNonExistentOrganizerId() {
        createTestData();
        // Test finding events with an organizer ID that doesn't exist
        List<Event> events = eventRepository.findByOrganizerId(9999);
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    public void testFindAll() {
        createTestData();
        // Test retrieving all events
        List<Event> events = eventRepository.findAll();
        assertNotNull(events);
        assertEquals(3, events.size()); // We have 3 events in our setup
    }

    @Test
    public void testSaveNew() {
        // Create test organizer first
        User organizer = new User();
        organizer.setUsername("eventorganizer");
        organizer.setEmail("eventorg@test.com");
        organizer.setPasswordHash("$2a$10$1gJJgBlL75OIjkSgkYPXI.mV7ihEpjqTLMKNFnNnmLsX0LweouLVW");
        organizer.setFirstName("Event");
        organizer.setLastName("Organizer");
        organizer.setRole(User.Role.ORGANIZER);
        organizer = userRepository.save(organizer);
        
        // Test saving a new event
        LocalDateTime now = LocalDateTime.now();
        Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setDescription("Description for a new event");
        newEvent.setLocation("New Location");
        newEvent.setStartTime(now.plusDays(5));
        newEvent.setEndTime(now.plusDays(5).plusHours(4));
        newEvent.setCategory("Lecture");
        newEvent.setOrganizer(organizer);
        newEvent.setMaxAttendees(75);

        Event savedEvent = eventRepository.save(newEvent);
        assertNotNull(savedEvent.getEventId());
        assertEquals("New Event", savedEvent.getTitle());
        assertEquals("Lecture", savedEvent.getCategory());

        // Verify the event was actually saved
        Optional<Event> retrievedEvent = eventRepository.findById(savedEvent.getEventId());
        assertTrue(retrievedEvent.isPresent());
    }

    @Test
    public void testSaveUpdate() {
        createTestData();
        // Test updating an existing event
        seminarEvent.setTitle("Updated Seminar");
        seminarEvent.setMaxAttendees(30);
        
        Event updatedEvent = eventRepository.save(seminarEvent);
        assertEquals("Updated Seminar", updatedEvent.getTitle());
        assertEquals(Integer.valueOf(30), updatedEvent.getMaxAttendees());
        
        // Verify the update was actually saved
        Optional<Event> retrievedEvent = eventRepository.findById(seminarEvent.getEventId());
        assertTrue(retrievedEvent.isPresent());
        assertEquals("Updated Seminar", retrievedEvent.get().getTitle());
        assertEquals(Integer.valueOf(30), retrievedEvent.get().getMaxAttendees());
    }

    @Test
    public void testDeleteById() {
        createTestData();
        // Delete an event
        eventRepository.deleteById(workshopEvent.getEventId());
        
        // Verify the event was deleted
        Optional<Event> retrievedEvent = eventRepository.findById(workshopEvent.getEventId());
        assertFalse(retrievedEvent.isPresent());
        
        // Verify we now have only 2 events
        List<Event> events = eventRepository.findAll();
        assertEquals(2, events.size());
    }

    @Test
    public void testEventWithoutOrganizer() {
        // Test that we can't save an event without an organizer
        LocalDateTime now = LocalDateTime.now();
        Event invalidEvent = new Event();
        invalidEvent.setTitle("Invalid Event");
        invalidEvent.setDescription("Description for an invalid event");
        invalidEvent.setLocation("Invalid Location");
        invalidEvent.setStartTime(now.plusDays(10));
        invalidEvent.setEndTime(now.plusDays(10).plusHours(2));
        invalidEvent.setCategory("Invalid");
        invalidEvent.setMaxAttendees(10);
        // No organizer set

        assertThrows(IllegalArgumentException.class, () -> {
            eventRepository.save(invalidEvent);
        });
    }
} 