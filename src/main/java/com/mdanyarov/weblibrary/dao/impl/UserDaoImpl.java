package com.mdanyarov.weblibrary.dao.impl;

import com.mdanyarov.weblibrary.dao.UserDao;
import com.mdanyarov.weblibrary.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDao
 */
@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String FIND_BY_ID =
            "SELECT id, username, password, email, first_name, last_name, role, status, created_at, updated_at " +
                    "FROM users WHERE id = ?";

    private static final String FIND_ALL =
            "SELECT id, username, password, email, first_name, last_name, role, status, created_at, updated_at " +
                    "FROM users ORDER BY username";

    private static final String FIND_BY_USERNAME =
            "SELECT id, username, password, email, first_name, last_name, role, status, created_at, updated_at " +
                    "FROM users WHERE username = ?";

    private static final String FIND_BY_EMAIL =
            "SELECT id, username, password, email, first_name, last_name, role, status, created_at, updated_at " +
                    "FROM users WHERE email = ?";

    private static final String INSERT_USER =
            "INSERT INTO users (username, password, email, first_name, last_name, role, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_USER =
            "UPDATE users SET username = ?, email = ?, first_name = ?, last_name = ?, role = ?, status = ?, updated_at = ? " +
                    "WHERE id = ?";

    private static final String UPDATE_PASSWORD =
            "UPDATE users SET password = ?, updated_at = ? WHERE id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE users SET status = ?, updated_at = ? WHERE id = ?";

    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";

    /**
     * Finds a user by ID.
     *
     * @param id The ID of the user
     * @param connection Database connections to use
     * @return The entity, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public User findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all users.
     *
     * @param connection Database connection to use
     * @return A list of all entities
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<User> findAll(Connection connection) throws SQLException {
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Finds a user by username
     *
     * @param username The username to search for
     * @param connection Database connection to use
     * @return An optional containing the user if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Optional<User> findByUsername(String username, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_USERNAME)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @param connection Database connection to use
     * @return An Optional containing the user if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Optional<User> findByEmail(String email, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();


    }

    /**
     * Saves a user (creates or updates).
     *
     * @param user The user to save
     * @param connection Database connection to use
     * @return The saved entity with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public User save(User user, Connection connection) throws SQLException {
        if (user.getId() == null) {
            return insert(user, connection);
        } else {
            update(user, connection);
            return user;
        }
    }

    private User insert(User user, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getRole().name());
            stmt.setString(7, user.getStatus().name());
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    user.setCreatedAt(LocalDateTime.now());
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean update(User user, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_USER)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getStatus().name());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(8, user.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                user.setUpdatedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a user's password.
     *
     * @param user The user ID
     * @param password The new password
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean updatePassword(User user, String password, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PASSWORD)) {
            stmt.setString(1, password);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, user.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                user.setPassword(password);
                user.setUpdatedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    /**
     * Updates a user's status.
     *
     * @param userId The user ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean updateStatus(Long userId, User.UserStatus status, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STATUS)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a user by ID.
     *
     * @param id The ID of the user to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    @Override
    public boolean delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_USER)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps a ResultSet row to a User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setStatus(User.UserStatus.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}
