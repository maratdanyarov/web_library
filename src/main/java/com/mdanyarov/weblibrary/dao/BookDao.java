package com.mdanyarov.weblibrary.dao;

import com.mdanyarov.weblibrary.entity.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Book entity
 */
public interface BookDao extends BasicDao<Book, Long> {

    /**
     * Finds books by title (partial match).
     * @param title The title to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    List<Book> findByTitle(String title, Connection connection) throws SQLException;

    /**
     * Finds books by author (partial march).
     * @param author The author to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    List<Book> findByAuthor(String author, Connection connection) throws SQLException;

    /**
     * Finds book by genre.
     * @param genreId The genre ID to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    List<Book> findByGenre(Long genreId, Connection connection) throws SQLException;

    /**
     * Finds books by ISBN
     * @param isbn The ISBN to search for
     * @param connection Database connection to use
     * @return A list of matching books (should be 0 or 1)
     * @throws SQLException if there's an error executing the query
     */
    List<Book> findByIsbn(String isbn, Connection connection) throws SQLException;

    /**
     * Finds books with at least one available copy
     * @param connection Database connection to use
     * @return A list of books with available copies
     * @throws SQLException if there is an error executing the query
     */
    List<Book> findAvailable(Connection connection) throws SQLException;

    /**
     * Finds books with pagination
     * @param limit The maximym number of books to return
     * @param offset The number of books to skip
     * @param connection Database connection to use
     * @return A list of books limited by pagination parameters
     * @throws SQLException if there is an error executing the query
     */
    List<Book> findWithPagination(int limit, int offset, Connection connection) throws SQLException;

    /**
     * Counts the total number of books.
     * @param connection Database connection to use
     * @return The total number of books
     * @throws SQLException if there is an error executing the query
     */
    int countAll(Connection connection) throws SQLException;
}
