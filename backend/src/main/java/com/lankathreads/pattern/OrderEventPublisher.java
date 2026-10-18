package com.lankathreads.pattern;

import com.lankathreads.model.Order;
import com.lankathreads.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN (Behavioral Design Pattern)
 * Role: Subject (Publisher / Event Source)
 * Context: Lanka Threads Customer Order Lifecycle Notifications
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * The Subject in the Observer pattern. It maintains a list of OrderObserver
 * instances and notifies all of them whenever an order undergoes a status
 * transition.
 *
 * Spring auto-wires all beans implementing OrderObserver, so adding a new
 * observer only requires creating a new @Component class — no changes
 * needed here or in the controller/service.
 *
 * Usage in OrderService:
 *   orderEventPublisher.publishStatusChange(order, oldStatus, newStatus);
 * ════════════════════════════════════════════════════════════════════════════════
 */
@Component
public class OrderEventPublisher {

    private final List<OrderObserver> observers;

    /**
     * Spring automatically injects every bean implementing OrderObserver.
     * If no observers are registered, an empty list is used.
     */
    @Autowired
    public OrderEventPublisher(List<OrderObserver> observers) {
        this.observers = (observers != null) ? new ArrayList<>(observers) : new ArrayList<>();
    }

    /**
     * Registers a new observer at runtime (for programmatic registration).
     *
     * @param observer The observer to add
     */
    public void subscribe(OrderObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer so it no longer receives notifications.
     *
     * @param observer The observer to remove
     */
    public void unsubscribe(OrderObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers about an order status change.
     *
     * @param order          The order whose status changed
     * @param previousStatus The status before the transition
     * @param newStatus      The status after the transition
     */
    public void publishStatusChange(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        OrderEvent event = new OrderEvent(order, previousStatus, newStatus);
        for (OrderObserver observer : observers) {
            observer.onOrderStatusChanged(event);
        }
    }

    /**
     * Returns the count of currently registered observers (for testing/debugging).
     */
    public int getObserverCount() {
        return observers.size();
    }
}
