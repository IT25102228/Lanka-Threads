package com.lankathreads.pattern;

import com.lankathreads.model.Order;
import com.lankathreads.model.OrderStatus;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN (Behavioral Design Pattern)
 * Role: Event / Subject State Snapshot
 * Context: Lanka Threads Customer Order Lifecycle Notifications
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * OrderEvent is the "state change" object that the Subject (OrderEventPublisher)
 * broadcasts to all registered Observers (OrderObserver implementations).
 *
 * It carries a snapshot of:
 *  - The Order that changed
 *  - The previous OrderStatus (before the transition)
 *  - The new OrderStatus (after the transition)
 *
 * This allows each observer to react based on the type of transition
 * (e.g. PENDING → CONFIRMED, PROCESSING → SHIPPED, etc.).
 * ════════════════════════════════════════════════════════════════════════════════
 */
public class OrderEvent {

    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    public OrderEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    /** The order whose status has changed. */
    public Order getOrder() {
        return order;
    }

    /** The status the order had before the transition. */
    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    /** The status the order now holds after the transition. */
    public OrderStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public String toString() {
        return String.format("OrderEvent[order=%s, %s → %s]",
                order.getOrderCode(), previousStatus, newStatus);
    }
}
