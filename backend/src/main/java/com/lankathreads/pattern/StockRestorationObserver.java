package com.lankathreads.pattern;

import com.lankathreads.model.Order;
import com.lankathreads.model.OrderItem;
import com.lankathreads.model.OrderStatus;
import com.lankathreads.model.Product;
import com.lankathreads.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN — Concrete Observer #3
 * Role: Inventory Stock Restoration on Cancellation
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * When a customer cancels an order, the purchased product quantities must be
 * returned to the inventory so they can be purchased by other customers.
 *
 * This observer listens for transitions into the CANCELLED state and
 * automatically restores stock for each order item. Previously, this logic
 * was embedded directly inside OrderService.cancelOrderForCustomer(). By
 * extracting it into an observer, the cancellation logic follows the Single
 * Responsibility Principle and the stock restoration can be independently
 * tested, modified, or extended (e.g. partial cancellations in the future).
 * ════════════════════════════════════════════════════════════════════════════════
 */
@Component
public class StockRestorationObserver implements OrderObserver {

    private static final Logger logger = LoggerFactory.getLogger(StockRestorationObserver.class);

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void onOrderStatusChanged(OrderEvent event) {
        // Only act when an order transitions to CANCELLED
        if (event.getNewStatus() != OrderStatus.CANCELLED) {
            return;
        }

        Order order = event.getOrder();
        String orderCode = (order.getOrderCode() != null) ? order.getOrderCode() : "N/A";

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            logger.warn("[STOCK RESTORE] Order {} cancelled but has no items to restore.", orderCode);
            return;
        }

        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                continue;
            }

            Long productId = item.getProduct().getId();
            if (productId != null) {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null) {
                    int restoredQty = item.getQuantity();
                    product.setStockQuantity(product.getStockQuantity() + restoredQty);
                    productRepository.save(product);

                    logger.info("[STOCK RESTORE] Order {} cancelled → Restored {} units of '{}' (Product ID: {}). New stock: {}",
                            orderCode, restoredQty, product.getName(), product.getId(), product.getStockQuantity());
                }
            }
        }
    }
}
