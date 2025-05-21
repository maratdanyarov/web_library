package com.mdanyarov.weblibrary.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom thread-safe connection pool implementation.
 * Manages database connections for the application.
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;

    private final String url;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int connectionTimeout;

    private final BlockingQueue<Connection> connectionPool;
    private final BlockingQueue<Connection> usedConnections = new ArrayBlockingQueue<>(50);
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    /**
     * Creates a connection pool with the specified parameters.
     *
     * @param url               Database URL
     * @param username          Database username
     * @param password          Database password
     * @param maxPoolSize       Maximum number of connections in the pool
     * @param connectionTimeout Connection timeout in seconds
     * @throws SQLException if there is an error initializing the pool
     */
    public ConnectionPool(String url, String username, String password, int maxPoolSize,
                          int connectionTimeout) throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.connectionTimeout = connectionTimeout;
        this.connectionPool = new ArrayBlockingQueue<>(maxPoolSize);

        initializeConnectionPool();
    }

    /**
     * Gets the singleton instance of the connection pool.
     *
     * @param url         Database URL
     * @param username    Database username
     * @param password    Database password
     * @param maxPoolSize Maximum number of connections in the pool
     * @return The connection pool instance
     * @throws SQLException if there is an error initializing the pool
     */
    public static synchronized ConnectionPool getInstance(String url, String username,
                                                          String password, int maxPoolSize) throws SQLException {
        if (instance == null) {
            instance = new ConnectionPool(url, username, password, maxPoolSize, 30);
            logger.info("Connection pool initialized with max size: {}", maxPoolSize);
        }
        return instance;
    }

    /**
     * Initializes the connection pool with the specified number of connections.
     *
     * @throws SQLException if there is an error creating connections
     */
    private void initializeConnectionPool() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Could not load JDBC driver", e);
        }

        for (int i = 0; i < maxPoolSize; i++) {
            connectionPool.add(createConnection());
        }
    }

    /**
     * Creates a new database connection.
     *
     * @return A new database connection
     * @throws SQLException is there's an error creating the connection
     */
    private Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        activeConnections.incrementAndGet();
        return connection;
    }

    /**
     * Gets a connection from the pool.
     *
     * @return A database connection
     * @throws SQLException if there is an error getting a connection
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.poll(connectionTimeout, TimeUnit.SECONDS);
            if (connection == null) {
                logger.warn("Timeout waiting for connection. Active connections: {}", activeConnections.get());
                throw new SQLException("Timeout waiting for connection.");
            }

            if (!isConnectionValid(connection)) {
                logger.info("Connection invalid, creating new one.");
                connection = createConnection();
            }

            usedConnections.add(connection);
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }

    /**
     * Releases a connection back to the pool.
     *
     * @param connection The connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        usedConnections.remove(connection);
        try {
            if (!connection.isClosed() && isConnectionValid(connection)) {
                connectionPool.add(connection);
            } else {
                activeConnections.decrementAndGet();
                connectionPool.add(createConnection());
            }
        } catch (SQLException e) {
            logger.error("Error releasing connection", e);
            try {
                connection.close();
                activeConnections.decrementAndGet();
                connectionPool.add(createConnection());
            } catch (SQLException ex) {
                logger.error("Error closing bad connection", ex);
            }
        }
    }

    /**
     * Checks if a connection is valid.
     *
     * @param connection The connection to check
     * @return true if the connection is valid, false otherwise
     */
    private boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Connection validation failed", e);
            return false;
        }
    }

    /**
     * Gets the number of available connections in the pool.
     *
     * @return The number of available connections
     */
    public int getActiveConnectionsCount() {
        return activeConnections.get();
    }

    public int getAvailableConnectionsCount() {
        return connectionPool.size();
    }

    /**
     * Closes all connections in the pool.
     */
    public void shutdown() {
        try {
            for (Connection connection : usedConnections) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
            usedConnections.clear();

            for (Connection connection : connectionPool) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error closing available connection", e);
                }
            }
            connectionPool.clear();

            activeConnections.set(0);
            logger.info("Connection pool shutdown.");
        } catch (Exception e) {
            logger.error("Error shutting down connection pool", e);
        }
    }
}
