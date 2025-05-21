package com.mdanyarov.weblibrary.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Generic DAO interface for CRUD operations.
 * @param <T>
 * @param <K>
 */
public interface GenericDao<T, K> {

    /**
     * Finds an entity by ID.
     *
     * @param id The ID of the entity
     * @param connection Database connections to use
     * @return The entity, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    T findById(K id, Connection connection) throws SQLException;

    /**
     * Finds all entites.
     *
     * @param connection Database connection to use
     * @return A list of all entities
     * @throws SQLException if there is an error executing the query
     */
    List<T> findAll(Connection connection) throws SQLException;

    /**
     * Saves an entity (creqtes or updates).
     *
     * @param entity The entity to save
     * @param connection Database connection to use
     * @return The saved entity with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    T save(T entity, Connection connection) throws SQLException;

    /**
     * Updates an existing entity.
     *
     * @param entity The entity to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean update(T entity, Connection connection) throws SQLException;

    /**
     * Deletes an entity by ID.
     *
     * @param id The ID of the entity to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    boolean delete(K id, Connection connection) throws SQLException;
}
