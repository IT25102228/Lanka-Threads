package com.lankathreads.controller;

import com.lankathreads.util.ValidationUtils;
import com.lankathreads.model.Customer;
import com.lankathreads.model.Order;
import com.lankathreads.service.CustomerService;
import com.lankathreads.service.OrderService;
import com.lankathreads.model.Product;

import com.lankathreads.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Sales Manager:
 * Sales Analytics Dashboard, Finished Goods Product Catalog CRUD, Order Fulfilment, and Customer Management.
 */
@Controller
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;



    // =========================================================================
    // 1. SALES DASHBOARD
    // =========================================================================

    /**
     * Sales Manager Dashboard:
     * Calculates total revenue, pending processing orders, completed shipments, and recent orders.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("totalOrders", orders.size());
        
        // Step 1: Calculate total sales revenue and order status breakdown
        BigDecimal totalSales = BigDecimal.ZERO;
        long processingCount = 0;
        long completedCount = 0;

        for (Order o : orders) {
            String status = o.getStatus();
            if (status != null && !status.equalsIgnoreCase("Cancelled")) {
                if (o.getTotalAmount() != null) {
                    totalSales = totalSales.add(o.getTotalAmount());
                }
            }
            if (status != null && (status.equalsIgnoreCase("Processing") || status.equalsIgnoreCase("Pending"))) {
                processingCount++;
            }
            if (status != null && status.equalsIgnoreCase("Shipped")) {
                completedCount++;
            }
        }

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("completedCount", completedCount);
        
        // Step 2: Extract top 5 most recent orders (newest first)
        List<Order> recentOrders = new ArrayList<>(orders);
        recentOrders.sort((a, b) -> {
            if (a.getId() == null && b.getId() == null) return 0;
            if (a.getId() == null) return 1;
            if (b.getId() == null) return -1;
            return b.getId().compareTo(a.getId());
        });
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }
        model.addAttribute("recentOrders", recentOrders);
        
        return "sales/dashboard";
    }

    // =========================================================================
    // 2. PRODUCT CATALOG MANAGEMENT
    // =========================================================================

    /**
     * Lists all products for management.
     */
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "sales/products";
    }

    /**
     * Adds a new product with image uploads and cost calculations.
     */
    @PostMapping("/products/add")
    public String addProduct(Product product,
                             @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                             @RequestParam(value = "mainImageUrl", required = false) String mainImageUrl,
                             @RequestParam(value = "mainImagePath", required = false) String mainImagePath,
                             @RequestParam(value = "subImage1", required = false) MultipartFile subImage1,
                             @RequestParam(value = "subImage1Url", required = false) String subImage1Url,
                             @RequestParam(value = "subImage1Path", required = false) String subImage1Path,
                             @RequestParam(value = "subImage2", required = false) MultipartFile subImage2,
                             @RequestParam(value = "subImage2Url", required = false) String subImage2Url,
                             @RequestParam(value = "subImage2Path", required = false) String subImage2Path,
                             RedirectAttributes redirectAttributes) {
        // Step 1: Validate required fields
        if (hasMissingRequiredValues(product)) {
            redirectAttributes.addFlashAttribute("error", "Please fill in all required product fields.");
            return "redirect:/sales/products";
        }
        if (hasNegativeValues(product)) {
            redirectAttributes.addFlashAttribute("error", "Values cannot be negative.");
            return "redirect:/sales/products";
        }

        // Step 2: Process and attach images (main image + 2 optional gallery views)
        try {
            applyProductImages(product,
                    mainImage, mainImageUrl, mainImagePath,
                    subImage1, subImage1Url, subImage1Path,
                    subImage2, subImage2Url, subImage2Path);
            
            // Step 3: Save product in database
            productService.saveProduct(product);
            return "redirect:/sales/products?success=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Could not save product: " + e.getMessage());
            return "redirect:/sales/products";
        }
    }

    /**
     * Updates an existing product details and images.
     */
    @PostMapping("/products/update")
    public String updateProduct(@RequestParam Long id,
                                Product product,
                                @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                                @RequestParam(value = "mainImageUrl", required = false) String mainImageUrl,
                                @RequestParam(value = "mainImagePath", required = false) String mainImagePath,
                                @RequestParam(value = "subImage1", required = false) MultipartFile subImage1,
                                @RequestParam(value = "subImage1Url", required = false) String subImage1Url,
                                @RequestParam(value = "subImage1Path", required = false) String subImage1Path,
                                @RequestParam(value = "subImage2", required = false) MultipartFile subImage2,
                                @RequestParam(value = "subImage2Url", required = false) String subImage2Url,
                                @RequestParam(value = "subImage2Path", required = false) String subImage2Path,
                                RedirectAttributes redirectAttributes) {
        if (hasMissingRequiredValues(product)) {
            redirectAttributes.addFlashAttribute("error", "Please fill in all required product fields.");
            return "redirect:/sales/products";
        }
        if (hasNegativeValues(product)) {
            redirectAttributes.addFlashAttribute("error", "Values cannot be negative.");
            return "redirect:/sales/products";
        }

        Product existing = productService.getProductById(id);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/sales/products";
        }

        try {
            // Update fields
            existing.setName(product.getName());
            existing.setCategory(product.getCategory());
            existing.setSize(product.getSize());
            existing.setColour(product.getColour());
            existing.setStockQuantity(product.getStockQuantity());
            existing.setFabricCost(product.getFabricCost());
            existing.setLaborCost(product.getLaborCost());
            existing.setOverheadCost(product.getOverheadCost());
            existing.setPrice(product.getPrice());

            // Clear old images before applying newly uploaded ones
            existing.setMainImageUrl(null);
            existing.setSubImage1Url(null);
            existing.setSubImage2Url(null);

            applyProductImages(existing,
                    mainImage, mainImageUrl, mainImagePath,
                    subImage1, subImage1Url, subImage1Path,
                    subImage2, subImage2Url, subImage2Path);

            productService.saveProduct(existing);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Could not update product: " + e.getMessage());
            return "redirect:/sales/products";
        }
        return "redirect:/sales/products?success=true";
    }

    /**
     * Deletes a product by ID.
     */
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete product because it is associated with existing customer orders.");
        }
        return "redirect:/sales/products";
    }

    // =========================================================================
    // 3. ORDER MANAGEMENT
    // =========================================================================

    /**
     * Lists all customer orders for status tracking and fulfillment.
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "sales/orders";
    }

    /**
     * Updates order fulfillment status (e.g. Processing, Shipped, Delivered).
     */
    @PostMapping("/orders/update")
    public String updateOrder(@RequestParam Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            order.setStatus(status);
            orderService.placeOrder(order);
            redirectAttributes.addFlashAttribute("success", "Order #" + order.getOrderCode() + " status updated to " + status + ".");
        } else {
            redirectAttributes.addFlashAttribute("error", "Order not found.");
        }
        return "redirect:/sales/orders";
    }

    // =========================================================================
    // 4. CUSTOMER PROFILE MANAGEMENT
    // =========================================================================

    /**
     * Lists all registered customers.
     */
    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "sales/customers";
    }

    /**
     * Updates customer name and email.
     */
    @PostMapping("/customers/update")
    public String updateCustomer(@RequestParam Long id,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String email,
                                 RedirectAttributes redirectAttributes) {
        Customer existing = customerService.getCustomerById(id);
        if (existing != null) {
            if (email != null && !email.isBlank() && !email.trim().equalsIgnoreCase(existing.getEmail())) {
                if (!ValidationUtils.isValidEmail(email)) {
                    redirectAttributes.addFlashAttribute("error", "Please provide a valid email address.");
                    return "redirect:/sales/customers";
                }
                if (customerService.getCustomerByEmail(email.trim()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Email '" + email.trim() + "' is already in use by another user.");
                    return "redirect:/sales/customers";
                }
                existing.setEmail(email.trim());
            }
            if (name != null && !name.isBlank()) {
                existing.setName(name.trim());
            }
            try {
                customerService.saveExistingCustomer(existing);
                redirectAttributes.addFlashAttribute("success", "Customer profile updated successfully.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to update customer: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Customer not found.");
        }
        return "redirect:/sales/customers";
    }

    /**
     * Deletes a customer account.
     */
    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete customer because they are associated with existing orders.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete customer: " + e.getMessage());
        }
        return "redirect:/sales/customers";
    }

    // =========================================================================
    // 5. HELPER METHODS (VALIDATION & IMAGE HANDLING)
    // =========================================================================

    private boolean hasMissingRequiredValues(Product product) {
        return product == null
                || product.getName() == null || product.getName().isBlank()
                || product.getCategory() == null || product.getCategory().isBlank()
                || product.getSize() == null || product.getSize().isBlank()
                || product.getColour() == null || product.getColour().isBlank()
                || product.getPrice() == null
                || product.getStockQuantity() == null
                || product.getFabricCost() == null
                || product.getLaborCost() == null
                || product.getOverheadCost() == null;
    }

    private boolean hasNegativeValues(Product product) {
        return product.getPrice().signum() < 0
                || product.getStockQuantity() < 0
                || product.getFabricCost().signum() < 0
                || product.getLaborCost().signum() < 0
                || product.getOverheadCost().signum() < 0;
    }

    /**
     * Resolves and sets image paths for mainImage, subImage1, and subImage2.
     * Priority order: uploaded file > web URL > local/relative path.
     */
    private void applyProductImages(Product product,
                                    MultipartFile mainImage, String mainImageUrl, String mainImagePath,
                                    MultipartFile subImage1, String subImage1Url, String subImage1Path,
                                    MultipartFile subImage2, String subImage2Url, String subImage2Path) {
        String main = resolveImagePath(mainImage, mainImageUrl, mainImagePath);
        String sub1 = resolveImagePath(subImage1, subImage1Url, subImage1Path);
        String sub2 = resolveImagePath(subImage2, subImage2Url, subImage2Path);

        if (main != null) product.setMainImageUrl(main);
        if (sub1 != null) product.setSubImage1Url(sub1);
        if (sub2 != null) product.setSubImage2Url(sub2);
    }

    /**
     * Resolves the best available image source using simple priority:
     * uploaded file > web URL > local/relative path.
     */
    private String resolveImagePath(MultipartFile file, String webUrl, String localPath) {
        // Priority 1: uploaded file
        if (file != null && !file.isEmpty()) {
            try {
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/products/");
                java.nio.file.Files.createDirectories(uploadPath);
                String original = file.getOriginalFilename();
                String ext = (original != null && original.contains("."))
                        ? original.substring(original.lastIndexOf('.'))
                        : ".jpg";
                String filename = java.util.UUID.randomUUID() + ext;
                java.nio.file.Files.copy(file.getInputStream(),
                        uploadPath.resolve(filename),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return "/uploads/products/" + filename;
            } catch (java.io.IOException e) {
                // fall through to next option
            }
        }
        // Priority 2: web URL
        if (webUrl != null && !webUrl.isBlank()
                && (webUrl.trim().startsWith("http://") || webUrl.trim().startsWith("https://"))) {
            return webUrl.trim();
        }
        // Priority 3: local / web-relative path
        if (localPath != null && !localPath.isBlank()) {
            return localPath.trim();
        }
        return null;
    }
}
