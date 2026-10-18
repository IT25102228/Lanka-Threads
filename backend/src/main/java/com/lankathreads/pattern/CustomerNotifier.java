package com.lankathreads.pattern;

import com.lankathreads.model.Order;
import com.lankathreads.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN — Concrete Observer #2
 * Role: Customer Notification Dispatcher
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * Reacts to order status transitions by generating customer-facing notification
 * messages. In a production system this would send emails or SMS. For Lanka
 * Threads, we log the notification message that would be sent.
 *
 * Different transitions produce different messages:
 *   • PENDING → CONFIRMED  : "Your order has been confirmed!"
 *   • CONFIRMED → PROCESSING : "Your order is being prepared."
 *   • PROCESSING → SHIPPED : "Your order is on its way!"
 *   • SHIPPED → DELIVERED   : "Your order has been delivered."
 *   • Any → CANCELLED       : "Your order has been cancelled."
 * ════════════════════════════════════════════════════════════════════════════════
 */
@Component
public class CustomerNotifier implements OrderObserver {

    private static final Logger logger = LoggerFactory.getLogger(CustomerNotifier.class);

    @Override
    public void onOrderStatusChanged(OrderEvent event) {
        Order order = event.getOrder();
        OrderStatus newStatus = event.getNewStatus();

        if (order.getCustomer() == null) {
            return; // Cannot notify if no customer is linked
        }

        String customerName = order.getCustomer().getName();
        String customerEmail = order.getCustomer().getEmail();
        String orderCode = (order.getOrderCode() != null) ? order.getOrderCode() : "N/A";
        String message = buildNotificationMessage(orderCode, newStatus);

        // In production: integrate email/SMS service here (e.g. JavaMailSender, Twilio)
        logger.info("[CUSTOMER NOTIFICATION] To: {} ({}), Subject: Order {} Update — {}",
                customerName, customerEmail, orderCode, message);
    }

    /**
     * Builds a customer-friendly notification message based on the new status.
     */
    private String buildNotificationMessage(String orderCode, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                return "Your order " + orderCode + " has been confirmed! We're getting it ready.";
            case PROCESSING:
                return "Your order " + orderCode + " is now being prepared by our team.";
            case SHIPPED:
                return "Great news! Your order " + orderCode + " has been shipped and is on its way.";
            case DELIVERED:
                return "Your order " + orderCode + " has been delivered. Thank you for shopping with Lanka Threads!";
            case CANCELLED:
                return "Your order " + orderCode + " has been cancelled. A refund will be processed if applicable.";
            default:
                return "Your order " + orderCode + " status has been updated to: " + newStatus.getDisplayName();
        }
    }
}
