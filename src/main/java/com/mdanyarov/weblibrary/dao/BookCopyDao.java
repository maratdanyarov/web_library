package com.mdanyarov.weblibrary.dao;

import com.mdanyarov.weblibrary.entity.BookCopy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for BookCopy entity
 */
public interface BookCopyDao extends BasicDao<BookCopy, Long> {

    /**
     * Finds book copies by book ID.
     *
     * @param bookId The book ID
     * @param connection Database connection to use
     * @return A list of book copies for the specified book
     * @throws SQLException if there is an error executing the query
     */
    List<BookCopy> findByBookId(Long bookId, Connection connection) throws SQLException;

    /**
     * Finds book copies by inventory number.
     *
     * @param inventoryNumber The inventory number to search for
     * @param connection Database connection to use
     * @return An Optional containing the book copy if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    Optional<BookCopy> findByInventoryNumber(String inventoryNumber, Connection connection) throws SQLException;

    /**
     * Finds available book copies for a specific book.
     *
     * @param bookId The book ID
     * @param connection Database connection to use
     * @return A list of available book copies for the specified book
     * @throws SQLException if there is an error executing the query
     */
    List<BookCopy> findAvailableByBookId(Long bookId, Connection connection) throws SQLException;

    /**
     * Updates the status of a book copy.
     * @param bookCopyId The book copy ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean updateStatus(Long bookCopyId, BookCopy.CopyStatus status, Connection connection) throws SQLException;
}
