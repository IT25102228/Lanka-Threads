package com.lankathreads.controller;

import com.lankathreads.model.Role;
import com.lankathreads.model.User;
import com.lankathreads.repository.UserRepository;
import com.lankathreads.service.UserService;
import com.lankathreads.util.FileUploadUtil;
import com.lankathreads.util.ValidationUtils;
import com.lankathreads.model.Customer;
import com.lankathreads.repository.CustomerRepository;
import com.lankathreads.service.CustomerService;
import com.lankathreads.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for managing the currently logged-in user's profile:
 * Personal details, Profile photo, Password change, Theme preferences, and Account deletion.
 */
@SuppressWarnings("null")
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Helper: retrieves the active User record from session authentication.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "";
        
        // 1. Check if user is in staff users table
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        // 2. Check if user is in customers table
        Optional<Customer> custOpt = customerRepository.findByUsername(username);
        if (custOpt.isPresent()) {
            Customer c = custOpt.get();
            User u = new User();
            u.setId(c.getId());
            u.setUsername(c.getUsername());
            u.setPassword(c.getPassword());
            u.setName(c.getName());
            u.setEmail(c.getEmail());
            u.setRole(Role.CUSTOMER);
            u.setProfilePhoto(c.getProfilePhoto());
            u.setThemePreference(c.getThemePreference());
            return u;
        }
        throw new RuntimeException("Current authenticated user not found in database.");
    }

    /**
     * Helper: checks if the current user is a Customer.
     */
    private boolean isCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.isCustomer();
        }
        String username = authentication != null ? authentication.getName() : "";
        return customerRepository.findByUsername(username).isPresent();
    }

    /**
     * Injects common model attributes (user details, display role name) into all profile views.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        
        String roleDisplayName = switch (user.getRole()) {
            case FACTORY_MANAGER -> "Factory Manager";
            case PRODUCTION_SUPERVISOR -> "Production Supervisor";
            case INVENTORY_OFFICER -> "Inventory Officer";
            case SALES_MANAGER -> "Sales Manager";
            case SUPPLIER_OFFICER -> "Supplier Coordination Officer";
            case CUSTOMER -> "Customer";
        };
        model.addAttribute("roleName", roleDisplayName);
    }

    @GetMapping
    public String viewProfile(Model model) {
        model.addAttribute("activeMenu", "profile");
        return "profile/profile";
    }

    /**
     * Helper: redirects user back to their designated role dashboard.
     */
    private String getDashboardRedirect(User user) {
        return switch (user.getRole().name()) {
            case "FACTORY_MANAGER" -> "redirect:/manager/dashboard";
            case "PRODUCTION_SUPERVISOR" -> "redirect:/supervisor/dashboard";
            case "INVENTORY_OFFICER" -> "redirect:/inventory/dashboard";
            case "SALES_MANAGER" -> "redirect:/sales/dashboard";
            case "SUPPLIER_OFFICER" -> "redirect:/supplier/dashboard";
            default -> "redirect:/customer/shop";
        };
    }

    /**
     * Updates user's name and email address.
     */
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name, @RequestParam String email, RedirectAttributes redirectAttributes) {
        // Step 1: Validate email format
        if (!ValidationUtils.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email address (e.g. name@example.com).");
            return "redirect:/profile";
        }
        
        User user = getCurrentUser();
        String cleanEmail = email.trim();
        String cleanName = name.trim();

        // Step 2: Ensure email is not already taken by another account
        if (isCustomer()) {
            Customer cust = customerRepository.findById(user.getId()).orElseThrow();
            Optional<Customer> existingByEmail = customerRepository.findByEmail(cleanEmail);
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(cust.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email '" + cleanEmail + "' is already in use by another account.");
                return "redirect:/profile";
            }
            cust.setName(cleanName);
            cust.setEmail(cleanEmail);
            customerRepository.save(cust);
            user.setName(cleanName);
            user.setEmail(cleanEmail);
        } else {
            Optional<User> userWithEmail = userRepository.findByEmail(cleanEmail);
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email '" + cleanEmail + "' is already in use by another account.");
                return "redirect:/profile";
            }
            user.setName(cleanName);
            user.setEmail(cleanEmail);
            userRepository.save(user);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return getDashboardRedirect(user);
    }

    /**
     * Uploads and updates the user's profile avatar photo.
     */
    @PostMapping("/photo")
    public String updateProfilePhoto(@RequestParam("image") MultipartFile multipartFile,
                                     RedirectAttributes redirectAttributes) throws IOException {
        User user = getCurrentUser();
        
        // Step 1: Validate uploaded file is present
        if (multipartFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select an image file to upload.");
            return "redirect:/profile";
        }

        // Step 2: Create a unique filename and save to local uploads directory
        String rawFilename = multipartFile.getOriginalFilename();
        String cleanOriginalName = StringUtils.cleanPath(rawFilename != null ? rawFilename : "photo.jpg");
        String uniqueFileName = user.getId() + "_" + System.currentTimeMillis() + "_" + cleanOriginalName;
        
        if (isCustomer()) {
            Customer cust = customerRepository.findById(user.getId()).orElseThrow();
            cust.setProfilePhoto(uniqueFileName);
            customerRepository.save(cust);
        } else {
            user.setProfilePhoto(uniqueFileName);
            userRepository.save(user);
        }
        
        String uploadDir = "uploads/profile-photos/" + user.getId();
        FileUploadUtil.saveFile(uploadDir, uniqueFileName, multipartFile);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile photo updated successfully!");
        return getDashboardRedirect(user);
    }

    @GetMapping("/password")
    public String viewChangePassword(Model model) {
        model.addAttribute("activeMenu", "password");
        return "profile/change-password";
    }

    /**
     * Changes user account password after validating old password and new password confirmation.
     */
    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword, 
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        
        // Step 1: Verify current password is correct
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect!");
            return "redirect:/profile/password";
        }
        
        // Step 2: Ensure new password matches confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match!");
            return "redirect:/profile/password";
        }
        
        // Step 3: Hash new password using BCrypt and save
        String encoded = passwordEncoder.encode(newPassword);
        if (isCustomer()) {
            Customer cust = customerRepository.findById(user.getId()).orElseThrow();
            cust.setPassword(encoded);
            customerRepository.save(cust);
        } else {
            user.setPassword(encoded);
            userRepository.save(user);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        return getDashboardRedirect(user);
    }

    @GetMapping("/preferences")
    public String viewPreferences(Model model) {
        model.addAttribute("activeMenu", "preferences");
        return "profile/preferences";
    }
    
    /**
     * Updates theme preference (Dark or Light mode) from form submission.
     */
    @PostMapping("/preferences")
    public String updatePreferences(@RequestParam(required = false) Boolean darkMode, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        String newTheme = Boolean.TRUE.equals(darkMode) ? "DARK" : "LIGHT";

        if (isCustomer()) {
            Customer cust = customerRepository.findById(user.getId()).orElseThrow();
            cust.setThemePreference(newTheme);
            customerRepository.save(cust);
        } else {
            user.setThemePreference(newTheme);
            userRepository.save(user);
        }
        
        // Update in-memory security context so the theme takes effect immediately
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            customUserDetails.setThemePreference(newTheme);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Preferences saved successfully!");
        return getDashboardRedirect(user);
    }

    /**
     * Toggles theme preference between LIGHT and DARK via AJAX.
     */
    @PostMapping("/preferences/toggle")
    @ResponseBody
    public Map<String, Object> toggleTheme() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            
            // Check Staff user
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String newTheme = "LIGHT".equals(user.getThemePreference()) ? "DARK" : "LIGHT";
                user.setThemePreference(newTheme);
                userRepository.save(user);

                if (auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
                    customUserDetails.setThemePreference(newTheme);
                }
                return Map.of("success", true, "theme", newTheme);
            }
            
            // Check Customer user
            Optional<Customer> custOpt = customerRepository.findByUsername(username);
            if (custOpt.isPresent()) {
                Customer cust = custOpt.get();
                String newTheme = "LIGHT".equals(cust.getThemePreference()) ? "DARK" : "LIGHT";
                cust.setThemePreference(newTheme);
                customerRepository.save(cust);

                if (auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
                    customUserDetails.setThemePreference(newTheme);
                }
                return Map.of("success", true, "theme", newTheme);
            }
        }
        return Map.of("success", false, "message", "User not authenticated");
    }

    /**
     * Deletes the currently logged-in account, invalidating the HTTP session.
     */
    @PostMapping("/delete")
    public String deleteOwnAccount(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        try {
            Long userId = user.getId();
            boolean customerUser = isCustomer();
            
            // Step 1: Invalidate session and clear security context
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Step 2: Delete user from database
            if (customerUser) {
                customerService.deleteCustomer(userId);
            } else {
                userService.deleteUser(userId);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Your account has been deleted successfully.");
            return "redirect:/login?accountDeleted=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete account: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
