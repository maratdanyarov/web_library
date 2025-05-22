package com.mdanyarov.weblibrary.dao;

import com.mdanyarov.weblibrary.entity.Genre;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Genre entity
 */
public interface GenreDao extends GenericDao<Genre, Long> {

    /**
     * Finds a genre by name
     * @param name The genre name to search for
     * @param connection Database connection to use
     * @return An Optional containing the genre if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    Optional<Genre> findByName(String name, Connection connection) throws SQLException;

    /**
     * Finds genres for a specific book.
     *
     * @param bookId The Book ID
     * @param connection Database connection to use
     * @return A list of genres associated with the specified book
     * @throws SQLException if there is an error executing the query
     */
    List<Genre> findByBookId(Long bookId, Connection connection) throws SQLException;

    /**
     * Associates a genre with a book.
     *
     * @param bookId The book ID
     * @param genreId The genre ID
     * @param connection Database connection to use
     * @return true if the association was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean addGenreToBook(Long bookId, Long genreId, Connection connection) throws SQLException;

    /**
     * Removes a genre association from a book.
     * @param bookId The book ID
     * @param genreId The genre ID
     * @param connection Database connection to use
     * @return true if the removal was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean removeGenreFromBook(Long bookId, Long genreId, Connection connection) throws SQLException;
}
