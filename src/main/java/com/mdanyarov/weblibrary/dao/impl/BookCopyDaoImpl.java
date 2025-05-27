package com.mdanyarov.weblibrary.dao.impl;

import com.mdanyarov.weblibrary.dao.BookCopyDao;
import com.mdanyarov.weblibrary.entity.Book;
import com.mdanyarov.weblibrary.entity.BookCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of BookCopyDao
 */
@Repository
public class BookCopyDaoImpl implements BookCopyDao {
    private static final Logger logger = LoggerFactory.getLogger(BookCopyDaoImpl.class);

    private static final String FIND_BY_ID =
            "SELECT bc.id, bc.book_id, bc.inventory_number, bc.status, bc.location, bc.created_at, bc.updated_at, " +
                    "b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description " +
                    "FROM book_copies bc JOIN books b ON bc.book_id = b.id WHERE bc.id = ?";

    private static final String FIND_ALL =
            "SELECT bc.id, bc.book_id, bc.inventory_number, bc.status, bc.location, bc.created_at, bc.updated_at, " +
                    "b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description " +
                    "FROM book_copies bc JOIN books b ON bc.book_id = b.id ORDER BY bc.inventory_number";

    private static final String FIND_BY_BOOK_ID =
            "SELECT bc.id, bc.book_id, bc.inventory_number, bc.status, bc.location, bc.created_at, bc.updated_at, " +
                    "b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description " +
                    "FROM book_copies bc JOIN books b ON bc.book_id = b.id WHERE bc.book_id = ? ORDER BY bc.inventory_number";

    private static final String FIND_BY_INVENTORY_NUMBER =
            "SELECT bc.id, bc.book_id, bc.inventory_number, bc.status, bc.location, bc.created_at, bc.updated_at, " +
                    "b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description " +
                    "FROM book_copies bc JOIN books b ON bc.book_id = b.id WHERE bc.inventory_number = ?";

    private static final String FIND_AVAILABLE_BY_BOOK_ID =
            "SELECT bc.id, bc.book_id, bc.inventory_number, bc.status, bc.location, bc.created_at, bc.updated_at, " +
                    "b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description " +
                    "FROM book_copies bc JOIN books b ON bc.book_id = b.id " +
                    "WHERE bc.book_id = ? AND bc.status = 'AVAILABLE' ORDER BY bc.inventory_number";

    private static final String INSERT_BOOK_COPY =
            "INSERT INTO book_copies (book_id, inventory_number, status, location, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_BOOK_COPY =
            "UPDATE book_copies SET book_id = ?, inventory_number = ?, status = ?, location = ?, updated_at = ? " +
                    "WHERE id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE book_copies SET status = ?, updated_at = ? WHERE id = ?";

    private static final String DELETE_BOOK_COPY =
            "DELETE FROM book_copies WHERE id = ?";

    /**
     * Finds a BookCopy by ID.
     *
     * @param id The ID of the BookCopy
     * @param connection Database connections to use
     * @return The book copy, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public BookCopy findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookCopy(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all book copies.
     *
     * @param connection Database connection to use
     * @return A list of all book copies
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<BookCopy> findAll(Connection connection) throws SQLException {
        List<BookCopy> bookCopies = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookCopies.add(mapResultSetToBookCopy(rs));
                }
            }
        }
        return bookCopies;
    }

    /**
     * Finds book copies by book ID.
     *
     * @param bookId The book ID
     * @param connection Database connection to use
     * @return A list of book copies for the specified book
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<BookCopy> findByBookId(Long bookId, Connection connection) throws SQLException {
        List<BookCopy> bookCopies = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_BOOK_ID)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookCopies.add(mapResultSetToBookCopy(rs));
                }
            }
        }
        return bookCopies;
    }

    /**
     * Finds book copies by inventory number.
     *
     * @param inventoryNumber The inventory number to search for
     * @param connection Database connection to use
     * @return An Optional containing the book copy if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Optional<BookCopy> findByInventoryNumber(String inventoryNumber, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_INVENTORY_NUMBER)) {
            stmt.setString(1, inventoryNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBookCopy(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds available book copies for a specific book.
     *
     * @param bookId The book ID
     * @param connection Database connection to use
     * @return A list of available book copies for the specified book
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<BookCopy> findAvailableByBookId(Long bookId, Connection connection) throws SQLException {
        List<BookCopy> bookCopies = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_AVAILABLE_BY_BOOK_ID)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookCopies.add(mapResultSetToBookCopy(rs));
                }
            }
        }
        return bookCopies;
    }

    /**
     * Saves a book copy (creates or updates).
     *
     * @param bookCopy The entity to save
     * @param connection Database connection to use
     * @return The saved book copy with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public BookCopy save(BookCopy bookCopy, Connection connection) throws SQLException {
        if (bookCopy.getId() == null) {
            return insert(bookCopy, connection);
        } else {
            update(bookCopy, connection);
            return bookCopy;
        }
    }

    private BookCopy insert(BookCopy bookCopy, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_BOOK_COPY, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, bookCopy.getBook().getId());
            stmt.setString(2, bookCopy.getInventoryNumber());
            stmt.setString(3, bookCopy.getStatus().name());
            stmt.setString(4, bookCopy.getLocation());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating book copies failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bookCopy.setId(generatedKeys.getLong(1));
                    return bookCopy;
                } else {
                    throw new SQLException("Creating book copies failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing book copy.
     *
     * @param bookCopy The book copy to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean update(BookCopy bookCopy, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_BOOK_COPY)) {
            stmt.setLong(1, bookCopy.getBook().getId());
            stmt.setString(2, bookCopy.getInventoryNumber());
            stmt.setString(3, bookCopy.getStatus().name());
            stmt.setString(4, bookCopy.getLocation());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(6, bookCopy.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the status of a book copy.
     * @param bookCopyId The book copy ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean updateStatus(Long bookCopyId, BookCopy.CopyStatus status, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STATUS)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, bookCopyId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a book copy by ID.
     *
     * @param id The ID of the book copy to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    @Override
    public boolean delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BOOK_COPY)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps a ResultSet row to a BookCopy object with associated Book information.
     */
    private BookCopy mapResultSetToBookCopy(ResultSet rs) throws SQLException {
        BookCopy bookCopy = new BookCopy();
        bookCopy.setId(rs.getLong(1));
        bookCopy.setInventoryNumber(rs.getString("inventory_number"));
        bookCopy.setStatus(BookCopy.CopyStatus.valueOf(rs.getString("status")));
        bookCopy.setLocation(rs.getString("location"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            bookCopy.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            bookCopy.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setIsbn(rs.getString("isbn"));
        book.setDescription(rs.getString("description"));

        bookCopy.setBook(book);

        return bookCopy;
    }
}
