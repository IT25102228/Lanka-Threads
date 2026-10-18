package com.lankathreads.pattern;

import com.lankathreads.model.Product;

/**
 * Observer Pattern — Observer Interface (Sales Module).
 *
 * Any class that wants to be notified of Product stock changes must implement
 * this interface. This keeps stock-change listeners decoupled from ProductService.
 *
 * Design Pattern: Observer (GoF Behavioural)
 * Role: Observer
 */
public interface ProductStockObserver {

    /**
     * Called by the Subject whenever a product's stock quantity changes.
     *
     * @param product      the product whose stock has been updated
     * @param oldQuantity  the stock quantity before the change
     * @param newQuantity  the stock quantity after the change
     */
    void onStockChanged(Product product, int oldQuantity, int newQuantity);
}
