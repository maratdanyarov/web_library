package com.mdanyarov.weblibrary.service;

import com.mdanyarov.weblibrary.entity.Order;
import com.mdanyarov.weblibrary.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for order operations.
 */
public interface OrderService {
    /**
     * Finds an order by ID.
     *
     * @param id Order ID
     * @return Optional containing the order if found, empty otherwise
     * @throws Exception if there is an error finding the order
     */
    Optional<Order> findById(Long id) throws Exception;

    /**
     * Finds all orders.
     *
     * @return List of all orders
     * @throws Exception if there is an error finding orders
     */
    List<Order> findAll() throws Exception;

    /**
     * Finds orders by user ID.
     *
     * @param userId User ID
     * @return List of orders for the specified user
     * @throws Exception if there is an error finding orders
     */
    List<Order> findByUserId(Long userId) throws Exception;

    /**
     * Finds active orders (PENDING or APPROVED) by user ID.
     *
     * @param userId User ID
     * @return List of active orders for the specified user
     * @throws Exception if there is an error finding orders
     */
    List<Order> findActiveByUserId(Long userId) throws Exception;

    /**
     * Finds orders by status.
     *
     * @param status Order status
     * @return List of orders with the specified status
     * @throws Exception if there is an error finding orders
     */
    List<Order> findByStatus(Order.OrderStatus status) throws Exception;

    /**
     * Finds overdue orders.
     *
     * @return List of overdue orders
     * @throws Exception if there is an error finding orders
     */
    List<Order> findOverdue() throws Exception;

    /**
     * Creates a new book order.
     *
     * @param userId User ID requesting the book
     * @param bookCopyId Book copy ID
     * @param orderType Order type (HOME or READING_ROOM)
     * @param notes Optional notes
     * @return The created order
     * @throws Exception if there is an error creating the order
     */
    Order createOrder(Long userId, Long bookCopyId, Order.OrderType orderType, String notes) throws Exception;

    /**
     * Approves an order.
     *
     * @param orderId Order ID
     * @param librarian Librarian approving the order
     * @param returnDate Expected return date
     * @return true if the approval was successful, false otherwise
     * @throws Exception if there is an error approving the order
     */
    boolean approveOrder(Long orderId, User librarian, java.time.LocalDateTime returnDate) throws Exception;

    /**
     * Rejects an order.
     *
     * @param orderId Order ID
     * @param librarian Librarian rejecting the order
     * @param notes Reason for rejection
     * @return true if the rejection was successful, false otherwise
     * @throws Exception if there is an error rejecting the order
     */
    boolean rejectOrder(Long orderId, User librarian, String notes) throws Exception;

    /**
     * Marks an order as returned.
     *
     * @param orderId Order ID
     * @return true if the return was successful, false otherwise
     * @throws Exception if there is an error processing the return
     */
    boolean returnOrder(Long orderId) throws Exception;

    /**
     * Cancels an order.
     *
     * @param orderId Order ID
     * @param userId User ID (must match the order's user)
     * @return true if the cancellation was successful, false otherwise
     * @throws Exception if there is an error cancelling the order
     */
    boolean cancelOrder(Long orderId, Long userId) throws Exception;

    /**
     * Updates an order.
     *
     * @param order Order with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the order
     */
    boolean updateOrder(Order order) throws Exception;
}
