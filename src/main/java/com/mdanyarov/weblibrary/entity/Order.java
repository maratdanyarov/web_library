package com.mdanyarov.weblibrary.entity;

import java.time.LocalDateTime;

/**
 * Represents a book loan order in the library system.
 */
public class Order {
    private Long id;
    private User user;
    private BookCopy bookCopy;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private LocalDateTime approvalDate;
    private LocalDateTime returnDate;
    private LocalDateTime actualReturnDate;
    private User processedBy;
    private String notes;

    /**
     * Enum representing possible order types.
     */
    public enum OrderType {
        HOME, READING_ROOM
    }

    /**
     * Enum representing possible order statuses.
     */
    public enum OrderStatus {
        PENDING, APPROVED, REJECTED, RETURNED, CANCELLED
    }

    public Order() {}

    public Order(Long id, User user, BookCopy bookCopy, OrderType orderType, OrderStatus orderStatus) {
        this.id = id;
        this.user = user;
        this.bookCopy = bookCopy;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvalDate;
    }

    public void setApprovedDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDateTime getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDateTime actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public User getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(User processedBy) {
        this.processedBy = processedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Checks if the order is overdue.
     *
     * @return true if the return date has passed and the order is still approved, false otherwise
     */
    public boolean isOverdue() {
        return this.orderStatus == OrderStatus.APPROVED &&
                returnDate != null &&
                LocalDateTime.now().isAfter(returnDate) &&
                actualReturnDate == null;
    }

    /**
     * Approves the order.
     *
     * @param librarian The librarian approving the order
     * @param returnDate The expected return date
     */
    public void approve(User librarian, LocalDateTime returnDate) {
        if (this.orderStatus == OrderStatus.PENDING) {
            this.orderStatus = OrderStatus.APPROVED;
            this.processedBy = librarian;
            this.approvalDate = LocalDateTime.now();
            this.returnDate = returnDate;
            if (bookCopy != null) {
                bookCopy.setStatus(BookCopy.CopyStatus.ISSUED);
            }
        }
    }

    /**
     * Rejects the order.
     *
     * @param librarian The librarian rejecting the order
     * @param notes The reason for rejection
     */
    public void reject(User librarian, String notes) {
        if (this.orderStatus == OrderStatus.PENDING) {
            this.orderStatus = OrderStatus.REJECTED;
            this.processedBy = librarian;
            this.approvalDate = LocalDateTime.now();
            this.notes = notes;
            if (this.bookCopy != null && bookCopy.getStatus() == BookCopy.CopyStatus.RESERVED) {
                bookCopy.setStatus(BookCopy.CopyStatus.AVAILABLE);
            }
        }
    }

    /**
     * Marks the order as returned.
     */
    public void returnBook() {
        if (this.orderStatus == OrderStatus.APPROVED) {
            this.orderStatus = OrderStatus.RETURNED;
            this.actualReturnDate = LocalDateTime.now();
            if (bookCopy != null) {
                bookCopy.setStatus(BookCopy.CopyStatus.AVAILABLE);
            }
        }
    }

    /**
     * Cancels the order.
     */
    public void cancel() {
        if (this.orderStatus == OrderStatus.PENDING) {
            this.orderStatus = OrderStatus.CANCELLED;
            if (this.bookCopy != null && this.bookCopy.getStatus() == BookCopy.CopyStatus.RESERVED) {
                bookCopy.setStatus(BookCopy.CopyStatus.AVAILABLE);
            }
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", bookCopy=" + (bookCopy != null ? bookCopy.getInventoryNumber() : "null") +
                ", orderType=" + orderType +
                ", status=" + orderStatus +
                ", orderDate=" + orderDate +
                ", returnDate=" + returnDate +
                '}';
    }
}
