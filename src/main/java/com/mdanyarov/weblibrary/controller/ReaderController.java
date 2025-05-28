package com.mdanyarov.weblibrary.controller;

import com.mdanyarov.weblibrary.dto.OrderRequest;
import com.mdanyarov.weblibrary.entity.Book;
import com.mdanyarov.weblibrary.entity.Order;
import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.BookService;
import com.mdanyarov.weblibrary.service.OrderService;
import com.mdanyarov.weblibrary.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller
 */
@Controller
@RequestMapping("/reader")
@PreAuthorize("hasAnyRole('READER', 'LIBRARIAN', 'ADMIN')")
public class ReaderController {

    private static final Logger logger = LoggerFactory.getLogger(ReaderController.class);

    private final OrderService orderService;
    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public ReaderController(OrderService orderService, BookService bookService, UserService userService) {
        this.orderService = orderService;
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Shows the reader dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                List<Order> activeOrders = orderService.findActiveByUserId(user.getId());
                List<Order> userOrders = orderService.findByUserId(user.getId());

                model.addAttribute("user", user);
                model.addAttribute("activeOrders", activeOrders);
                model.addAttribute("userOrders", userOrders);
                model.addAttribute("activeOrdersCount", activeOrders.size());

                long pendingCount = userOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.PENDING).count();
                long completedCount = userOrders.stream().filter(o -> o.getOrderStatus() == Order.OrderStatus.RETURNED).count();

                model.addAttribute("pendingOrdersCount", pendingCount);
                model.addAttribute("completedOrdersCount", completedCount);
            }

            return "reader/dashboard";
        } catch (Exception e) {
            logger.error("Error loading reader dashboard", e);
            model.addAttribute("errorMessage", "Error loading reader dashboard. Please try again later.");
            return "reader/dashboard";
        }
    }

    /**
     * Shows the user's orders.
     */
    @GetMapping("/orders")
    public String showOrders(Model model, Authentication auth) {
        try {
            String username = auth.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                List<Order> orders = orderService.findByUserId(user.getId());

                model.addAttribute("orders", orders);
                model.addAttribute("hasOrders",!orders.isEmpty());
            }

            return "reader/orders";
        } catch (Exception e) {
            logger.error("Error loading reader orders", e);
            model.addAttribute("errorMessage", "Error loading reader orders. Please try again later.");
            return "reader/orders";
        }
    }

    /**
     * Shows the book request form.
     */
    @GetMapping("/request/{bookId}")
    public String showRequestForm(@PathVariable Long bookId, Model model, Authentication auth) {
        try {
            Optional<Book> bookOptional = bookService.findById(bookId);

            if (bookOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Book not found");
                return "error/404";
            }

            Book book = bookOptional.get();
            if (book.getAvailableCopiesCount() == 0) {
                model.addAttribute("errorMessage", "No available copies for this book.");
                return "redirect:/books/" + bookId + "?error=no_copies";
            }

            // Check if user already has an active order for this book
            String username = auth.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                List<Order> activeOrders = orderService.findActiveByUserId(user.getId());

                boolean hasActiveOrderForBook = activeOrders.stream()
                        .anyMatch(order -> order.getBookCopy().getBook().getId().equals(bookId));

                if (hasActiveOrderForBook) {
                    model.addAttribute("errorMessage", "You already have an active order for this book.");
                    return "redirect:/books/" + bookId + "?error=existing_order";
                }
            }

            model.addAttribute("book", book);
            model.addAttribute("orderRequest", new OrderRequest());

            return "reader/request-form";
        } catch (Exception e) {
            logger.error("Error showing request form for book: " + bookId, e);
            model.addAttribute("errorMessage", "Error loading request form. Please try again later.");
            return "error/500";
        }
    }

    /**
     * Processes a book request.
     */
    @PostMapping("/request/{bookId}")
    public String processRequest(@PathVariable Long bookId,
                                 @Valid @ModelAttribute("orderRequest") OrderRequest orderRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            try {
                Optional<Book> bookOptional = bookService.findById(bookId);
                if (bookOptional.isPresent()) {
                    model.addAttribute("book", bookOptional.get());
                }
            } catch (Exception e) {
                logger.error("Error reloading book for request form", e);
            }
            return "reader/request-form";
        }

        try {
            String username = auth.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/books/" + bookId;
            }

            User user = userOptional.get();

            // The following would require creating a BookCopyService, but for now we'll use a placeholder
            // TODO: Implement proper book copy selection
            Order order = orderService.createOrder(
                    user.getId(),
                    1L, // Placeholder - should be actual available copy ID
                    orderRequest.getOrderType(),
                    orderRequest.getNotes()
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "Book request submitted successfully. Request ID: " + order.getId());

            return "redirect:/reader/orders";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books/" + bookId;

        } catch (Exception e) {
            logger.error("Error processing book request", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing request. Please try again later.");
            return "redirect:/books/" + bookId;
        }
    }

    /**
     * Cancels an order.
     */
    @PostMapping("/orders/{orderId/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {

        try {
            String username = auth.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/reader/orders/";
            }

            User user = userOptional.get();
            boolean cancelled = orderService.cancelOrder(orderId, user.getId());

            if (cancelled) {
                redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel order.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error canceling order " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling order. Please try again later.");
        }

        return "redirect:/reader/orders";
    }

    /**
     * Shows order details.
     */
    @GetMapping("/orders/{orderId}")
    public String showOrderDetails(@PathVariable Long orderId, Model model, Authentication auth) {
        try {
            Optional<Order> optionalOrder = orderService.findById(orderId);

            if (optionalOrder.isEmpty()) {
                model.addAttribute("errorMessage", "Order not found");
                return "error/404";
            }

            Order order = optionalOrder.get();

            String username = auth.getName();
            if (!order.getUser().getUsername().equals(username)) {
                model.addAttribute("errorMessage", "Access denied");
                return "error/403";
            }

            model.addAttribute("order", order);
            return "reader/order-details";

        } catch (Exception e) {
            logger.error("Error loading order details: " + orderId, e);
            model.addAttribute("errorMessage", "Error loading order details. Please try again later.");
            return "error/500";
        }
    }
}
