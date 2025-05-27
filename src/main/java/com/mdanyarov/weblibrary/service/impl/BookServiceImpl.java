package com.mdanyarov.weblibrary.service.impl;

import com.mdanyarov.weblibrary.dao.BookDao;
import com.mdanyarov.weblibrary.dao.ConnectionPool;
import com.mdanyarov.weblibrary.dao.TransactionManager;
import com.mdanyarov.weblibrary.entity.Book;
import com.mdanyarov.weblibrary.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of BookService.
 */
@Service
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookDao bookDao;
    private final ConnectionPool connectionPool;

    @Autowired
    public BookServiceImpl(BookDao bookDao, ConnectionPool connectionPool) {
        this.bookDao = bookDao;
        this.connectionPool = connectionPool;
    }

    /**
     * Finds a book by ID.
     *
     * @param id Book ID
     * @return Optional containing the book if found, empty otherwise
     * @throws Exception if there is an error finding the book
     */
    @Override
    public Optional<Book> findById(Long id) throws Exception {
        logger.debug("Finding book by ID: {}", id);

        try (Connection connection = connectionPool.getConnection()) {
            Book book = bookDao.findById(id, connection);
            return Optional.ofNullable(book);
        }
    }

    /**
     * Finds all books.
     *
     * @return List of all books
     * @throws Exception if there is an error finding books
     */
    @Override
    public List<Book> findAll() throws Exception {
        logger.debug("Finding all books");

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findAll(connection);
        }
    }

    /**
     * Searches books by title.
     *
     * @param title Title to search for (partial match)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    @Override
    public List<Book> searchByTitle(String title) throws Exception {
        logger.debug("Searching books by title: {}", title);

        if (title == null || title.trim().isEmpty()) {
            return findAll();
        }

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findByTitle(title.trim(), connection);
        }
    }

    /**
     * Searches books by author.
     *
     * @param author Author to search for (partial match)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    @Override
    public List<Book> searchByAuthor(String author) throws Exception {
        logger.debug("Searching books by author: {}", author);

        if (author == null || author.trim().isEmpty()) {
            return findAll();
        }

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findByAuthor(author.trim(), connection);
        }
    }

    /**
     * Searches books by ISBN.
     *
     * @param isbn ISBN to search for
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    @Override
    public List<Book> searchByIsbn(String isbn) throws Exception {
        logger.debug("Searching books by ISBN: {}", isbn);

        if (isbn == null || isbn.trim().isEmpty()) {
            return findAll();
        }

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findByIsbn(isbn.trim(), connection);
        }
    }

    /**
     * Finds books by genre.
     *
     * @param genreId Genre ID
     * @return List of books in the specified genre
     * @throws Exception if there is an error finding books
     */
    @Override
    public List<Book> findByGenre(Long genreId) throws Exception {
        logger.debug("Finding books by genre ID: {}", genreId);

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findByGenre(genreId, connection);
        }
    }

    /**
     * Finds books that have at least one available copy.
     *
     * @return List of available books
     * @throws Exception if there is an error finding books
     */
    @Override
    public List<Book> findAvailable() throws Exception {
        logger.debug("Finding available books");

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findAvailable(connection);
        }
    }

    /**
     * Finds books with pagination.
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of books for the specified page
     * @throws Exception if there is an error finding books
     */
    @Override
    public List<Book> findWithPagination(int page, int size) throws Exception {
        logger.debug("Finding books with pagination: page={}, size={}", page, size);

        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        int offset = page * size;

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.findWithPagination(size, offset, connection);
        }
    }

    /**
     * Gets the total count of books.
     *
     * @return Total number of books
     * @throws Exception if there is an error counting books
     */
    @Override
    public int getTotalCount() throws Exception {
        logger.debug("Getting total book count");

        try (Connection connection = connectionPool.getConnection()) {
            return bookDao.countAll(connection);
        }
    }

    /**
     * Searches books using multiple criteria.
     *
     * @param query Search query (title, author, or ISBN)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    @Override
    public List<Book> search(String query) throws Exception {
        logger.debug("Searching books with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        String trimmedQuery = query.trim();

        try (Connection connection = connectionPool.getConnection()) {
            // Search by title, author, and ISBN, then combine and deduplicate results
            List<Book> titleResults = bookDao.findByTitle(trimmedQuery, connection);
            List<Book> authorResults = bookDao.findByAuthor(trimmedQuery, connection);
            List<Book> isbnResults = bookDao.findByIsbn(trimmedQuery, connection);

            // Combine results and remove duplicates based on book ID
            return Stream.of(titleResults, authorResults, isbnResults)
                    .flatMap(List::stream)
                    .distinct()
                    .sorted((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()))
                    .toList();
        }
    }

    /**
     * Creates a new book.
     *
     * @param book Book to create
     * @return The created book with ID
     * @throws Exception if there is an error creating the book
     */
    @Override
    public Book createBook(Book book) throws Exception {
        logger.info("Creating new book: {}", book.getTitle());

        return TransactionManager.executeTransaction(connectionPool, connection -> {

            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Book title is required");
            }
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                throw new IllegalArgumentException("Book author is required");
            }

            // Check if ISBN already exists (if provided)
            if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
                List<Book> existingBooks = bookDao.findByIsbn(book.getIsbn(), connection);
                if (!existingBooks.isEmpty()) {
                    throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
                }
            }

            Book savedBook = bookDao.save(book, connection);
            logger.info("Book created successfully with ID: {}", savedBook.getId());
            return savedBook;
        });
    }

    /**
     * Updates an existing book.
     *
     * @param book Book with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the book
     */
    @Override
    public boolean updateBook(Book book) throws Exception {
        logger.info("Updating book: {}", book.getId());

        return TransactionManager.executeTransaction(connectionPool, connection -> {

            if (book.getId() == null) {
                throw new IllegalArgumentException("Book ID is required for update");
            }
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Book title is required");
            }
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                throw new IllegalArgumentException("Book author is required");
            }

            // Check if the book exists
            Book existingBook = bookDao.findById(book.getId(), connection);
            if (existingBook == null) {
                throw new IllegalArgumentException("Book not found with ID: " + book.getId());
            }

            boolean result = bookDao.update(book, connection);
            if (result) {
                logger.info("Book updated successfully: {}", book.getId());
            }
            return result;
        });
    }

    /**
     * Deletes a book by ID.
     *
     * @param id Book ID
     * @return true if the deletion was successful, false otherwise
     * @throws Exception if there is an error deleting the book
     */
    @Override
    public boolean deleteBook(Long id) throws Exception {
        logger.info("Deleting book: {}", id);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            // Check if the book exists
            Book existingBook = bookDao.findById(id, connection);
            if (existingBook == null) {
                throw new IllegalArgumentException("Book not found with ID: " + id);
            }

            // TODO: Check if book has active orders or copies

            boolean result = bookDao.delete(id, connection);
            if (result) {
                logger.info("Book deleted successfully: {}", id);
            }
            return result;
        });
    }
}