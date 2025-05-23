package com.mdanyarov.weblibrary.controller;


import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller for authentication-related operations
 */
@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Shows the home page.
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Shows the login page.
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "auth/login";
    }

    /**
     * Shows the registration page.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationForm());
        return "auth/register";
    }

    /**
     * Process user registration.
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            if (!form.getPassword().equals(form.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match.");
                return "auth/register";
            }

            userService.registerUser(
                    form.getUsername(),
                    form.getPassword(),
                    form.getEmail(),
                    form.getFirstName(),
                    form.getLastName()
            );

            redirectAttributes.addFlashAttribute("message", "User registered successfully.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "error.username", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            logger.error("Error during user registration", e);
            bindingResult.reject("error.global", "Registration failed. Please try again.");
            return "auth/register";
        }
    }

    /**
     * Shows the dashboard after successful login.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);

                switch (user.getRole()) {
                    case ADMIN:
                        return "redirect:/admin/dashboard";
                    case LIBRARIAN:
                        return "redirect:/librarian/dashboard";
                    case READER:
                        return "redirect:/reader/dashboard";
                    default:
                        return "dashboard";

                }
            }
        } catch (Exception e) {
            logger.error("Error loading user dashboard", e);
        }

        return "dashboard";
    }

    /**
     * User registration form DTO.
     */
    public static class UserRegistrationForm {
        private String username;
        private String password;
        private String confirmPassword;
        private String email;
        private String firstName;
        private String lastName;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
