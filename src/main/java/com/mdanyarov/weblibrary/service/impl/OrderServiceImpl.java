package com.mdanyarov.weblibrary.service.impl;

import com.mdanyarov.weblibrary.dao.*;
import com.mdanyarov.weblibrary.entity.BookCopy;
import com.mdanyarov.weblibrary.entity.Order;
import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrderService.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDao orderDao;
    public final UserDao userDao;
    private final BookCopyDao bookCopyDao;
    private final ConnectionPool connectionPool;

    @Autowired
    public OrderServiceImpl(OrderDao orderDao, UserDao userDao, BookCopyDao bookCopyDao, ConnectionPool connectionPool) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.bookCopyDao = bookCopyDao;
        this.connectionPool = connectionPool;
    }

    /**
     * Finds an order by ID.
     *
     * @param id Order ID
     * @return Optional containing the order if found, empty otherwise
     * @throws Exception if there is an error finding the order
     */
    @Override
    public Optional<Order> findById(Long id) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            Order order = orderDao.findById(id, connection);
            return Optional.ofNullable(order);
        }
    }

    /**
     * Finds all orders.
     *
     * @return List of all orders
     * @throws Exception if there is an error finding orders
     */
    @Override
    public List<Order> findAll() throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return orderDao.findAll(connection);
        }
    }

    /**
     * Finds orders by user ID.
     *
     * @param userId User ID
     * @return List of orders for the specified user
     * @throws Exception if there is an error finding orders
     */
    @Override
    public List<Order> findByUserId(Long userId) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return orderDao.findByUserId(userId, connection);
        }
    }

    /**
     * Finds active orders (PENDING or APPROVED) by user ID.
     *
     * @param userId User ID
     * @return List of active orders for the specified user
     * @throws Exception if there is an error finding orders
     */
    @Override
    public List<Order> findActiveByUserId(Long userId) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return orderDao.findActiveByUserId(userId, connection);
        }
    }

    /**
     * Finds orders by status.
     *
     * @param status Order status
     * @return List of orders with the specified status
     * @throws Exception if there is an error finding orders
     */
    @Override
    public List<Order> findByStatus(Order.OrderStatus status) throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return orderDao.findByStatus(status, connection);
        }
    }

    /**
     * Finds overdue orders.
     *
     * @return List of overdue orders
     * @throws Exception if there is an error finding orders
     */
    @Override
    public List<Order> findOverdue() throws Exception {
        try (Connection connection = connectionPool.getConnection()) {
            return orderDao.findOverdue(connection);
        }
    }

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
    @Override
    public Order createOrder(Long userId, Long bookCopyId, Order.OrderType orderType, String notes) throws Exception {
        logger.info("Creating order for user: {} and book copy: {}", userId, bookCopyId);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            User user = userDao.findById(userId, connection);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new IllegalArgumentException("User is not active: " + userId);
            }

            BookCopy bookCopy = bookCopyDao.findById(bookCopyId, connection);
            if (bookCopy == null) {
                throw new IllegalArgumentException("Book copy not found: " + bookCopyId);
            }

            if (bookCopy.getStatus() != BookCopy.CopyStatus.AVAILABLE) {
                throw new IllegalArgumentException("Book copy is not available: " + bookCopyId);
            }

            List<Order> userActiveOrders = orderDao.findActiveByUserId(userId, connection);
            boolean hasActiveOrderForThisBook = userActiveOrders.stream()
                    .anyMatch(order -> order.getBookCopy().getId().equals(bookCopyId));

            if (hasActiveOrderForThisBook) {
                throw new IllegalArgumentException("User already has an active order for this book: " + bookCopyId);
            }

            Order order = new Order();
            order.setUser(user);
            order.setBookCopy(bookCopy);
            order.setOrderType(orderType);
            order.setOrderStatus(Order.OrderStatus.PENDING);
            order.setOrderDate(LocalDateTime.now());
            order.setNotes(notes);

            Order savedOrder = orderDao.save(order, connection);

            bookCopyDao.updateStatus(bookCopyId, BookCopy.CopyStatus.RESERVED, connection);

            logger.info("Order created successfully with ID: {}", savedOrder.getId());
            return savedOrder;
        });
    }

    /**
     * Approves an order.
     *
     * @param orderId Order ID
     * @param librarian Librarian approving the order
     * @param returnDate Expected return date
     * @return true if the approval was successful, false otherwise
     * @throws Exception if there is an error approving the order
     */
    @Override
    public boolean approveOrder(Long orderId, User librarian, LocalDateTime returnDate) throws Exception {
        logger.info("Approving order: {} by librarian: {}", orderId, librarian.getUsername());

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            Order order = orderDao.findById(orderId, connection);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }

            if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
                throw new IllegalArgumentException("Order is not in pending status");
            }

            order.approve(librarian, returnDate);

            boolean orderUpdated = orderDao.update(order, connection);
            boolean copyStatusUpdated = bookCopyDao.updateStatus(order.getBookCopy().getId(),
                    BookCopy.CopyStatus.ISSUED, connection);

            if (orderUpdated && copyStatusUpdated) {
                logger.info("Order approved successfully: {}", orderId);
                return true;
            }
            return false;
        });
    }

    /**
     * Rejects an order.
     *
     * @param orderId Order ID
     * @param librarian Librarian rejecting the order
     * @param notes Reason for rejection
     * @return true if the rejection was successful, false otherwise
     * @throws Exception if there is an error rejecting the order
     */
    @Override
    public boolean rejectOrder(Long orderId, User librarian, String notes) throws Exception {
        logger.info("Rejecting order: {} by librarian: {}", orderId, librarian.getUsername());

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            Order order = orderDao.findById(orderId, connection);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }

            if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
                throw new IllegalArgumentException("Order is not in pending status");
            }

            order.reject(librarian, notes);

            boolean orderUpdated = orderDao.update(order, connection);
            boolean copyStatusUpdated = bookCopyDao.updateStatus(order.getBookCopy().getId(),
                    BookCopy.CopyStatus.AVAILABLE, connection );

            if (orderUpdated && copyStatusUpdated) {
                logger.info("Order rejected successfully: {}", orderId);
                return true;
            }
            return false;
        });
    }

    /**
     * Marks an order as returned.
     *
     * @param orderId Order ID
     * @return true if the return was successful, false otherwise
     * @throws Exception if there is an error processing the return
     */
    @Override
    public boolean returnOrder(Long orderId) throws Exception {
        logger.info("Processing return for order: {}", orderId);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            Order order = orderDao.findById(orderId, connection);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            if (order.getOrderStatus() != Order.OrderStatus.APPROVED) {
                throw new IllegalArgumentException("Order is not in approved status");
            }

            order.returnBook();

            boolean orderUpdated = orderDao.update(order, connection);
            boolean copyStatusUpdated = bookCopyDao.updateStatus(order.getBookCopy().getId(),
                    BookCopy.CopyStatus.AVAILABLE, connection);

            if (orderUpdated && copyStatusUpdated) {
                logger.info("Order returned successfully: {}", orderId);
                return true;
            }

            return false;
        });
    }

    /**
     * Cancels an order.
     *
     * @param orderId Order ID
     * @param userId User ID (must match the order's user)
     * @return true if the cancellation was successful, false otherwise
     * @throws Exception if there is an error cancelling the order
     */
    @Override
    public boolean cancelOrder(Long orderId, Long userId) throws Exception {
        logger.info("Cancelling order: {} by user: {}", orderId, userId);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            Order order = orderDao.findById(orderId, connection);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }

            if (!order.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Order does not belong to the specified user");
            }

            if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
                throw new IllegalArgumentException("Order is not in pending status. Only pending order can be cancelled");
            }

            order.cancel();

            boolean orderUpdated = orderDao.update(order, connection);
            boolean copyStatusUpdated = bookCopyDao.updateStatus(order.getBookCopy().getId(),
                    BookCopy.CopyStatus.AVAILABLE, connection);

            if (orderUpdated && copyStatusUpdated) {
                logger.info("Order cancelled successfully: {}", orderId);
                return true;
            }

            return false;
        });
    }

    /**
     * Updates an order.
     *
     * @param order Order with updated information
     * @return true if the update was successful, false otherwise
     * @throws Exception if there is an error updating the order
     */
    @Override
    public boolean updateOrder(Order order) throws Exception {
        logger.info("Updating order: {}", order);

        return TransactionManager.executeTransaction(connectionPool, connection -> {
            boolean result = orderDao.update(order, connection);
            if (result) {
                logger.info("Order updated successfully: {}", order.getId());
            }
            return result;
        });
    }
}
