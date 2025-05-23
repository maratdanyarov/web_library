package com.mdanyarov.weblibrary.controller;

import com.mdanyarov.weblibrary.entity.Book;
import com.mdanyarov.weblibrary.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for book-related operations.
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private static final int DEFAULT_PAGE_SIZE = 12;

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Shows the book catalog page.
     */
    @GetMapping
    public String showBookCatalog(@RequestParam(value = "search", required = false) String search,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "12") int size,
                                  Model model) {

        try {
            List<Book> books;
            int totalBooks;

            if (search != null && !search.trim().isEmpty()) {
                // Perform search
                books = bookService.search(search);
                totalBooks = books.size();

                // Apply pagination to search results
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, books.size());

                if (startIndex < books.size()) {
                    books = books.subList(startIndex, endIndex);
                } else {
                    books = List.of();
                }

                model.addAttribute("searchQuery", search);
            } else {
                // Get paginated results
                books = bookService.findWithPagination(page, size);
                totalBooks = bookService.getTotalCount();
            }

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalBooks / size);

            model.addAttribute("books", books);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("hasBooks", !books.isEmpty());

            // Pagination flags
            model.addAttribute("hasPrevious", page > 0);
            model.addAttribute("hasNext", page < totalPages - 1);
            model.addAttribute("previousPage", page - 1);
            model.addAttribute("nextPage", page + 1);

            return "books/catalog";

        } catch (Exception e) {
            logger.error("Error loading book catalog", e);
            model.addAttribute("errorMessage", "Error loading books. Please try again.");
            return "books/catalog";
        }
    }

    /**
     * Shows book details page.
     */
    @GetMapping("/{id}")
    public String showBookDetails(@PathVariable Long id, Model model) {
        try {
            Optional<Book> bookOptional = bookService.findById(id);

            if (bookOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Book not found.");
                return "error/404";
            }

            Book book = bookOptional.get();
            model.addAttribute("book", book);

            // TODO: Load book copies and availability info
            // TODO: Load genres for this book

            return "books/details";

        } catch (Exception e) {
            logger.error("Error loading book details for ID: " + id, e);
            model.addAttribute("errorMessage", "Error loading book details. Please try again.");
            return "error/500";
        }
    }

    /**
     * Handles book search via AJAX or form submission.
     */
    @GetMapping("/search")
    public String searchBooks(@RequestParam("q") String query,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              Model model) {

        // Redirect to main catalog with search parameter
        return "redirect:/books?search=" + query + "&page=" + page;
    }

    /**
     * Shows available books only.
     */
    @GetMapping("/available")
    public String showAvailableBooks(Model model) {
        try {
            List<Book> books = bookService.findAvailable();

            model.addAttribute("books", books);
            model.addAttribute("hasBooks", !books.isEmpty());
            model.addAttribute("showAvailableOnly", true);

            return "books/catalog";

        } catch (Exception e) {
            logger.error("Error loading available books", e);
            model.addAttribute("errorMessage", "Error loading available books. Please try again.");
            return "books/catalog";
        }
    }

    /**
     * Shows the book creation form (admin/librarian only).
     */
    @GetMapping("/create")
    public String showCreateBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("isEdit", false);
        return "books/form";
    }

    /**
     * Handles book creation (admin/librarian only).
     */
    @PostMapping("/create")
    public String createBook(@ModelAttribute Book book,
                             RedirectAttributes redirectAttributes) {
        try {
            Book createdBook = bookService.createBook(book);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + createdBook.getTitle() + "' created successfully.");
            return "redirect:/books/" + createdBook.getId();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("book", book);
            return "redirect:/books/create";

        } catch (Exception e) {
            logger.error("Error creating book", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating book. Please try again.");
            redirectAttributes.addFlashAttribute("book", book);
            return "redirect:/books/create";
        }
    }

    /**
     * Shows the book edit form (admin/librarian only).
     */
    @GetMapping("/{id}/edit")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        try {
            Optional<Book> bookOptional = bookService.findById(id);

            if (bookOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Book not found.");
                return "error/404";
            }

            model.addAttribute("book", bookOptional.get());
            model.addAttribute("isEdit", true);
            return "books/form";

        } catch (Exception e) {
            logger.error("Error loading book for edit: " + id, e);
            model.addAttribute("errorMessage", "Error loading book. Please try again.");
            return "error/500";
        }
    }

    /**
     * Handles book update (admin/librarian only).
     */
    @PostMapping("/{id}/edit")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             RedirectAttributes redirectAttributes) {
        try {
            book.setId(id);
            boolean updated = bookService.updateBook(book);

            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Book '" + book.getTitle() + "' updated successfully.");
                return "redirect:/books/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update book.");
                return "redirect:/books/" + id + "/edit";
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books/" + id + "/edit";

        } catch (Exception e) {
            logger.error("Error updating book: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating book. Please try again.");
            return "redirect:/books/" + id + "/edit";
        }
    }

    /**
     * Handles book deletion (admin only).
     */
    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Optional<Book> bookOptional = bookService.findById(id);

            if (bookOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
                return "redirect:/books";
            }

            String bookTitle = bookOptional.get().getTitle();
            boolean deleted = bookService.deleteBook(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Book '" + bookTitle + "' deleted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete book.");
            }

            return "redirect:/books";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books";

        } catch (Exception e) {
            logger.error("Error deleting book: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting book. Please try again.");
            return "redirect:/books";
        }
    }
}