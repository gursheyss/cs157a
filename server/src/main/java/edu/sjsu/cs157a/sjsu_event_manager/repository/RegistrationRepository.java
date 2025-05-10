package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// this class handles database operations for registrations using jdbc
@Repository
public class RegistrationRepository {

    private static final Logger log = LoggerFactory.getLogger(RegistrationRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RowMapper<Registration> registrationRowMapper;

    @Autowired
    public RegistrationRepository(DataSource dataSource, UserRepository userRepository, EventRepository eventRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;

        this.registrationRowMapper = (rs, rowNum) -> {
            Registration registration = new Registration();
            int registrationId = rs.getInt("registration_id");
            registration.setRegistrationId(registrationId);
            registration.setRegistrationTime(rs.getTimestamp("registration_time").toLocalDateTime());

            int userId = rs.getInt("user_id");
            try {
                this.userRepository.findById(userId).ifPresentOrElse(
                    registration::setUser,
                    () -> log.warn("User with ID {} not found for registration ID {}", userId, registrationId)
                );
            } catch (DataAccessException e) {
                log.error("Error fetching user {} for registration {}: {}", userId, registrationId, e.getMessage());
            }

            int eventId = rs.getInt("event_id");
            try {
                this.eventRepository.findById(eventId).ifPresentOrElse(
                    registration::setEvent,
                    () -> log.warn("Event with ID {} not found for registration ID {}", eventId, registrationId)
                );
            } catch (DataAccessException e) {
                log.error("Error fetching event {} for registration {}: {}", eventId, registrationId, e.getMessage());
            }
            return registration;
        };
    }

    // get a registration by its id
    public Optional<Registration> findById(Integer registrationId) {
        String sql = "SELECT * FROM registrations WHERE registration_id = ?";
        log.debug("Executing SQL: {} with registrationId: {}", sql, registrationId);
        try {
            Registration registration = jdbcTemplate.queryForObject(sql, registrationRowMapper, registrationId);
            return Optional.ofNullable(registration);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No registration found with registrationId: {}", registrationId);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding registration by id {}: {}", registrationId, e.getMessage());
            throw e;
        }
    }

