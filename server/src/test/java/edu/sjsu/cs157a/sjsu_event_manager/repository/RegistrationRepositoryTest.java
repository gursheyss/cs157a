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
import org.springframework.dao.DataIntegrityViolationException;
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
    
    private User adminUser;
    private User organizerUser;
    private User regularUser;
    private Event conferenceEvent;
    private Event workshopEvent;
    private Event seminarEvent;
    private Registration reg1;
    private Registration reg2;
    private Registration reg3;
    private Registration reg4;

    @BeforeEach
    public void clearDatabase() {
        // Clear all tables before each test to avoid conflicts
        jdbcTemplate.execute("DELETE FROM registrations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM users");
    }

    private void createTestData() {
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
        
        // Create test registrations
        reg1 = new Registration();
        reg1.setUser(regularUser);
        reg1.setEvent(conferenceEvent);
        reg1.setRegistrationTime(now.minusDays(1));
        reg1 = registrationRepository.save(reg1);
        
        reg2 = new Registration();
        reg2.setUser(regularUser);
        reg2.setEvent(workshopEvent);
        reg2.setRegistrationTime(now.minusDays(2));
        reg2 = registrationRepository.save(reg2);
        
        reg3 = new Registration();
        reg3.setUser(adminUser);
        reg3.setEvent(seminarEvent);
        reg3.setRegistrationTime(now.minusDays(3));
        reg3 = registrationRepository.save(reg3);
        
        reg4 = new Registration();
        reg4.setUser(organizerUser);
        reg4.setEvent(seminarEvent);
        reg4.setRegistrationTime(now.minusDays(4));
        reg4 = registrationRepository.save(reg4);
    }

    @Test
    public void testFindById() {
        createTestData();
        // Test finding a registration by ID
        Optional<Registration> registration = registrationRepository.findById(reg1.getRegistrationId());
        assertTrue(registration.isPresent());
        assertEquals(regularUser.getUserId(), registration.get().getUser().getUserId());
        assertEquals(conferenceEvent.getEventId(), registration.get().getEvent().getEventId());
    }

    @Test
    public void testFindByUserAndEvent() {
        createTestData();
        // Test finding a registration by user and event
        Optional<Registration> registration = registrationRepository.findByUserAndEvent(
                regularUser.getUserId(), conferenceEvent.getEventId());
        assertTrue(registration.isPresent());
        assertEquals(reg1.getRegistrationId(), registration.get().getRegistrationId());
    }

    @Test
    public void testFindByUserId() {
        createTestData();
        // Test finding registrations by user ID
        List<Registration> registrations = registrationRepository.findByUserId(regularUser.getUserId());
        assertNotNull(registrations);
        assertEquals(2, registrations.size()); // Regular user has 2 registrations
        
        // Verify the registrations are for the correct events
        assertTrue(registrations.stream().anyMatch(r -> 
                r.getEvent().getEventId().equals(conferenceEvent.getEventId())));
        assertTrue(registrations.stream().anyMatch(r -> 
                r.getEvent().getEventId().equals(workshopEvent.getEventId())));
    }

    @Test
    public void testFindByEventId() {
        createTestData();
        // Test finding registrations by event ID
        List<Registration> registrations = registrationRepository.findByEventId(seminarEvent.getEventId());
        assertNotNull(registrations);
        assertEquals(2, registrations.size()); // Seminar event has 2 registrations
        
        // Verify the registrations are for the correct users
        assertTrue(registrations.stream().anyMatch(r -> 
                r.getUser().getUserId().equals(adminUser.getUserId())));
        assertTrue(registrations.stream().anyMatch(r -> 
                r.getUser().getUserId().equals(organizerUser.getUserId())));
    }

    @Test
    public void testExistsByUserAndEvent() {
        createTestData();
        // Test checking if a registration exists by user and event
        assertTrue(registrationRepository.existsByEventIdAndUserId(
                regularUser.getUserId(), conferenceEvent.getEventId()));
        assertFalse(registrationRepository.existsByEventIdAndUserId(
                regularUser.getUserId(), seminarEvent.getEventId()));
    }

    @Test
    public void testCountByEventId() {
        createTestData();
        // Test counting registrations by event ID
        long count = registrationRepository.countByEventId(seminarEvent.getEventId());
        assertEquals(2, count); // Seminar event has 2 registrations
        
        count = registrationRepository.countByEventId(conferenceEvent.getEventId());
        assertEquals(1, count); // Conference event has 1 registration
    }

    @Test
    public void testFindAll() {
        createTestData();
        // Test retrieving all registrations
        List<Registration> registrations = registrationRepository.findAll();
        assertNotNull(registrations);
        assertEquals(4, registrations.size()); // We have 4 registrations in test data
    }

    @Test
    public void testSaveNew() {
        createTestData();
        // Test saving a new registration
        Registration newRegistration = new Registration();
        newRegistration.setUser(adminUser);
        newRegistration.setEvent(conferenceEvent);
        newRegistration.setRegistrationTime(LocalDateTime.now());

        Registration savedRegistration = registrationRepository.save(newRegistration);
        assertNotNull(savedRegistration.getRegistrationId());
        assertEquals(adminUser.getUserId(), savedRegistration.getUser().getUserId());
        assertEquals(conferenceEvent.getEventId(), savedRegistration.getEvent().getEventId());

        // Verify the registration was actually saved
        Optional<Registration> retrievedRegistration = registrationRepository.findById(savedRegistration.getRegistrationId());
        assertTrue(retrievedRegistration.isPresent());
    }

    @Test
    public void testDeleteById() {
        createTestData();
        // Delete a registration
        registrationRepository.deleteById(reg2.getRegistrationId());
        
        // Verify the registration was deleted
        Optional<Registration> retrievedRegistration = registrationRepository.findById(reg2.getRegistrationId());
        assertFalse(retrievedRegistration.isPresent());
        
        // Verify we now have only 3 registrations
        List<Registration> registrations = registrationRepository.findAll();
        assertEquals(3, registrations.size());
    }

    @Test
    public void testUniqueUserEvent() {
        createTestData();
        // Test that we can't save a duplicate registration for the same user and event
        Registration duplicateRegistration = new Registration();
        duplicateRegistration.setUser(regularUser);
        duplicateRegistration.setEvent(conferenceEvent);
        duplicateRegistration.setRegistrationTime(LocalDateTime.now());
        
        // Attempting to save a duplicate registration should throw an exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            registrationRepository.save(duplicateRegistration);
        });
    }

    @Test
    public void testRegistrationWithoutUser() {
        createTestData();
        // Test that we can't save a registration without a user
        Registration invalidRegistration = new Registration();
        invalidRegistration.setEvent(conferenceEvent);
        invalidRegistration.setRegistrationTime(LocalDateTime.now());
        // No user set
        
        assertThrows(IllegalArgumentException.class, () -> {
            registrationRepository.save(invalidRegistration);
        });
    }

    @Test
    public void testRegistrationWithoutEvent() {
        createTestData();
        // Test that we can't save a registration without an event
        Registration invalidRegistration = new Registration();
        invalidRegistration.setUser(regularUser);
        invalidRegistration.setRegistrationTime(LocalDateTime.now());
        // No event set
        
        assertThrows(IllegalArgumentException.class, () -> {
            registrationRepository.save(invalidRegistration);
        });
    }
} 