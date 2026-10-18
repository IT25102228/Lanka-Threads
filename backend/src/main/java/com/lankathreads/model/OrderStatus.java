package com.lankathreads.model;

/**
 * Represents the lifecycle stages of a customer order.
 * Flow: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 * An order can be CANCELLED from PENDING, CONFIRMED, or PROCESSING states.
 */
public enum OrderStatus {
    PENDING("Pending", "Order placed, awaiting confirmation", 1),
    CONFIRMED("Confirmed", "Order confirmed by sales team", 2),
    PROCESSING("Processing", "Order is being prepared", 3),
    SHIPPED("Shipped", "Order dispatched for delivery", 4),
    DELIVERED("Delivered", "Order delivered to customer", 5),
    CANCELLED("Cancelled", "Order has been cancelled", 0);

    private final String displayName;
    private final String description;
    private final int stepNumber;

    OrderStatus(String displayName, String description, int stepNumber) {
        this.displayName = displayName;
        this.description = description;
        this.stepNumber = stepNumber;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getStepNumber() { return stepNumber; }

    /**
     * Check if the order can transition to the given target status.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this == CANCELLED || this == DELIVERED) {
            return false; // Terminal states
        }
        if (target == CANCELLED) {
            return this == PENDING || this == CONFIRMED || this == PROCESSING;
        }
        // Forward-only progression
        return target.stepNumber == this.stepNumber + 1;
    }

    /**
     * Check if the customer can cancel the order in this status.
     */
    public boolean isCancellableByCustomer() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Parse a status string into an OrderStatus, with fallback handling
     * for legacy status strings from old data.
     */
    public static OrderStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return PENDING;
        }
        String s = status.trim().toUpperCase();
        try {
            return valueOf(s);
        } catch (IllegalArgumentException ignored) {}

        // Handle legacy status strings
        String lower = status.trim().toLowerCase();
        if (lower.contains("pending")) return PENDING;
        if (lower.contains("confirmed")) return CONFIRMED;
        if (lower.contains("processing") || lower.contains("paid")) return PROCESSING;
        if (lower.contains("shipped")) return SHIPPED;
        if (lower.contains("delivered") || lower.contains("completed")) return DELIVERED;
        if (lower.contains("cancelled") || lower.contains("canceled")) return CANCELLED;

        return PENDING;
    }
}
