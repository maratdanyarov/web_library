package com.mdanyarov.weblibrary.dao;

import com.mdanyarov.weblibrary.entity.Order;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Order entity
 */
public interface OrderDao extends GenericDao<Order, Long> {

    /**
     * Finds orders by user ID.
     *
     * @param userId The user ID
     * @param connection Database connection to use
     * @return A list of orders for the specified user
     * @throws SQLException if there is an error executing the query
     */
    List<Order> findByUserId(Long userId, Connection connection) throws SQLException;

    /**
     * Find orders by book copy ID.
     *
     * @param bookCopyId The book copy ID
     * @param connection Database connection to use
     * @return A list of orders for the specified book copy
     * @throws SQLException if there is an error executing the query
     */
    List<Order> findByBookCopyId(Long bookCopyId, Connection connection) throws SQLException;

    /**
     * Finds active orders (PENDING or APPROVED) by user ID.
     *
     * @param userId The user ID
     * @param connection Database connection to use
     * @return A list of active orders for the specified user
     * @throws SQLException if there is an error executing the query
     */
    List<Order> findActiveByUserId(Long userId, Connection connection) throws SQLException;

    /**
     * Finds orders by status
     *
     * @param status The order status
     * @param connection Database connection to use
     * @return A list of orders with the specified status
     * @throws SQLException if there is an error executing the query
     */
    List<Order> findByStatus(Order.OrderStatus status, Connection connection) throws SQLException;

    /**
     * Updates the status of an order.
     *
     * @param orderId The order ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    boolean updateStatus(Long orderId, Order.OrderStatus status, Connection connection) throws SQLException;

    /**
     * Finds orders that are overdue (past return date but not returned).
     * @param connection Database connection to use
     * @return A list of overdue orders
     * @throws SQLException if there is an error executing the query
     */
    List<Order> findOverdue(Connection connection) throws SQLException;
}
