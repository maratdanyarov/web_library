package com.mdanyarov.weblibrary.dao;

import com.mdanyarov.weblibrary.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDao extends GenericDao<User, Long> {

    /**
     * Finds a user by username
     *
     * @param username The username to search for
     * @param connection Database connection to use
     * @return An optional containing the user if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    Optional<User> findByUsername(String username, Connection connection) throws SQLException;

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @param connection Database connection to use
     * @return An Optional containing the user if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    Optional<User> findByEmail(String email, Connection connection) throws SQLException;

    /**
     * Updates a user's password.
     *
     * @param user The user ID
     * @param password The new password
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean updatePassword(User user, String password, Connection connection) throws SQLException;

    /**
     * Updates a user's status.
     *
     * @param userId The user ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean updateStatus(Long userId, User.UserStatus status, Connection connection) throws SQLException;
}
