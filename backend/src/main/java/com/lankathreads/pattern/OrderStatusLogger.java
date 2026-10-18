package com.lankathreads.pattern;

import com.lankathreads.model.Order;
import com.lankathreads.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN — Concrete Observer #1
 * Role: Audit Logger
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * Logs every order status transition to the application log for auditing
 * and traceability. This ensures Lanka Threads has a complete history
 * of all order state changes without coupling the logging logic into
 * the OrderService.
 * ════════════════════════════════════════════════════════════════════════════════
 */
@Component
public class OrderStatusLogger implements OrderObserver {

    private static final Logger logger = LoggerFactory.getLogger(OrderStatusLogger.class);

    @Override
    public void onOrderStatusChanged(OrderEvent event) {
        Order order = event.getOrder();
        OrderStatus prev = event.getPreviousStatus();
        OrderStatus next = event.getNewStatus();

        String customerName = (order.getCustomer() != null) ? order.getCustomer().getName() : "Unknown";
        String orderCode = (order.getOrderCode() != null) ? order.getOrderCode() : "N/A";

        logger.info("[ORDER AUDIT] Order {} (Customer: {}) status changed: {} → {}",
                orderCode, customerName, prev.getDisplayName(), next.getDisplayName());

        if (next == OrderStatus.CANCELLED) {
            logger.warn("[ORDER AUDIT] Order {} has been CANCELLED. Items: {}, Total: Rs.{}",
                    orderCode,
                    order.getOrderItems() != null ? order.getOrderItems().size() : 0,
                    order.getTotalAmount());
        }

        if (next == OrderStatus.DELIVERED) {
            logger.info("[ORDER AUDIT] Order {} successfully DELIVERED to customer {}.",
                    orderCode, customerName);
        }
    }
}
