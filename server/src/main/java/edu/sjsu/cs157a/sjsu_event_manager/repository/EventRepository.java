package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
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

/**
 * this class handles database operations for events using jdbc
 */
@Repository
public class EventRepository {

    private static final Logger log = LoggerFactory.getLogger(EventRepository.class);

    private final JdbcTemplate jdbcTemplate;
    // inject userrepository to fetch the organizer user object for the event
    private final UserRepository userRepository;
    private final RowMapper<Event> eventRowMapper;

    @Autowired
    public EventRepository(DataSource dataSource, UserRepository userRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRepository = userRepository;

        this.eventRowMapper = (rs, rowNum) -> {
            Event event = new Event();
            event.setEventId(rs.getInt("event_id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setLocation(rs.getString("location"));
            event.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            event.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            event.setCategory(rs.getString("category"));
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            event.setMaxAttendees(rs.getObject("max_attendees") != null ? rs.getInt("max_attendees") : null);

            int organizerId = rs.getInt("organizer_id");
            try {
                this.userRepository.findById(organizerId).ifPresentOrElse(
                    event::setOrganizer,
                    () -> log.warn("Organizer user with ID {} not found for event ID {}", organizerId, event.getEventId())
                );
            } catch (DataAccessException e) {
                log.error("Error fetching organizer user with ID {} for event ID {}: {}", organizerId, event.getEventId(), e.getMessage());
            }
            return event;
        };
    }

    /**
     * get an event by its id
     */
    public Optional<Event> findById(Integer eventId) {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        log.debug("Executing SQL: {} with eventId: {}", sql, eventId);
        try {
            Event event = jdbcTemplate.queryForObject(sql, eventRowMapper, eventId);
            return Optional.ofNullable(event);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No event found with eventId: {}", eventId);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding event by id {}: {}", eventId, e.getMessage());
            throw e;
        }
    }

    /**
     * get all events for an organizer
     */
    public List<Event> findByOrganizerId(Integer organizerId) {
        String sql = "SELECT * FROM events WHERE organizer_id = ?";
        log.debug("Executing SQL: {} with organizerId: {}", sql, organizerId);
        try {
            return jdbcTemplate.query(sql, eventRowMapper, organizerId);
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding events by organizerId {}: {}", organizerId, e.getMessage());
            throw e;
        }
    }

    /**
     * get all events for a category (case-insensitive)
     */
    public List<Event> findByCategoryIgnoreCase(String category) {
        String sql = "SELECT * FROM events WHERE LOWER(category) = LOWER(?)";
        log.debug("Executing SQL: {} with category: {}", sql, category);
        try {
            return jdbcTemplate.query(sql, eventRowMapper, category);
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding events by category {}: {}", category, e.getMessage());
            throw e;
        }
    }

    /**
     * get all events
     */
    public List<Event> findAll() {
         String sql = "SELECT * FROM events";
         log.debug("Executing SQL: {}", sql);
          try {
             return jdbcTemplate.query(sql, eventRowMapper);
         } catch (DataAccessException e) {
             log.error("Error accessing data while finding all events: {}", e.getMessage());
             throw e;
         }
    }

    /**
     * save an event (insert if new, update if it already has an id)
     * event must have an organizer with a valid id
     */
    public Event save(Event event) {
         if (event == null) {
            throw new IllegalArgumentException("Event to save cannot be null");
        }
        if (event.getOrganizer() == null || event.getOrganizer().getUserId() == null) {
            log.error("Attempted to save event with null organizer or organizer ID. Title: {}", event.getTitle());
            throw new IllegalArgumentException("Organizer with a valid ID must be set before saving an event.");
        }

        try {
            if (event.getEventId() == null) {
                log.debug("Creating new event with title: {}", event.getTitle());
                return insertEvent(event);
            } else {
                log.debug("Updating event with ID: {}", event.getEventId());
                return updateEvent(event);
            }
        } catch (DataAccessException e) {
             log.error("Error saving event \"{}\": {}", event.getTitle(), e.getMessage());
             throw e;
        }
    }

    /**
     * insert a new event into the database
     */
    private Event insertEvent(Event event) {
        String sql = "INSERT INTO events (title, description, location, start_time, end_time, category, organizer_id, max_attendees) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getLocation());
            ps.setTimestamp(4, Timestamp.valueOf(event.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(event.getEndTime()));
            ps.setString(6, event.getCategory());
            ps.setInt(7, event.getOrganizer().getUserId());
            if (event.getMaxAttendees() != null) {
                ps.setInt(8, event.getMaxAttendees());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        if (rowsAffected == 0) {
             log.error("Failed to insert event, no rows affected. Title: {}", event.getTitle());
             throw new RuntimeException("Event insert failed for title: " + event.getTitle());
        }

        Number key = null;
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys.containsKey("event_id")) {
                key = (Number) keys.get("event_id");
            } else if (keyHolder.getKeyList().size() > 0) {
                Map<String, Object> firstKeyMap = keyHolder.getKeyList().get(0);
                if (firstKeyMap.containsKey("event_id")) {
                    key = (Number) firstKeyMap.get("event_id");
                }
            }
        }
        
        if (key == null && keyHolder.getKey() != null) {
            key = keyHolder.getKey();
        }

        if (key != null) {
            event.setEventId(key.intValue());
            log.info("Successfully inserted event with ID: {} and title: {}", event.getEventId(), event.getTitle());
            return findById(event.getEventId())
                   .orElseThrow(() -> new RuntimeException("Failed to fetch newly inserted event with ID: " + event.getEventId()));
        } else {
            log.error("Failed to retrieve generated key for inserted event: {}", event.getTitle());
            throw new RuntimeException("Failed to retrieve generated key for event: " + event.getTitle());
        }
    }

    /**
     * update an existing event
     */
    private Event updateEvent(Event event) {
        String sql = "UPDATE events SET title = ?, description = ?, location = ?, start_time = ?, end_time = ?, category = ?, organizer_id = ?, max_attendees = ? WHERE event_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
            event.getTitle(),
            event.getDescription(),
            event.getLocation(),
            Timestamp.valueOf(event.getStartTime()),
            Timestamp.valueOf(event.getEndTime()),
            event.getCategory(),
            event.getOrganizer().getUserId(),
            event.getMaxAttendees(),
            event.getEventId());

        if (rowsAffected == 0) {
             log.warn("Attempted to update event with ID {} but no rows were affected. Event might not exist.", event.getEventId());
        } else {
            log.info("Successfully updated event with ID: {}", event.getEventId());
        }

        return findById(event.getEventId())
               .orElseThrow(() -> new RuntimeException("Failed to fetch event after update with ID: " + event.getEventId()));
    }

    /**
     * delete an event by its id
     */
    public void deleteById(Integer eventId) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        log.debug("Executing SQL: {} with eventId: {}", sql, eventId);
        try {
            int rowsAffected = jdbcTemplate.update(sql, eventId);
             if (rowsAffected == 0) {
                log.warn("Attempted to delete event with ID {} but no rows were affected. Event might not exist.", eventId);
            } else {
                log.info("Successfully deleted event with ID: {}", eventId);
            }
        } catch (DataAccessException e) {
            log.error("Error deleting event with ID {}: {}", eventId, e.getMessage());
            throw e;
        }
    }
} 