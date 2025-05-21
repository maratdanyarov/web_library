package com.mdanyarov.weblibrary.entity;


import java.time.LocalDateTime;

/**
 * Represents a physical copy of a book in the library.
 */
public class BookCopy {
    private Long id;
    private Book book;
    private String inventoryNumber;
    private CopyStatus status;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Enum representing possible statuses of a book copy.
     */
    public enum CopyStatus {
        AVAILABLE, ISSUED, RESERVED, DAMAGED, LOST
    }

    public BookCopy() {}

    public BookCopy(Long id, Book book, String inventoryNumber, CopyStatus status, String location) {
        this.id = id;
        this.book = book;
        this.inventoryNumber = inventoryNumber;
        this.status = status;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Checks if the copy is available for borrowing.
     *
     * @return true if the copy is available, false otherwise
     */
    public boolean isAvailable() {
        return this.status == CopyStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", inventoryNumber='" + inventoryNumber + '\'' +
                ", status=" + status +
                ", location='" + location + '\'' +
                '}';
    }
}
