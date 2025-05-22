package com.mdanyarov.weblibrary.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Utility class for handling database transactions.
 * Provides methods for starting, committing, and rolling back transactions.
 */
public class TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private TransactionManager(){}

    /**
     * Begins a transaction on the provided connection.
     *
     * @param connection The database connection
     * @throws SQLException if there is an error committing the transaction
     */
    public static void beginTransaction(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connection is null");
        }
        connection.setAutoCommit(false);
        logger.debug("Transaction started");
    }

    /**
     * Commits a transaction on the provided connection.
     *
     * @param connection The database connection
     * @throws SQLException if there is an error committing the transaction
     */
    public static void commitTransaction(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Connection is null");
        }
        connection.commit();
        connection.setAutoCommit(true);
        logger.debug("Transaction committed");
    }

    /**
     * Rolls back a transaction on the provided connection.
     *
     * @param connection The database connection
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection == null) {
            logger.warn("Cannot rollback transaction: connection is null");
            return;
        }

        try {
            connection.rollback();
            connection.setAutoCommit(true);
            logger.debug("Transaction rolled back");
        } catch (SQLException e) {
            logger.error("Error while rolling back transaction", e);
        }
    }

    /**
     * Executes a transaction with the provided callback.
     *
     * @param connectionPool The connection pool to get a connection from
     * @param callback The transaction callback to execute
     * @param <T> The return type of the callback
     * @return The result of the callback
     * @throws SQLException
     */
    public static <T> T executeTransaction(ConnectionPool connectionPool, TransactionCallback<T> callback) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            beginTransaction(connection);

            T result = callback.execute(connection);

            commitTransaction(connection);
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                rollbackTransaction(connection);
            }
            throw e;
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Functional interface for transaction callbacks.
     *
     * @param <T> The return type of the callback
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        /**
         * Executes the transaction callback.
         *
         * @param connection The database connection
         * @return The result of the callback
         * @throws SQLException if there is an error executing the callback
         */
        T execute(Connection connection) throws SQLException;
    }
}
