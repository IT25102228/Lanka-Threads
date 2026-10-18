package com.lankathreads.pattern;

import com.lankathreads.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Stock Audit Log (Sales Module).
 *
 * Records a structured INFO-level audit trail entry every time a product's
 * stock quantity is modified. This provides a lightweight, append-only audit
 * log of all stock mutations without touching the database schema or frontend.
 *
 * Design Pattern: Observer (GoF Behavioural)
 * Role: Concrete Observer
 */
@Component
public class StockAuditLogObserver implements ProductStockObserver {

    private static final Logger log = LoggerFactory.getLogger(StockAuditLogObserver.class);

    /**
     * Fires whenever the Subject detects a product stock change.
     * Writes a structured audit entry: product identity, old quantity, new
     * quantity, and the net delta so that changes can be traced over time.
     *
     * @param product      the product whose stock has changed
     * @param oldQuantity  stock quantity before the update
     * @param newQuantity  stock quantity after the update
     */
    @Override
    public void onStockChanged(Product product, int oldQuantity, int newQuantity) {
        int delta = newQuantity - oldQuantity;
        String direction = delta >= 0 ? "+" : "";

        log.info(
            "[STOCK AUDIT] Product '{}' (ID: {}, Category: {}) — Stock changed: {} → {} (Delta: {}{})",
            product.getName(),
            product.getId(),
            product.getCategory(),
            oldQuantity,
            newQuantity,
            direction,
            delta
        );
    }
}