    // get a registration for a specific user and event
    public Optional<Registration> findByUserAndEvent(Integer userId, Integer eventId) {
        String sql = "SELECT * FROM registrations WHERE user_id = ? AND event_id = ?";
        log.debug("Executing SQL: {} with userId: {}, eventId: {}", sql, userId, eventId);
        try {
            Registration registration = jdbcTemplate.queryForObject(sql, registrationRowMapper, userId, eventId);
            return Optional.ofNullable(registration);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No registration found for userId: {}, eventId: {}", userId, eventId);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding registration by userId {} and eventId {}: {}", userId, eventId, e.getMessage());
            throw e;
        }
    }

    // get all registrations for a user
    public List<Registration> findByUserId(Integer userId) {
        String sql = "SELECT * FROM registrations WHERE user_id = ?";
        log.debug("Executing SQL: {} with userId: {}", sql, userId);
         try {
            return jdbcTemplate.query(sql, registrationRowMapper, userId);
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding registrations by userId {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    // get all registrations for an event
    public List<Registration> findByEventId(Integer eventId) {
        String sql = "SELECT * FROM registrations WHERE event_id = ?";
        log.debug("Executing SQL: {} with eventId: {}", sql, eventId);
        try {
            return jdbcTemplate.query(sql, registrationRowMapper, eventId);
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding registrations by eventId {}: {}", eventId, e.getMessage());
            throw e;
        }
    }

    // check if a registration exists for a user and event
    public boolean existsByUserAndEvent(Integer userId, Integer eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ?";
        log.debug("Executing SQL: {} with userId: {}, eventId: {}", sql, userId, eventId);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("Error checking registration existence for userId {} and eventId {}: {}", userId, eventId, e.getMessage());
            throw e;
        }
    }

    // check if a registration exists for an event and user (for compatibility with EventService)
    public boolean existsByEventIdAndUserId(Integer eventId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND user_id = ?";
        log.debug("Executing SQL: {} with eventId: {}, userId: {}", sql, eventId, userId);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, userId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("Error checking registration existence for eventId {} and userId {}: {}", eventId, userId, e.getMessage());
            throw e;
        }
    }

    // count how many registrations there are for an event
    public long countByEventId(Integer eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ?";
        log.debug("Executing SQL: {} with eventId: {}", sql, eventId);
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, eventId);
            return count != null ? count : 0L;
        } catch (DataAccessException e) {
            log.error("Error counting registrations for eventId {}: {}", eventId, e.getMessage());
            throw e;
        }
    }

    // get all registrations in the database
    public List<Registration> findAll() {
        String sql = "SELECT * FROM registrations";
        log.debug("Executing SQL: {}", sql);
        try {
            return jdbcTemplate.query(sql, registrationRowMapper);
        } catch (DataAccessException e) {
            log.error("Error finding all registrations: {}", e.getMessage());
            throw e;
        }
   }

   // save a registration (insert if new, update if it already has an id)
   // registration must have a user and event with valid ids
    public Registration save(Registration registration) {
        if (registration == null) {
             throw new IllegalArgumentException("Registration to save cannot be null");
        }
        // make sure user and event are set and have ids
        if (registration.getUser() == null || registration.getUser().getUserId() == null) {
            log.error("Attempted to save registration with null user or user ID.");
            throw new IllegalArgumentException("User with a valid ID must be set before saving a registration.");
        }
        if (registration.getEvent() == null || registration.getEvent().getEventId() == null) {
            log.error("Attempted to save registration with null event or event ID. User ID: {}", registration.getUser().getUserId());
            throw new IllegalArgumentException("Event with a valid ID must be set before saving a registration.");
        }

        try {
            if (registration.getRegistrationId() == null) {
                log.debug("Creating new registration for User ID: {} and Event ID: {}", registration.getUser().getUserId(), registration.getEvent().getEventId());
                return insertRegistration(registration);
            } else {
                log.debug("Updating registration with ID: {}", registration.getRegistrationId());
                return updateRegistration(registration);
            }
        } catch (DataAccessException e) {
            log.error("Error saving registration for User ID {} and Event ID {}: {}",
                      registration.getUser().getUserId(), registration.getEvent().getEventId(), e.getMessage());
            throw e;
        }
    }

    // insert a new registration into the database
    private Registration insertRegistration(Registration registration) {
        // registration_time has a default value in the database, but we set it here
        String sql = "INSERT INTO registrations (user_id, event_id, registration_time) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        java.time.LocalDateTime registrationTime = registration.getRegistrationTime() != null ? registration.getRegistrationTime() : java.time.LocalDateTime.now();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, registration.getUser().getUserId());
            ps.setInt(2, registration.getEvent().getEventId());
            ps.setTimestamp(3, Timestamp.valueOf(registrationTime));
            return ps;
        }, keyHolder);

         if (rowsAffected == 0) {
             log.error("Failed to insert registration, no rows affected. User ID: {}, Event ID: {}", registration.getUser().getUserId(), registration.getEvent().getEventId());
             throw new RuntimeException("Registration insert failed.");
        }

        // Extract registration_id from the generated keys
        Number key = null;
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys.containsKey("registration_id")) {
                key = (Number) keys.get("registration_id");
            } else if (keyHolder.getKeyList().size() > 0) {
                Map<String, Object> firstKeyMap = keyHolder.getKeyList().get(0);
                if (firstKeyMap.containsKey("registration_id")) {
                    key = (Number) firstKeyMap.get("registration_id");
                }
            }
        }
        
        if (key == null && keyHolder.getKey() != null) {
            key = keyHolder.getKey();
        }

        if (key != null) {
            registration.setRegistrationId(key.intValue());
            // set the time we used for the insert
            registration.setRegistrationTime(registrationTime);
            log.info("Successfully inserted registration with ID: {}. User ID: {}, Event ID: {}",
                     registration.getRegistrationId(), registration.getUser().getUserId(), registration.getEvent().getEventId());
            return registration;
        } else {
             log.error("Failed to retrieve generated key for registration. User ID: {}, Event ID: {}", registration.getUser().getUserId(), registration.getEvent().getEventId());
             throw new RuntimeException("Failed to retrieve generated key for registration.");
        }
    }

    // update an existing registration
    // usually you don't update registrations, but this lets you change user/event/time
    private Registration updateRegistration(Registration registration) {
         String sql = "UPDATE registrations SET user_id = ?, event_id = ?, registration_time = ? WHERE registration_id = ?";
         java.time.LocalDateTime registrationTime = registration.getRegistrationTime() != null ? registration.getRegistrationTime() : java.time.LocalDateTime.now();

         int rowsAffected = jdbcTemplate.update(sql,
            registration.getUser().getUserId(),
            registration.getEvent().getEventId(),
            Timestamp.valueOf(registrationTime),
            registration.getRegistrationId());

         if (rowsAffected == 0) {
             log.warn("Attempted to update registration with ID {} but no rows affected.", registration.getRegistrationId());
         } else {
             log.info("Successfully updated registration with ID: {}", registration.getRegistrationId());
         }
         // set the time we used for the update
         registration.setRegistrationTime(registrationTime);
         return registration;
    }

    // delete a registration by its id
     public void deleteById(Integer registrationId) {
        String sql = "DELETE FROM registrations WHERE registration_id = ?";
        log.debug("Executing SQL: {} with registrationId: {}", sql, registrationId);
        try {
            int rowsAffected = jdbcTemplate.update(sql, registrationId);
            if (rowsAffected == 0) {
                log.warn("Attempted to delete registration with ID {} but no rows were affected. Registration might not exist.", registrationId);
            } else {
                log.info("Successfully deleted registration with ID: {}", registrationId);
            }
        } catch (DataAccessException e) {
            log.error("Error deleting registration with ID {}: {}", registrationId, e.getMessage());
            throw e;
        }
    }
} 