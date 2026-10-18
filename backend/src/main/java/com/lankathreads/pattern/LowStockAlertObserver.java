package com.lankathreads.pattern;

import com.lankathreads.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Low Stock Alert (Sales Module).
 *
 * Monitors product stock levels and logs a warning whenever stock drops
 * below the defined LOW_STOCK_THRESHOLD. This observer is automatically
 * notified by ProductStockEventPublisher on every stock change without
 * requiring any modification to the frontend or Controller layer.
 *
 * Design Pattern: Observer (GoF Behavioural)
 * Role: Concrete Observer
 */
@Component
public class LowStockAlertObserver implements ProductStockObserver {

    private static final Logger log = LoggerFactory.getLogger(LowStockAlertObserver.class);

    /** Products with stock at or below this value trigger a low-stock alert. */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Fires when the observed Subject (ProductStockEventPublisher) detects a
     * stock change. If the new quantity is at or below the threshold, a
     * WARNING-level alert is written to the application log.
     *
     * @param product      the product whose stock has changed
     * @param oldQuantity  stock quantity before the update
     * @param newQuantity  stock quantity after the update
     */
    @Override
    public void onStockChanged(Product product, int oldQuantity, int newQuantity) {
        if (newQuantity <= LOW_STOCK_THRESHOLD) {
            log.warn(
                "[LOW STOCK ALERT] Product '{}' (ID: {}) — stock dropped to {} unit(s). "
                + "Immediate restock recommended. (Previous stock: {})",
                product.getName(),
                product.getId(),
                newQuantity,
                oldQuantity
            );
        }
    }
}
