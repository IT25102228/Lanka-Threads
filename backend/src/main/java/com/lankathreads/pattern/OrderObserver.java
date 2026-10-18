package com.lankathreads.pattern;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: OBSERVER PATTERN (Behavioral Design Pattern)
 * Role: Observer Interface
 * Context: Lanka Threads Customer Order Lifecycle Notifications
 * ════════════════════════════════════════════════════════════════════════════════
 *
 * Pattern Intent:
 *   Define a one-to-many dependency between objects so that when one object
 *   (the Subject) changes state, all its dependents (Observers) are notified
 *   and updated automatically.
 *
 * Why Observer fits the Customer Module:
 *   A customer order goes through multiple lifecycle stages:
 *       PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *   Each transition triggers different cross-cutting concerns:
 *     • Logging the status change for auditing
 *     • Notifying the customer (email / SMS / in-app)
 *     • Restoring inventory stock when an order is cancelled
 *
 *   Without Observer, all of this logic would be crammed into the service
 *   method, violating the Single Responsibility Principle and making it
 *   hard to add or remove reactions to order events.
 *
 * Participants (GoF):
 *   Subject       → OrderEventPublisher   (maintains observer list, fires events)
 *   Observer      → OrderObserver          (this interface)
 *   ConcreteObs.  → OrderStatusLogger      (logs every transition)
 *                 → CustomerNotifier       (sends notifications to the customer)
 *                 → StockRestorationObserver (restores stock on cancellation)
 *   Event/State   → OrderEvent            (carries order + old/new status)
 *
 * Adding a new reaction (e.g. analytics, loyalty points) requires only a
 * new class implementing this interface — no existing code changes.
 * ════════════════════════════════════════════════════════════════════════════════
 */
public interface OrderObserver {

    /**
     * Called by the OrderEventPublisher whenever an order status transition occurs.
     *
     * @param event Snapshot of the order and the status transition (previous → new)
     */
    void onOrderStatusChanged(OrderEvent event);
}
