package com.lankathreads.service;

import com.lankathreads.model.Customer;
import com.lankathreads.model.Order;
import com.lankathreads.model.OrderItem;
import com.lankathreads.repository.OrderRepository;
import com.lankathreads.model.Product;
import com.lankathreads.repository.ProductRepository;
import com.lankathreads.model.OrderStatus;
import com.lankathreads.pattern.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing Customer Order placement, stock deduction, order cancellation, and stock restoration.
 */
@SuppressWarnings("null")
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired(required = false)
    private OrderEventPublisher orderEventPublisher;

    /**
     * Processes customer checkout:
     * - Generates order tracking code (ORD-XXXXXX)
     * - Validates sufficient stock for each item
     * - Deducts product stock in real time
     * - Saves order and order items
     * - Publishes OrderStatus change event to registered observers
     */
    @Transactional
    public Order processCheckout(Order order) {
        // Step 1: Generate unique order tracking code if missing
        if (order.getOrderCode() == null || order.getOrderCode().isBlank()) {
            order.setOrderCode(generateUniqueOrderCode());
        }

        // Step 2: Validate and deduct stock for each order line item
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // Step 3: Persist order
        Order savedOrder = orderRepository.save(order);

        // Step 4: Notify observers of confirmed order placement
        if (orderEventPublisher != null) {
            orderEventPublisher.publishStatusChange(savedOrder, OrderStatus.PENDING, OrderStatus.CONFIRMED);
        }

        return savedOrder;
    }

    /**
     * Cancels an order on customer request and publishes cancellation event to observers
     * (triggering StockRestorationObserver, OrderStatusLogger, and CustomerNotifier).
     */
    @Transactional
    public Order cancelOrderForCustomer(Long orderId, Customer customer) {
        // Step 1: Find order and verify customer ownership
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getCustomer() == null || customer == null || !order.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not allowed to cancel this order");
        }

        // Step 2: Ensure order is not already shipped or cancelled
        String status = (order.getStatus() == null) ? "" : order.getStatus().toLowerCase();
        if (status.contains("shipped") || status.contains("cancelled")) {
            throw new RuntimeException("This order can no longer be cancelled");
        }

        OrderStatus prevStatus = order.getOrderStatus();

        // Step 3: Mark order status as Cancelled and persist
        order.setStatus("Cancelled");
        Order savedOrder = orderRepository.save(order);

        // Step 4: Notify observers (StockRestorationObserver restores stock, Logger records audit, Notifier informs customer)
        if (orderEventPublisher != null) {
            orderEventPublisher.publishStatusChange(savedOrder, prevStatus, OrderStatus.CANCELLED);
        }

        return savedOrder;
    }

    /**
     * Saves or updates an order directly.
     */
    public Order placeOrder(Order order) {
        if (order.getOrderCode() == null || order.getOrderCode().isBlank()) {
            order.setOrderCode(generateUniqueOrderCode());
        }
        return orderRepository.save(order);
    }

    /**
     * Generates a unique 6-digit order code in format "ORD-XXXXXX".
     */
    private String generateUniqueOrderCode() {
        String code;
        do {
            int randomNum = (int) (Math.random() * 900000 + 100000); // 6-digit number
            code = "ORD-" + randomNum;
        } while (orderRepository.findByOrderCode(code).isPresent());
        return code;
    }
    
    /**
     * Retrieves all customer orders.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Finds an order by primary key ID.
     */
    @Nullable
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves orders placed by a specific customer, newest first.
     */
    public List<Order> getOrdersForCustomer(Customer customer) {
        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }
}

