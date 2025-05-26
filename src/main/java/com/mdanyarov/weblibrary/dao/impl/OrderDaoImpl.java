package com.mdanyarov.weblibrary.dao.impl;

import com.mdanyarov.weblibrary.dao.OrderDao;
import com.mdanyarov.weblibrary.entity.Book;
import com.mdanyarov.weblibrary.entity.BookCopy;
import com.mdanyarov.weblibrary.entity.Order;
import com.mdanyarov.weblibrary.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of OrderDao.
 */
public class OrderDaoImpl implements OrderDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);

    private static final String FIND_BY_ID = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.id = ?
            """;

    private static final String FIND_ALL = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            ORDER BY o.order_date DESC
            """;

    private static final String FIND_BY_USER_ID = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.user_id = ?
            ORDER BY o.order_date DESC
            """;

    private static final String FIND_BY_BOOK_COPY_ID = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.book_copy_id = ?
            ORDER BY o.order_date DESC
            """;

    private static final String FIND_ACTIVE_BY_USER_ID = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.user_id = ? AND o.order_status IN ('PENDING', 'APPROVED')
            ORDER BY o.order_date DESC
            """;

    private static final String FIND_BY_STATUS = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.order_status = ?
            ORDER BY o.order_date DESC
            """;

    private static final String FIND_OVERDUE = """
            SELECT o.id, o.order_type, o.order_status, o.order_date, o.approval_date, 
                   o.return_date, o.actual_return_date, o.notes, o.created_at, o.updated_at,
                   u.id as user_id, u.username, u.email, u.first_name, u.last_name, u.role, u.status,
                   bc.id as copy_id, bc.inventory_number, bc.status as copy_status, bc.location,
                   b.id as book_id, b.title, b.author, b.publisher, b.publication_year, b.isbn, b.description,
                   p.id as processed_by_id, p.username as processed_by_username, p.first_name as processed_by_first_name, p.last_name as processed_by_last_name
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN book_copies bc ON o.book_copy_id = bc.id
            JOIN books b ON bc.book_id = b.id
            LEFT JOIN users p ON o.processed_by = p.id
            WHERE o.order_status = 'APPROVED' AND o.return_date < NOW() AND o.actual_return_date IS NULL
            ORDER BY o.return_date ASC
            """;

    private static final String INSERT_ORDER =
            "INSERT INTO orders (user_id, book_copy_id, order_type, order_status, order_date, notes, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_ORDER =
            "UPDATE orders SET user_id = ?, book_copy_id = ?, order_type = ?, order_status = ?, " +
                    "approval_date = ?, return_date = ?, actual_return_date = ?, processed_by = ?, notes = ?, updated_at = ? " +
                    "WHERE id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE orders SET order_status = ?, updated_at = ? WHERE id = ?";

    private static final String DELETE_ORDER =
            "DELETE FROM orders WHERE id = ?";

    /**
     * Finds an order by ID.
     *
     * @param id         The ID of the order
     * @param connection Database connections to use
     * @return The order, or null if not found
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Order findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all orders.
     *
     * @param connection Database connection to use
     * @return A list of all orders
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findAll(Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ALL)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Finds orders by user ID.
     *
     * @param userId     The user ID
     * @param connection Database connection to use
     * @return A list of orders for the specified user
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findByUserId(Long userId, Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_USER_ID)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Find orders by book copy ID.
     *
     * @param bookCopyId The book copy ID
     * @param connection Database connection to use
     * @return A list of orders for the specified book copy
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findByBookCopyId(Long bookCopyId, Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_BOOK_COPY_ID)) {
            stmt.setLong(1, bookCopyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Finds active orders (PENDING or APPROVED) by user ID.
     *
     * @param userId     The user ID
     * @param connection Database connection to use
     * @return A list of active orders for the specified user
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findActiveByUserId(Long userId, Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_ACTIVE_BY_USER_ID)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Finds orders by status
     *
     * @param status     The order status
     * @param connection Database connection to use
     * @return A list of orders with the specified status
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findByStatus(Order.OrderStatus status, Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_STATUS)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    /**
     * Finds orders that are overdue (past return date but not returned).
     *
     * @param connection Database connection to use
     * @return A list of overdue orders
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public List<Order> findOverdue(Connection connection) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_OVERDUE);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    /**
     * Saves an order (creates or updates).
     *
     * @param order The order to save
     * @param connection Database connection to use
     * @return The saved order with updated ID (if created)
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public Order save(Order order, Connection connection) throws SQLException {
        if (order.getId() == null) {
            return insert(order, connection);
        } else {
            update(order, connection);
            return order;
        }
    }

    private Order insert(Order order, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ORDER)) {
            stmt.setLong(1, order.getUser().getId());
            stmt.setLong(2, order.getBookCopy().getId());
            stmt.setString(3, order.getOrderType().name());
            stmt.setString(4, order.getOrderStatus().name());
            stmt.setTimestamp(5, order.getOrderDate() != null ? Timestamp.valueOf(order.getOrderDate()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(6, order.getNotes());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                    return order;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Updates an existing order.
     *
     * @param order The order to update
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean update(Order order, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ORDER)) {
            stmt.setLong(1, order.getUser().getId());
            stmt.setLong(2, order.getBookCopy().getId());
            stmt.setString(3, order.getOrderType().name());
            stmt.setString(4, order.getOrderStatus().name());
            stmt.setTimestamp(5, order.getApprovedDate() != null ? Timestamp.valueOf(order.getApprovedDate()) : null);
            stmt.setTimestamp(6, order.getReturnDate() != null ? Timestamp.valueOf(order.getReturnDate()) : null);
            stmt.setTimestamp(7, order.getActualReturnDate() != null ? Timestamp.valueOf(order.getActualReturnDate()) : null);
            stmt.setObject(8, order.getProcessedBy() != null ? order.getProcessedBy().getId() : null);
            stmt.setString(9, order.getNotes());
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(11, order.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId The order ID
     * @param status The new status
     * @param connection Database connection to use
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error executing the query
     */
    @Override
    public boolean updateStatus(Long orderId, Order.OrderStatus status, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_STATUS)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, orderId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an order by ID.
     *
     * @param id The ID of the order to delete
     * @param connection Database connection to use
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is ane error executing the query
     */
    @Override
    public boolean delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ORDER)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderType(Order.OrderType.valueOf(rs.getString("order_type")));
        order.setOrderStatus(Order.OrderStatus.valueOf(rs.getString("order_status")));
        order.setNotes(rs.getString("notes"));

        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDateTime());
        }

        Timestamp approvalDate = rs.getTimestamp("approval_date");
        if (approvalDate != null) {
            order.setApprovedDate(approvalDate.toLocalDateTime());
        }

        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            order.setReturnDate(returnDate.toLocalDateTime());
        }

        Timestamp actualReturnDate = rs.getTimestamp("actual_return_date");
        if (actualReturnDate != null) {
            order.setActualReturnDate(actualReturnDate.toLocalDateTime());
        }

        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setStatus(User.UserStatus.valueOf(rs.getString("status")));
        order.setUser(user);

        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setIsbn(rs.getString("isbn"));
        book.setDescription(rs.getString("description"));

        BookCopy bookCopy = new BookCopy();
        bookCopy.setId(rs.getLong("copy_id"));
        bookCopy.setInventoryNumber(rs.getString("inventory_number"));
        bookCopy.setStatus(BookCopy.CopyStatus.valueOf(rs.getString("copy_status")));
        bookCopy.setLocation(rs.getString("location"));
        bookCopy.setBook(book);
        order.setBookCopy(bookCopy);

        Long processedById = rs.getLong("processed_by_id");
        if (!rs.wasNull() && processedById > 0) {
            User processedBy = new User();
            processedBy.setId(processedById);
            processedBy.setUsername(rs.getString("processed_by_username"));
            processedBy.setFirstName(rs.getString("processed_by_first_name"));
            processedBy.setLastName(rs.getString("processed_by_last_name"));
            order.setProcessedBy(processedBy);
        }

        return order;
    }
}
