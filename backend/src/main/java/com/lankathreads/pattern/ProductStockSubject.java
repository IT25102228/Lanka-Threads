package com.lankathreads.pattern;

import com.lankathreads.model.Product;

/**
 * Observer Pattern — Subject Interface (Sales Module).
 *
 * Defines the contract for any Subject that manages and notifies
 * ProductStockObservers about stock change events.
 *
 * Design Pattern: Observer (GoF Behavioural)
 * Role: Subject
 */
public interface ProductStockSubject {

    /**
     * Registers an observer to receive stock change notifications.
     *
     * @param observer the observer to register
     */
    void addObserver(ProductStockObserver observer);

    /**
     * Removes a previously registered observer.
     *
     * @param observer the observer to remove
     */
    void removeObserver(ProductStockObserver observer);

    /**
     * Notifies all registered observers about a stock change event.
     *
     * @param product      the product whose stock changed
     * @param oldQuantity  stock quantity before the change
     * @param newQuantity  stock quantity after the change
     */
    void notifyObservers(Product product, int oldQuantity, int newQuantity);
}
