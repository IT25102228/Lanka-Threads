package com.lankathreads.controller;

import com.lankathreads.model.Role;
import com.lankathreads.model.User;
import com.lankathreads.service.UserService;
import com.lankathreads.util.ValidationUtils;
import com.lankathreads.model.Customer;
import com.lankathreads.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling public authentication (Login, Register) and Support requests.
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

    /**
     * Handles public support ticket submissions.
     */
    @PostMapping("/support")
    public String handleSupportForm(@RequestParam String name,
                                    @RequestParam String email,
                                    @RequestParam String subject,
                                    @RequestParam String message,
                                    RedirectAttributes redirectAttributes) {
        // Step 1: Validate email format
        if (!ValidationUtils.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email address (e.g. name@example.com).");
            return "redirect:/support";
        }

        // Step 2: Generate random 6-digit confirmation ticket ID
        int ticketId = (int) (Math.random() * 900000 + 100000);
        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you " + name.trim() + "! Your support ticket #" + ticketId +
                " has been submitted. Our team will contact you at " + email.trim() + " within 24 hours.");
        return "redirect:/support";
    }

    /**
     * Handles new user registration for both Customers and Staff roles.
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               Model model) {
        String cleanEmail = email.trim();
        String cleanUsername = username.trim();
        String cleanName = name.trim();

        // Step 1: Validate email format
        if (!ValidationUtils.isValidEmail(cleanEmail)) {
            model.addAttribute("error", "Please enter a valid email address (e.g. kasun@example.com).");
            return "register";
        }

        // Step 2: Ensure username is unique across both Users and Customers
        if (userService.getUserByUsername(cleanUsername).isPresent() ||
            customerService.getCustomerByUsername(cleanUsername).isPresent()) {
            model.addAttribute("error", "Username '" + cleanUsername + "' is already taken. Please choose another.");
            return "register";
        }

        // Step 3: Ensure email is unique across both Users and Customers
        if (userService.getUserByEmail(cleanEmail).isPresent() ||
            customerService.getCustomerByEmail(cleanEmail).isPresent()) {
            model.addAttribute("error", "Email '" + cleanEmail + "' is already registered. Please sign in or use another email.");
            return "register";
        }

        // Step 4: Save record into the appropriate table based on selected role
        Role selectedRole = Role.valueOf(role);
        if (Role.CUSTOMER.equals(selectedRole)) {
            Customer customer = new Customer();
            customer.setName(cleanName);
            customer.setEmail(cleanEmail);
            customer.setUsername(cleanUsername);
            customer.setPassword(password); // Automatically BCrypt encoded inside customerService.saveCustomer()
            customerService.saveCustomer(customer);
        } else {
            User user = new User();
            user.setName(cleanName);
            user.setEmail(cleanEmail);
            user.setUsername(cleanUsername);
            user.setPassword(password); // Automatically BCrypt encoded inside userService.saveUser()
            user.setRole(selectedRole);
            userService.saveUser(user);
        }

        return "redirect:/login?registered";
    }
}
