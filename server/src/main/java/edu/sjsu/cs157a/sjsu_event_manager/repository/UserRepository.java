package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException; // Base class for Spring JDBC exceptions
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
 * this class handles database operations for users using jdbc
 */
@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    // jdbctemplate simplifies jdbc operations
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // this maps a row from the database to a user object
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        try {
            user.setRole(User.Role.valueOf(rs.getString("role")));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role value found in database for user_id {}: {}", rs.getInt("user_id"), rs.getString("role"));
            user.setRole(User.Role.USER);
        }
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    };

    /**
     * get a user by their id
     */
    public Optional<User> findById(Integer userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        log.debug("Executing SQL: {} with userId: {}", sql, userId);
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No user found with userId: {}", userId);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding user by id {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * get a user by their username
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        log.debug("Executing SQL: {} with username: {}", sql, username);
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No user found with username: {}", username);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding user by username {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * get a user by their email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        log.debug("Executing SQL: {} with email: {}", sql, email);
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.trace("No user found with email: {}", email);
            return Optional.empty();
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * check if a user exists with the given username
     */
    public Boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        log.debug("Executing SQL: {} with username: {}", sql, username);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("Error accessing data while checking existence by username {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * check if a user exists with the given email
     */
    public Boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        log.debug("Executing SQL: {} with email: {}", sql, email);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("Error accessing data while checking existence by email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * get all users
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        log.debug("Executing SQL: {}", sql);
        try {
            return jdbcTemplate.query(sql, userRowMapper);
        } catch (DataAccessException e) {
            log.error("Error accessing data while finding all users: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * save a user (insert if new, update if it already has an id)
     */
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User to save cannot be null");
        }
        try {
            if (user.getUserId() == null) {
                log.debug("Creating new user with username: {}", user.getUsername());
                return insertUser(user);
            } else {
                log.debug("Updating user with ID: {}", user.getUserId());
                return updateUser(user);
            }
        } catch (DataAccessException e) {
            log.error("Error saving user {}: {}", (user.getUsername() != null ? user.getUsername() : "(no username)"), e.getMessage());
            throw e;
        }
    }

    /**
     * insert a new user into the database
     */
    private User insertUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setString(6, user.getRole() != null ? user.getRole().name() : User.Role.USER.name());
            return ps;
        }, keyHolder);

        if (rowsAffected == 0) {
            log.error("Failed to insert user, no rows affected.");
            throw new RuntimeException("User insert failed for username: " + user.getUsername());
        }

        // Extract user_id from the generated keys
        Number key = null;
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys.containsKey("user_id")) {
                key = (Number) keys.get("user_id");
            } else if (keyHolder.getKeyList().size() > 0) {
                Map<String, Object> firstKeyMap = keyHolder.getKeyList().get(0);
                if (firstKeyMap.containsKey("user_id")) {
                    key = (Number) firstKeyMap.get("user_id");
                }
            }
        }
        
        if (key == null && keyHolder.getKey() != null) {
            key = keyHolder.getKey();
        }

        if (key != null) {
            user.setUserId(key.intValue());
            log.info("Successfully inserted user with ID: {} and username: {}", user.getUserId(), user.getUsername());
            return findById(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Failed to fetch newly inserted user with ID: " + user.getUserId()));
        } else {
            log.error("Failed to retrieve generated key for inserted user: {}", user.getUsername());
            throw new RuntimeException("Failed to retrieve generated key for user: " + user.getUsername());
        }
    }

    /**
     * update an existing user
     */
    private User updateUser(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, first_name = ?, last_name = ?, role = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
            user.getUsername(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole() != null ? user.getRole().name() : User.Role.USER.name(),
            user.getUserId());

        if (rowsAffected == 0) {
            log.warn("Attempted to update user with ID {} but no rows were affected. User might not exist.", user.getUserId());
        } else {
            log.info("Successfully updated user with ID: {}", user.getUserId());
        }

        return findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Failed to fetch user after update with ID: " + user.getUserId()));
    }

    /**
     * delete a user by their id
     */
    public void deleteById(Integer userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        log.debug("Executing SQL: {} with userId: {}", sql, userId);
        try {
            int rowsAffected = jdbcTemplate.update(sql, userId);
            if (rowsAffected == 0) {
                log.warn("Attempted to delete user with ID {} but no rows were affected. User might not exist.", userId);
            } else {
                log.info("Successfully deleted user with ID: {}", userId);
            }
        } catch (DataAccessException e) {
            log.error("Error deleting user with ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    // note: more methods from jparepository like delete(user), count(), etc. need to be implemented if needed
} 