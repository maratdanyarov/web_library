package com.mdanyarov.weblibrary.controller;

import com.mdanyarov.weblibrary.entity.Order;
import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.OrderService;
import com.mdanyarov.weblibrary.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for librarian-specific operations.
 */
@Controller
@RequestMapping("/librarian")
@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
public class LibrarianController {

    private static final Logger logger = LoggerFactory.getLogger(LibrarianController.class);

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public LibrarianController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * Shows the librarian dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<Order> pendingOrders = orderService.findByStatus(Order.OrderStatus.PENDING);
            List<Order> overdueOrders = orderService.findOverdue();
            List<Order> allOrders = orderService.findAll();

            model.addAttribute("pendingOrdersCount", pendingOrders.size());
            model.addAttribute("overdueOrdersCount", overdueOrders.size());
            model.addAttribute("totalOrdersCount", allOrders.size());

            List<Order> recendPendingOrders = pendingOrders.stream()
                    .limit(5)
                    .toList();

            model.addAttribute("recendPendingOrders", recendPendingOrders);
            model.addAttribute("overdueOrders", overdueOrders);

            return "librarian/dashboard";

        } catch (Exception e) {
            logger.error("Error loading librarian dashboard", e);
            model.addAttribute("errorMessage", "Error loading librarian dashboard. Please try again later.");
            return "librarian/dashboard";
        }
    }

    /**
     * Shows pending orders for approval.
     */
    @GetMapping("/orders/pending")
    public String showPendingOrders(Model model) {
        try {
            List<Order> pendingOrders = orderService.findByStatus(Order.OrderStatus.PENDING);

            model.addAttribute("orders", pendingOrders);
            model.addAttribute("hasOrders", !pendingOrders.isEmpty());
            model.addAttribute("orderStatus", "PENDING");

            return "librarian/orders";

        } catch (Exception e) {
            logger.error("Error loading pending orders", e);
            model.addAttribute("errorMessage", "Error loading orders. Please try again later.");
            return "librarian/orders";
        }
    }

    /**
     * Shows all orders with filtering options.
     */
    @GetMapping("/orders")
    public String showOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        try {
            List<Order> orders;

            if (status != null && !status.isEmpty()) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    orders = orderService.findByStatus(orderStatus);
                    model.addAttribute("orderStatus", status.toUpperCase());
                } catch (Exception e) {
                    orders = orderService.findAll();
                    model.addAttribute("orderStatus", "ALL");
                }
            } else {
                orders = orderService.findAll();
                model.addAttribute("orderStatus", "ALL");
            }

            model.addAttribute("orders", orders);
            model.addAttribute("hasOrders", !orders.isEmpty());

            return "librarian/orders";
        } catch (Exception e) {
            logger.error("Error loading orders", e);
            model.addAttribute("errorMessage", "Error loading orders. Please try again later.");
            return "librarian/orders";
        }
    }

    /**
     * Shows overdue orders.
     */
    @GetMapping("/orders/overdue")
    public String showOverdueOrders(Model model) {
        try {
            List<Order> overdueOrders = orderService.findOverdue();

            model.addAttribute("orders", overdueOrders);
            model.addAttribute("hasOrders", !overdueOrders.isEmpty());
            model.addAttribute("orderStatus", "OVERDUE");

            return "librarian/orders";
        } catch (Exception e) {
            logger.error("Error loading overdue orders", e);
            model.addAttribute("errorMessage", "Error loading orders. Please try again later.");
            return "librarian/orders";
        }
    }

    /**
     * Shows order details and approval form.
     */
    @GetMapping("/orders/{orderId}")
    public String showOrderDetails(@PathVariable("orderId") Integer orderId, Model model) {
        try {
            Optional<Order> orderOptional = orderService.findById(orderId);

            if (orderOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Order not found");
                return "error/404";
            }

            Order order = orderOptional.get();
            model.addAttribute("order", order);

            return "librarian/order-details";
        } catch (Exception e) {
            logger.error("Error loading order details: " + orderId, e);
            model.addAttribute("errorMessage", "Error loading order details. Please try again later.");
            return "error/500";
        }
    }

    /**
     * Approves and order.
     */
    @PostMapping("/orders/{orderId}/approve")
    public String approveOrder(@PathVariable("orderId") Long orderId,
                               @RequestParam("returnDate") String returnDateStr,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Optional<User> librarianOptional = userService.findByUsername(username);

            if (librarianOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Librarian not found");
                return "redirect:/librarian/orders";
            }

            User librarian = librarianOptional.get();

            LocalDateTime returnDate = LocalDateTime.parse(returnDateStr + "T23:59:59");

            boolean approved = orderService.approveOrder(orderId, librarian, returnDate);

            if (approved) {
                redirectAttributes.addFlashAttribute("successMessage", "Order approved successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve order.");
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error approving order: " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving order. Please try again later.");
        }

        return "redirect:/librarian/orders/" + orderId;
    }

    /**
     * Rejects an order.
     */
    @PostMapping("/orders/{orderId}/reject")
    public String rejectOrder(@PathVariable("orderId") Long orderId,
                              @RequestParam("notes") String notes,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        try {
            String username = auth.getName();
            Optional<User> librarianOptional = userService.findByUsername(username);

            if (librarianOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Librarian not found");
                return "redirect:/librarian/orders";
            }

            User librarian = librarianOptional.get();
            boolean rejected = orderService.rejectOrder(orderId, librarian, notes);

            if (rejected) {
                redirectAttributes.addFlashAttribute("successMessage", "Order rejected successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject order.");
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error rejecting order: " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting order. Please try again later.");
        }

        return "redirect:/librarian/orders/" + orderId;
    }

    /**
     * Processes book return.
     */
    @PostMapping("/orders/{orderId}/return")
    public String returnBook(@PathVariable("orderId") Long orderId,
                             RedirectAttributes redirectAttributes) {
        try {
            boolean returned = orderService.returnOrder(orderId);

            if (returned) {
                redirectAttributes.addFlashAttribute("successMessage", "Book returned successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to process return.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing return for order: " + orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing return. Please try again later.");
        }

        return "redirect:/librarian/orders/" + orderId;
    }
}
