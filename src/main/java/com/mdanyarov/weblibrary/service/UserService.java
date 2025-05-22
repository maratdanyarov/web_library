package com.mdanyarov.weblibrary.service;

import com.mdanyarov.weblibrary.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user operations.
 */
public interface UserService {

    /**
     * Register a new user.
     *
     * @param username Username
     * @param password Plain text password that will be encoded
     * @param email Email address
     * @param firstName First name
     * @param lastName Last name
     * @return The created user
     * @throws Exception if there is an error creating the user
     */
    User registerUser(String username, String password, String email, String firstName, String lastName) throws Exception;

    /**
     * Authenticates a user.
     *
     * @param username Username
     * @param password Plain text password (will be verified against encoded password)
     * @return Optional containing the user if authentication was successful, empty otherwise
     * @throws Exception if there is an error during authentication
     */
    Optional<User> authenticate(String username, String password) throws Exception;

    /**
     * Finds a user ID.
     *
     * @param id User ID
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    Optional<User> findById(Long id) throws Exception;

    /**
     * Finds a user by username.
     *
     * @param username Username
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    Optional<User> findByUsername(String username) throws Exception;

    /**
     * Finds a user by email.
     *
     * @param email Email address
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    Optional<User> findByEmail(String email) throws Exception;

    /**
     * Updates a user's information.
     *
     * @param user User with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the user
     */
    boolean updateUser(User user) throws Exception;

    /**
     * Changes a user's password.
     *
     * @param userId User ID
     * @param newPassword New plain text password that will be hashed
     * @return true if the password change was successful, false otherwise
     * @throws Exception if there is an error changing the password
     */
    boolean changePassword(Long userId, String newPassword) throws Exception;

    /**
     * Updates a user's status.
     *
     * @param userId User ID
     * @param status New status
     * @return true if the status update was successful, false otherwise
     * @throws Exception if there is an error updating status
     */
    boolean updateStatus(Long userId, User.UserStatus status) throws Exception;

    /**
     * Finds all users.
     *
     * @return List of all users
     * @throws Exception if there is an error finding users
     */
    List<User> findAll() throws Exception;

    /**
     * Finds users by role.
     *
     * @param role User role
     * @return List of users with the specified role
     * @throws Exception if there is an error finding users
     */
    List<User> findByRole(User.UserRole role) throws Exception;
}
