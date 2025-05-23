package com.mdanyarov.weblibrary.dao.impl;

import com.mdanyarov.weblibrary.dao.BookDao;
import com.mdanyarov.weblibrary.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of BookDao.
 */
@Repository
public class BookDaoImpl implements BookDao {
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);

    private static final String FIND_BY_ID =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books WHERE id = ?";

    private static final String FIND_ALL =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books ORDER BY title";

    private static final String FIND_BY_TITLE =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books WHERE LOWER(title) LIKE LOWER(?) ORDER BY title";

    private static final String FIND_BY_AUTHOR =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books WHERE LOWER(author) LIKE LOWER(?) ORDER BY author, title";

    private static final String FIND_BY_GENRE =
            "SELECT b.id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description, b.created_at, b.updated_at " +
                    "FROM books b JOIN book_genres bg ON b.id = bg.book_id WHERE bg.genre_id = ? ORDER BY b.title";

    private static final String FIND_BY_ISBN =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books WHERE isbn = ?";

    private static final String FIND_AVAILABLE =
            "SELECT DISTINCT b.id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description, b.created_at, b.updated_at " +
                    "FROM books b JOIN book_copies bc ON b.id = bc.book_id WHERE bc.status = 'AVAILABLE' ORDER BY b.title";

    private static final String FIND_WITH_PAGINATION =
            "SELECT id, title, author, publisher, publication_year, isbn, description, created_at, updated_at " +
                    "FROM books ORDER BY title LIMIT ? OFFSET ?";

    private static final String COUNT_ALL =
            "SELECT COUNT(*) FROM books";

    private static final String INSERT_BOOK =
            "INSERT INTO books (title, author, publisher, publication_year, isbn, description, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_BOOK =
            "UPDATE books SET title = ?, author = ?, publisher = ?, publication_year = ?, isbn = ?, description = ?, updated_at = ? " +
                    "WHERE id = ?";

    private static final String DELETE_BOOK =
            "DELETE FROM books WHERE id = ?";

    /**
     * Finds a book by ID.
     *
     * @param id The ID of the book
     * @param connection Database connections to use
     * @return The book, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Book findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all books.
     *
     * @param connection Database connection to use
     * @return A list of all books
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findAll(Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    /**
     * Finds books by title (partial match).
     * @param title The title to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findByTitle(String title, Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_TITLE)) {
            stmt.setString(1, "%" + title + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    /**
     * Finds books by author (partial march).
     * @param author The author to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findByAuthor(String author, Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_AUTHOR)) {
            stmt.setString(1, "%" + author + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    /**
     * Finds book by genre.
     * @param genreId The genre ID to search for
     * @param connection Database connection to use
     * @return A list of matching books
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findByGenre(Long genreId, Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_GENRE)) {
            stmt.setLong(1, genreId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    /**
     * Finds books by ISBN
     * @param isbn The ISBN to search for
     * @param connection Database connection to use
     * @return A list of matching books (should be 0 or 1)
     * @throws SQLException if there's an error executing the query
     */
    @Override
    public List<Book> findByIsbn(String isbn, Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ISBN)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    /**
     * Finds books with at least one available copy
     * @param connection Database connection to use
     * @return A list of books with available copies
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findAvailable(Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_AVAILABLE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    /**
     * Finds books with pagination
     * @param limit The maximym number of books to return
     * @param offset The number of books to skip
     * @param connection Database connection to use
     * @return A list of books limited by pagination parameters
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Book> findWithPagination(int limit, int offset, Connection connection) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_WITH_PAGINATION)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    /**
     * Counts the total number of books.
     * @param connection Database connection to use
     * @return The total number of books
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public int countAll(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(COUNT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Saves a book (creates or updates).
     *
     * @param book The book to save
     * @param connection Database connection to use
     * @return The saved book with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Book save(Book book, Connection connection) throws SQLException {
        if (book.getId() == null) {
            return insert(book, connection);
        } else {
            update(book, connection);
            return book;
        }
    }

    private Book insert(Book book, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setInt(4, book.getPublicationYear());
            stmt.setString(5, book.getIsbn());
            stmt.setString(6, book.getDescription());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getLong(1));
                    book.setCreatedAt(LocalDateTime.now());
                    return book;
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing book.
     *
     * @param book The book to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean update(Book book, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_BOOK)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setInt(4, book.getPublicationYear());
            stmt.setString(5, book.getIsbn());
            stmt.setString(6, book.getDescription());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(8, book.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                book.setUpdatedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a book by ID.
     *
     * @param id The ID of the book to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    @Override
    public boolean delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BOOK)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps a ResultSet row to a Book object.
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setIsbn(rs.getString("isbn"));
        book.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            book.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            book.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return book;
    }
}
