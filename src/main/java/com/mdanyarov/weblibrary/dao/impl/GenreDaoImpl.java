package com.mdanyarov.weblibrary.dao.impl;

import com.mdanyarov.weblibrary.dao.GenreDao;
import com.mdanyarov.weblibrary.entity.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of GenreDao
 */
@Repository
public class GenreDaoImpl implements GenreDao {
    private static final Logger logger = LoggerFactory.getLogger(GenreDaoImpl.class);

    private static final String FIND_BY_ID =
            "SELECT id, name, created_at FROM genres WHERE id = ?";

    private static final String FIND_ALL =
            "SELECT id, name, created_at FROM genres ORDER BY name";

    private static final String FIND_BY_NAME =
            "SELECT id, name, created_at FROM genres WHERE name = ?";

    private static final String FIND_BY_BOOK_ID =
            "SELECT g.id, g.name, g.created_at FROM genres g " +
                    "JOIN book_genres bg ON g.id = bg.genre_id WHERE bg.book_id = ? ORDER BY g.name";

    private static final String INSERT_GENRE =
            "INSERT INTO genres (name, created_at) VALUES (?, ?)";

    private static final String UPDATE_GENRE =
            "UPDATE genres SET name = ? WHERE id = ?";

    private static final String DELETE_GENRE =
            "DELETE FROM genres WHERE id = ?";

    private static final String ADD_GENRE_TO_BOOK =
            "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";

    private static final String REMOVE_GENRE_FROM_BOOK =
            "DELETE FROM book_genres WHERE book_id = ? AND genre_id = ?";

    private static final String CHECK_BOOK_GENRE_EXISTS =
            "SELECT 1 FROM book_genres WHERE book_id = ? AND genre_id = ?";

    /**
     * Finds a genre by ID.
     *
     * @param id The ID of the genre
     * @param connection Database connections to use
     * @return The entity, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Genre findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGenre(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all genres.
     *
     * @param connection Database connection to use
     * @return A list of all genres
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Genre> findAll(Connection connection) throws SQLException {
        List<Genre> genres = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(mapResultSetToGenre(rs));
                }
            }
        }
        return genres;
    }

    /**
     * Finds a genre by name
     * @param name The genre name to search for
     * @param connection Database connection to use
     * @return An Optional containing the genre if found, or empty if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Optional<Genre> findByName(String name, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGenre(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds genres for a specific book.
     *
     * @param bookId The Book ID
     * @param connection Database connection to use
     * @return A list of genres associated with the specified book
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Genre> findByBookId(Long bookId, Connection connection) throws SQLException {
        List<Genre> genres = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_BOOK_ID)) {
            stmt.setLong(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(mapResultSetToGenre(rs));
                }
            }
        }
        return genres;
    }

    /**
     * Saves a genre (creates or updates).
     *
     * @param genre The genre to save
     * @param connection Database connection to use
     * @return The saved genre with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Genre save(Genre genre, Connection connection) throws SQLException {
        if (genre.getId() == null) {
            return insert(genre, connection);
        } else {
            update(genre, connection);
            return genre;
        }
    }

    private Genre insert(Genre genre, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_GENRE, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, genre.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating genre failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    genre.setId(generatedKeys.getLong(1));
                    return genre;
                } else {
                    throw new SQLException("Creating genre failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing genre.
     *
     * @param genre The genre to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean update(Genre genre, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_GENRE)) {
            stmt.setString(1, genre.getName());
            stmt.setLong(2, genre.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a genre by ID.
     *
     * @param id The ID of the genre to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    @Override
    public boolean delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_GENRE)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Associates a genre with a book.
     *
     * @param bookId The book ID
     * @param genreId The genre ID
     * @param connection Database connection to use
     * @return true if the association was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean addGenreToBook(Long bookId, Long genreId, Connection connection) throws SQLException {
        try (PreparedStatement checkStmt = connection.prepareStatement(CHECK_BOOK_GENRE_EXISTS)) {
            checkStmt.setLong(1, bookId);
            checkStmt.setLong(2, genreId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(ADD_GENRE_TO_BOOK)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, genreId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Removes a genre association from a book.
     * @param bookId The book ID
     * @param genreId The genre ID
     * @param connection Database connection to use
     * @return true if the removal was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean removeGenreFromBook(Long bookId, Long genreId, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(REMOVE_GENRE_FROM_BOOK)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, genreId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Genre mapResultSetToGenre(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}
