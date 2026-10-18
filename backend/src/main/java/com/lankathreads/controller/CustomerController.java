package com.lankathreads.controller;

import com.lankathreads.util.ValidationUtils;
import com.lankathreads.model.Customer;
import com.lankathreads.model.Order;
import com.lankathreads.model.OrderItem;
import com.lankathreads.service.CustomerService;
import com.lankathreads.service.OrderService;
import com.lankathreads.model.Product;
import com.lankathreads.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for Customer Portal:
 * Product browsing & catalog filtering, Shopping Cart, Direct "Buy Now", Checkout, and Order History.
 */
@Controller
@RequestMapping("/customer")
@SessionAttributes({"cart", "checkoutCart"})
public class CustomerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomerService customerService;

    @Autowired
    private com.lankathreads.repository.CartItemRepository cartItemRepository;

    private Optional<Customer> getAuthenticatedCustomer(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return customerService.getCustomerByUsernameOrEmail(auth.getName());
        }
        return Optional.empty();
    }

    private void syncCartFromDatabase(List<OrderItem> cart, Authentication auth) {
        getAuthenticatedCustomer(auth).ifPresent(customer -> {
            List<com.lankathreads.model.CartItem> dbItems = cartItemRepository.findByCustomer(customer);
            if (!dbItems.isEmpty()) {
                cart.clear();
                for (var ci : dbItems) {
                    if (ci.getProduct() != null) {
                        OrderItem item = new OrderItem();
                        item.setProduct(ci.getProduct());
                        item.setQuantity(ci.getQuantity());
                        item.setUnitPrice(ci.getProduct().getPrice());
                        cart.add(item);
                    }
                }
            } else if (!cart.isEmpty()) {
                for (OrderItem oi : cart) {
                    if (oi.getProduct() != null) {
                        cartItemRepository.save(new com.lankathreads.model.CartItem(customer, oi.getProduct(), oi.getQuantity()));
                    }
                }
            }
        });
    }

    /**
     * Initializes the standard shopping cart in the session.
     */
    @ModelAttribute("cart")
    public List<OrderItem> cart() {
        return new ArrayList<>();
    }

    /**
     * Initializes the direct "Buy Now" single-item checkout cart in the session.
     */
    @ModelAttribute("checkoutCart")
    public List<OrderItem> checkoutCart() {
        return new ArrayList<>();
    }

    /**
     * Helper: calculates total currency value for a list of order items.
     */
    private BigDecimal calculateTotal(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (OrderItem item : items) {
                if (item.getUnitPrice() != null) {
                    BigDecimal itemSubtotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                    total = total.add(itemSubtotal);
                }
            }
        }
        return total;
    }

    /**
     * Helper: picks items from checkoutCart (for Buy Now) or cart (for Cart Checkout).
     */
    private List<OrderItem> resolveCheckoutItems(List<OrderItem> cart, List<OrderItem> checkoutCart, boolean directCheckout) {
        return directCheckout ? checkoutCart : cart;
    }

    // =========================================================================
    // 1. CUSTOMER DASHBOARD & SHOP CATALOG
    // =========================================================================

    /**
     * Customer Home / Dashboard: displays the 4 latest featured products.
     */
    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute("cart") List<OrderItem> cart, Authentication auth, Model model) {
        syncCartFromDatabase(cart, auth);
        List<Product> allProducts = productService.getAllProducts();
        List<Product> latestProducts = new ArrayList<>(allProducts);

        // Sort descending by product ID (newest first)
        latestProducts.sort((a, b) -> {
            Long aId = a.getId();
            Long bId = b.getId();
            if (aId == null && bId == null) return 0;
            if (aId == null) return 1;
            if (bId == null) return -1;
            return bId.compareTo(aId);
        });

        if (latestProducts.size() > 4) {
            latestProducts = latestProducts.subList(0, 4);
        }

        model.addAttribute("latestProducts", latestProducts);
        return "customer/dashboard";
    }

    /**
     * Product Shop / Catalog:
     * Supports search keywords, category filtering, colour selection, and size selection.
     */
    @GetMapping("/shop")
    public String shop(@RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "") String colour,
                       @RequestParam(defaultValue = "") String size,
                       @ModelAttribute("cart") List<OrderItem> cart,
                       Authentication auth,
                       Model model) {
        syncCartFromDatabase(cart, auth);
        List<Product> allProducts = productService.searchProducts(search, category, colour, size);

        // Separate products into tabs by demographic category
        List<Product> mensProducts = new ArrayList<>();
        List<Product> womensProducts = new ArrayList<>();
        List<Product> childrenProducts = new ArrayList<>();

        for (Product p : allProducts) {
            String cat = p.getCategory();
            if ("Men".equalsIgnoreCase(cat)) {
                mensProducts.add(p);
            } else if ("Women".equalsIgnoreCase(cat)) {
                womensProducts.add(p);
            } else if ("Children".equalsIgnoreCase(cat)) {
                childrenProducts.add(p);
            }
        }

        // Collect all distinct colours and sizes available in the catalog
        List<Product> fullCatalog = productService.getAllProducts();
        Set<String> colourSet = new TreeSet<>();
        Set<String> sizeSet = new TreeSet<>();

        for (Product p : fullCatalog) {
            if (p.getColour() != null && !p.getColour().trim().isEmpty()) {
                colourSet.add(p.getColour().trim());
            }
            if (p.getSize() != null && !p.getSize().trim().isEmpty()) {
                sizeSet.add(p.getSize().trim());
            }
        }

        model.addAttribute("products", allProducts);
        model.addAttribute("mensProducts", mensProducts);
        model.addAttribute("womensProducts", womensProducts);
        model.addAttribute("childrenProducts", childrenProducts);
        model.addAttribute("availableColours", new ArrayList<>(colourSet));
        model.addAttribute("availableSizes", new ArrayList<>(sizeSet));
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("colour", colour);
        model.addAttribute("size", size);
        return "customer/shop";
    }

    // =========================================================================
    // 2. SHOPPING CART OPERATIONS
    // =========================================================================

    /**
     * Adds an item with quantity to the customer's session shopping cart.
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, @RequestParam Integer quantity,
                            @ModelAttribute("cart") List<OrderItem> cart,
                            Authentication auth,
                            RedirectAttributes redirect) {
        Product product = productService.getProductById(productId);
        if (product != null && quantity != null && quantity > 0 && quantity <= product.getStockQuantity()) {
            // Check if product is already in the cart
            for (OrderItem existing : cart) {
                if (existing.getProduct().getId().equals(productId)) {
                    int newQuantity = existing.getQuantity() + quantity;
                    if (newQuantity <= product.getStockQuantity()) {
                        existing.setQuantity(newQuantity);
                        getAuthenticatedCustomer(auth).ifPresent(cust -> {
                            cartItemRepository.findByCustomerIdAndProductId(cust.getId(), productId).ifPresentOrElse(
                                dbItem -> {
                                    dbItem.setQuantity(newQuantity);
                                    cartItemRepository.save(dbItem);
                                },
                                () -> cartItemRepository.save(new com.lankathreads.model.CartItem(cust, product, newQuantity))
                            );
                        });
                        redirect.addFlashAttribute("success", "Cart updated.");
                    } else {
                        redirect.addFlashAttribute("error", "Requested quantity exceeds available stock.");
                    }
                    return "redirect:/customer/shop";
                }
            }

            // Create new order item line
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            cart.add(item);

            getAuthenticatedCustomer(auth).ifPresent(cust -> {
                cartItemRepository.save(new com.lankathreads.model.CartItem(cust, product, quantity));
            });

            redirect.addFlashAttribute("success", "Product added to cart.");
        } else {
            redirect.addFlashAttribute("error", "Invalid quantity or insufficient stock.");
        }
        return "redirect:/customer/shop";
    }

    /**
     * Removes an item from the shopping cart.
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, 
                                 @ModelAttribute("cart") List<OrderItem> cart,
                                 Authentication auth) {
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
        getAuthenticatedCustomer(auth).ifPresent(cust -> {
            cartItemRepository.deleteByCustomerIdAndProductId(cust.getId(), productId);
        });
        return "redirect:/customer/cart";
    }

    /**
     * Displays the current shopping cart page.
     */
    @GetMapping("/cart")
    public String viewCart(@ModelAttribute("cart") List<OrderItem> cart, Authentication auth, Model model) {
        syncCartFromDatabase(cart, auth);
        model.addAttribute("total", calculateTotal(cart));
        return "customer/cart";
    }

    /**
     * Empties the current shopping cart.
     */
    @GetMapping("/cart/clear")
    public String clearCart(@ModelAttribute("cart") List<OrderItem> cart, Authentication auth) {
        cart.clear();
        getAuthenticatedCustomer(auth).ifPresent(cust -> {
            cartItemRepository.deleteByCustomer(cust);
        });
        return "redirect:/customer/cart";
    }

    // =========================================================================
    // 3. CHECKOUT & PAYMENT PROCESSING
    // =========================================================================

    /**
     * "Buy Now" button: skips standard cart and enters direct checkout for 1 product.
     */
    @PostMapping("/buy-now")
    public String buyNow(@RequestParam Long productId,
                         @RequestParam(defaultValue = "1") Integer quantity,
                         @ModelAttribute("checkoutCart") List<OrderItem> checkoutCart,
                         RedirectAttributes redirect) {
        Product product = productService.getProductById(productId);
        if (product != null && quantity != null && quantity > 0 && quantity <= product.getStockQuantity()) {
            checkoutCart.clear();

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            checkoutCart.add(item);

            return "redirect:/customer/checkout/direct";
        } else {
            redirect.addFlashAttribute("error", "Invalid quantity or insufficient stock.");
            return "redirect:/customer/shop";
        }
    }

    /**
     * Entry router to direct checkout or cart checkout.
     */
    @GetMapping("/checkout")
    public String checkoutEntry(@ModelAttribute("cart") List<OrderItem> cart,
                                @ModelAttribute("checkoutCart") List<OrderItem> checkoutCart,
                                Authentication auth) {
        syncCartFromDatabase(cart, auth);
        if (!checkoutCart.isEmpty()) {
            return "redirect:/customer/checkout/direct";
        }
        if (!cart.isEmpty()) {
            return "redirect:/customer/cart?checkout=true";
        }
        return "redirect:/customer/shop";
    }

    /**
     * Direct checkout page for "Buy Now" purchases.
     */
    @GetMapping("/checkout/direct")
    public String checkoutDirect(@ModelAttribute("checkoutCart") List<OrderItem> checkoutCart, Model model,
                                 RedirectAttributes redirect) {
        if (checkoutCart.isEmpty()) {
            redirect.addFlashAttribute("error", "No direct checkout item found. Please try again.");
            return "redirect:/customer/shop";
        }

        model.addAttribute("checkoutItems", checkoutCart);
        model.addAttribute("total", calculateTotal(checkoutCart));
        model.addAttribute("directCheckout", true);
        return "customer/checkout";
    }

    /**
     * Submits order checkout:
     * - Validates delivery address and Sri Lankan phone
     * - Validates payment details (Credit/Debit Card, Bank Transfer, KOKO, COD)
     * - Verifies real-time stock availability
     * - Saves order and deducts inventory stock
     */
    @PostMapping("/checkout")
    public String checkout(@RequestParam(defaultValue = "Credit / Debit Card") String paymentMethod,
                           @RequestParam(required = false) String cardNumber,
                           @RequestParam(required = false) String cardExpiry,
                           @RequestParam(required = false) String cardCvv,
                           @RequestParam(required = false) String bankRef,
                           @RequestParam(required = false) String shippingAddress,
                           @RequestParam(required = false) String phone,
                           @RequestParam(defaultValue = "false") boolean directCheckout,
                           @ModelAttribute("cart") List<OrderItem> cart, 
                           @ModelAttribute("checkoutCart") List<OrderItem> checkoutCart,
                           Authentication auth,
                           RedirectAttributes redirect) {
        // Step 1: Ensure cart has items
        List<OrderItem> items = resolveCheckoutItems(cart, checkoutCart, directCheckout);
        if (items.isEmpty()) {
            return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart";
        }

        // Step 2: Validate delivery address and contact phone number
        if (shippingAddress == null || shippingAddress.isBlank() || phone == null || phone.isBlank()) {
            redirect.addFlashAttribute("error", "Shipping address and phone number are required.");
            return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
        }

        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            redirect.addFlashAttribute("error", "Enter a valid Sri Lankan mobile number, such as +94 77 123 4567.");
            return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
        }

        // Step 3: Validate Payment Details
        String cleanCard = (cardNumber != null) ? cardNumber.replaceAll("\\s+", "").trim() : "";
        if ("Credit / Debit Card".equals(paymentMethod)) {
            if (cleanCard.length() != 16 || !cleanCard.matches("\\d+")) {
                redirect.addFlashAttribute("error", "Invalid Card Number. Must be exactly 16 digits.");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
            }
            if (cardExpiry == null || !cardExpiry.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
                redirect.addFlashAttribute("error", "Invalid Card Expiry Date. Format must be MM/YY (e.g. 08/28).");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
            }
            try {
                String[] expParts = cardExpiry.trim().split("/");
                int expMonth = Integer.parseInt(expParts[0]);
                int expYear = 2000 + Integer.parseInt(expParts[1]);
                java.time.YearMonth expiry = java.time.YearMonth.of(expYear, expMonth);
                java.time.YearMonth currentMonth = java.time.YearMonth.now();

                if (expiry.isBefore(currentMonth)) {
                    redirect.addFlashAttribute("error", "Card has expired. Please enter a valid non-expired card.");
                    return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
                }
                if (expiry.isAfter(currentMonth.plusYears(15))) {
                    redirect.addFlashAttribute("error", "Invalid Card Expiry Year. Year must be within the next 15 years.");
                    return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
                }
            } catch (Exception e) {
                redirect.addFlashAttribute("error", "Invalid Card Expiry Date.");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
            }
            if (cardCvv == null || cardCvv.trim().length() != 3 || !cardCvv.trim().matches("\\d+")) {
                redirect.addFlashAttribute("error", "Invalid CVV. Must be exactly 3 digits.");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
            }
        } else if ("Direct Bank Transfer".equals(paymentMethod)) {
            if (bankRef == null || bankRef.trim().length() < 6) {
                redirect.addFlashAttribute("error", "Bank transfer reference must be at least 6 characters.");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
            }
        }

        if (auth == null) {
            return "redirect:/login";
        }

        // Step 4: Resolve authenticated Customer
        Customer customer = customerService.getCustomerByUsernameOrEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Customer account not found."));

        // Step 5: Construct Order
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setPhone(phone);
        order.setPaymentMethod(paymentMethod);

        // Determine status tag based on payment method
        String cleanStatus;
        if ("Credit / Debit Card".equals(paymentMethod)) {
            String suffix = cleanCard.substring(12);
            cleanStatus = "Paid - Card (Ending: " + suffix + ")";
            order.setPaymentReference("Card Ending: " + suffix);
        } else if ("Direct Bank Transfer".equals(paymentMethod)) {
            cleanStatus = "Pending Verification - Bank Ref: " + bankRef.trim();
            order.setPaymentReference(bankRef.trim());
        } else if ("KOKO Installments (3x)".equals(paymentMethod)) {
            cleanStatus = "Paid - KOKO Installment 1/3 (Approved)";
            order.setPaymentReference("KOKO-INST-1");
        } else {
            cleanStatus = "Pending COD Confirmation";
            order.setPaymentReference("COD");
        }
        order.setStatus(cleanStatus);
        
        // Calculate items and total amount
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product current = productService.getProductById(item.getProduct().getId());
            if (current == null || item.getQuantity() <= 0 || item.getQuantity() > current.getStockQuantity()) {
                redirect.addFlashAttribute("error", "Stock changed. Please review your cart.");
                return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart";
            }
            item.setProduct(current);
            item.setUnitPrice(current.getPrice());
            item.setOrder(order);
            total = total.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setOrderItems(new ArrayList<>(items));
        order.setTotalAmount(total);
        
        // Step 6: Process checkout (deduct stock and save order)
        try {
            orderService.processCheckout(order);
            if (directCheckout) {
                checkoutCart.clear();
            } else {
                cart.clear();
                getAuthenticatedCustomer(auth).ifPresent(cust -> cartItemRepository.deleteByCustomer(cust));
            }
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", "Checkout failed: " + e.getMessage());
            return directCheckout ? "redirect:/customer/checkout/direct" : "redirect:/customer/cart?checkout=true";
        }
        
        return "redirect:/customer/orders";
    }

    // =========================================================================
    // 4. ORDER HISTORY & CANCELLATION
    // =========================================================================

    /**
     * Lists all orders placed by the currently logged-in customer.
     */
    @GetMapping("/orders")
    public String orders(Model model, Authentication auth) {
        Customer customer = customerService.getCustomerByUsernameOrEmail(auth.getName()).orElseThrow();
        model.addAttribute("orders", orderService.getOrdersForCustomer(customer));
        return "customer/orders";
    }

    /**
     * Cancels an order and restores product quantities back to stock.
     */
    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam Long id,
                              Authentication auth,
                              RedirectAttributes redirect) {
        if (auth == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.getCustomerByUsernameOrEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Customer account not found."));

        try {
            orderService.cancelOrderForCustomer(id, customer);
            redirect.addFlashAttribute("success", "Order cancelled successfully.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/orders";
    }

    @GetMapping("/support")
    public String customerSupport() {
        return "support";
    }
}
