package com.mdanyarov.weblibrary.service;

import com.mdanyarov.weblibrary.entity.Book;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for book operations.
 */
public interface BookService {

    /**
     * Finds a book by ID.
     *
     * @param id Book ID
     * @return Optional containing the book if found, empty otherwise
     * @throws Exception if there is an error finding the book
     */
    Optional<Book> findById(Long id) throws Exception;

    /**
     * Finds all books.
     *
     * @return List of all books
     * @throws Exception if there is an error finding books
     */
    List<Book> findAll() throws Exception;

    /**
     * Searches books by title.
     *
     * @param title Title to search for (partial match)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    List<Book> searchByTitle(String title) throws Exception;

    /**
     * Searches books by author.
     *
     * @param author Author to search for (partial match)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    List<Book> searchByAuthor(String author) throws Exception;

    /**
     * Searches books by ISBN.
     *
     * @param isbn ISBN to search for
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    List<Book> searchByIsbn(String isbn) throws Exception;

    /**
     * Finds books by genre.
     *
     * @param genreId Genre ID
     * @return List of books in the specified genre
     * @throws Exception if there is an error finding books
     */
    List<Book> findByGenre(Long genreId) throws Exception;

    /**
     * Finds books that have at least one available copy.
     *
     * @return List of available books
     * @throws Exception if there is an error finding books
     */
    List<Book> findAvailable() throws Exception;

    /**
     * Finds books with pagination.
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of books for the specified page
     * @throws Exception if there is an error finding books
     */
    List<Book> findWithPagination(int page, int size) throws Exception;

    /**
     * Gets the total count of books.
     *
     * @return Total number of books
     * @throws Exception if there is an error counting books
     */
    int getTotalCount() throws Exception;

    /**
     * Searches books using multiple criteria.
     *
     * @param query Search query (title, author, or ISBN)
     * @return List of matching books
     * @throws Exception if there is an error searching books
     */
    List<Book> search(String query) throws Exception;

    /**
     * Creates a new book.
     *
     * @param book Book to create
     * @return The created book with ID
     * @throws Exception if there is an error creating the book
     */
    Book createBook(Book book) throws Exception;

    /**
     * Updates an existing book.
     *
     * @param book Book with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the book
     */
    boolean updateBook(Book book) throws Exception;

    /**
     * Deletes a book by ID.
     *
     * @param id Book ID
     * @return true if the deletion was successful, false otherwise
     * @throws Exception if there is an error deleting the book
     */
    boolean deleteBook(Long id) throws Exception;
}