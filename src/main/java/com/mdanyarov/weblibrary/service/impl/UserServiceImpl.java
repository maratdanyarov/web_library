package com.mdanyarov.weblibrary.service.impl;

import com.mdanyarov.weblibrary.dao.ConnectionPool;
import com.mdanyarov.weblibrary.dao.TransactionManager;
import com.mdanyarov.weblibrary.dao.UserDao;
import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final ConnectionPool connectionPool;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserDao userDao, ConnectionPool connectionPool, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.connectionPool = connectionPool;
        this.passwordEncoder = passwordEncoder;
    }

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
    @Override
    public User registerUser(String username, String password, String email, String firstName, String lastName) throws Exception {
        logger.info("Registering new user: {}", username);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            Optional<User> existingUser = userDao.findByUsername(username, connection);
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("Username is already in use: " + username);
            }

            Optional<User> existingEmail = userDao.findByEmail(email, connection);
            if (existingEmail.isPresent()) {
                throw new IllegalArgumentException("Email is already in use: " + email);
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(User.UserRole.READER);
            user.setStatus(User.UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userDao.save(user, connection);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
        });
    }

    /**
     * Authenticates a user.
     *
     * @param username Username
     * @param password Plain text password (will be verified against encoded password)
     * @return Optional containing the user if authentication was successful, empty otherwise
     * @throws Exception if there is an error during authentication
     */
    @Override
    public Optional<User> authenticate(String username, String password) throws Exception {
        logger.debug("Authenticating user: {}", username);

        try (Connection connection = connectionPool.getConnection()) {
            Optional<User> userOptional = userDao.findByUsername(username, connection);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (user.getStatus() == User.UserStatus.BLOCKED) {
                    logger.warn("Blocked user attempted login: {}", username);
                    return Optional.empty();
                }

                if (passwordEncoder.matches(password, user.getPassword())) {
                    logger.info("User authenticated successfully: {}", username);
                    return Optional.of(user);
                } else {
                    logger.warn("User password does not match: {}", username);
                }
            } else {
                logger.warn("User not found: {}", username);
            }

            return Optional.empty();
        }
    }

    /**
     * Finds a user ID.
     *
     * @param id User ID
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    @Override
    public Optional<User> findById(Long id) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            User user = userDao.findById(id, connection);
            return Optional.ofNullable(user);
        }
    }

    /**
     * Finds a user by username.
     *
     * @param username Username
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    @Override
    public Optional<User> findByUsername(String username) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return userDao.findByUsername(username, connection);
        }
    }

    /**
     * Finds a user by email.
     *
     * @param email Email address
     * @return Optional containing the user if found, empty otherwise
     * @throws Exception if there is an error finding the user
     */
    @Override
    public Optional<User> findByEmail(String email) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return userDao.findByEmail(email, connection);
        }
    }

    /**
     * Updates a user's information.
     *
     * @param user User with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the user
     */
    @Override
    public boolean updateUser(User user) throws Exception {
        logger.info("Updating user: {}", user.getUsername());

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            user.setUpdatedAt(LocalDateTime.now());
            boolean result = userDao.update(user, connection);
            if (result) {
                logger.info("User updated successfully: {}", user.getUsername());
            }
            return result;
        });
    }

    /**
     * Changes a user's password.
     *
     * @param userId User ID
     * @param newPassword New plain text password that will be hashed
     * @return true if the password change was successful, false otherwise
     * @throws Exception if there is an error changing the password
     */
    @Override
    public boolean changePassword(Long userId, String newPassword) throws Exception {
        logger.info("Changing password for user ID: {}", userId);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            User user = userDao.findById(userId, connection);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            String encodedPassword = passwordEncoder.encode(newPassword);
            boolean result = userDao.updatePassword(user, encodedPassword, connection);
            if (result) {
                logger.info("Password changed successfully for user: {}", user.getUsername());
            }
            return result;
        });
    }

    /**
     * Updates a user's status.
     *
     * @param userId User ID
     * @param status New status
     * @return true if the status update was successful, false otherwise
     * @throws Exception if there is an error updating status
     */
    @Override
    public boolean updateStatus(Long userId, User.UserStatus status) throws Exception {
        logger.info("Updating status for user ID {} to {}", userId, status);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            boolean result = userDao.updateStatus(userId, status, connection);
            if (result) {
                logger.info("Status updated successfully for user ID: {}", userId);
            }
            return result;
        });
    }

    /**
     * Finds all users.
     *
     * @return List of all users
     * @throws Exception if there is an error finding users
     */
    @Override
    public List<User> findAll() throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return userDao.findAll(connection);
        }
    }

    /**
     * Finds users by role.
     *
     * @param role User role
     * @return List of users with the specified role
     * @throws Exception if there is an error finding users
     */
    @Override
    public List<User> findByRole(User.UserRole role) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            List<User> users = userDao.findAll(connection);
            return users.stream()
                    .filter(user -> user.getRole() == role)
                    .toList();
        }
    }
}
