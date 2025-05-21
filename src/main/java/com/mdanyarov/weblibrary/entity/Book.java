package com.mdanyarov.weblibrary.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a book in the library system.
 */
public class Book {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private String isbn;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Genre> genres = new ArrayList<>();
    private List<BookCopy> copies = new ArrayList<>();

    public Book() {}

    public Book(Long id, String title, String author, String publisher,
                int publicationYear, String isbn, String description) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    /**
     * Adds a genre to the book
     *
     * @param genre The genre to add
     */
    public void addGenre(Genre genre) {
        if (genres == null) {
            genres = new ArrayList<>();
        }
        if (!genres.contains(genre)) {
            genres.add(genre);
        }
    }

    /**
     * Adds a book copy to the book.
     *
     * @param copy The book copy to add
     */
    public void addCopy(BookCopy copy) {
        if (copies == null) {
            copies = new ArrayList<>();
        }
        if (!copies.contains(copy)) {
            copies.add(copy);
            copy.setBook(this);
        }
    }

    /**
     * Returns the count of available copies.
     *
     * @return Count of available copies
     */
    public int getAvailableCopiesCount() {
        if (copies == null) {
            return 0;
        }
        return (int) copies.stream()
                .filter(copy -> copy.getStatus() == BookCopy.CopyStatus.AVAILABLE)
                .count();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publicationYear=" + publicationYear +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}
