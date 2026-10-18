package com.lankathreads.pattern;

import com.lankathreads.model.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern — Concrete Subject: Product Stock Event Publisher (Sales Module).
 *
 * This is the central event hub for product stock changes. It maintains the
 * list of registered observers and broadcasts stock-change events to every
 * observer in the order they were registered.
 *
 * ProductService injects this Spring-managed bean and calls
 * {@link #notifyObservers(Product, int, int)} whenever a save operation
 * results in a stock quantity change. No frontend change is needed.
 *
 * Design Pattern: Observer (GoF Behavioural)
 * Role: Concrete Subject
 *
 * Registered observers (auto-wired via constructor):
 *   1. {@link LowStockAlertObserver}  — warns when stock ≤ threshold
 *   2. {@link StockAuditLogObserver}  — records every stock mutation
 */
@Component
public class ProductStockEventPublisher implements ProductStockSubject {

    private final List<ProductStockObserver> observers = new ArrayList<>();

    /**
     * Spring constructor-injection: all {@link ProductStockObserver} beans
     * discovered in the application context are registered automatically.
     *
     * @param observers list of all ProductStockObserver beans in the context
     */
    public ProductStockEventPublisher(List<ProductStockObserver> observers) {
        this.observers.addAll(observers);
    }

    /** {@inheritDoc} */
    @Override
    public void addObserver(ProductStockObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeObserver(ProductStockObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers about a product stock change.
     * Observers are called sequentially; exceptions from one observer do not
     * prevent the remaining observers from being notified.
     *
     * @param product      the product whose stock changed
     * @param oldQuantity  stock before the change
     * @param newQuantity  stock after the change
     */
    @Override
    public void notifyObservers(Product product, int oldQuantity, int newQuantity) {
        for (ProductStockObserver observer : observers) {
            try {
                observer.onStockChanged(product, oldQuantity, newQuantity);
            } catch (Exception e) {
                // Isolate individual observer failures so one bad observer
                // cannot block the rest or roll back the product save.
                org.slf4j.LoggerFactory
                    .getLogger(ProductStockEventPublisher.class)
                    .error("[Observer error] {} failed on stock event for product ID {}: {}",
                            observer.getClass().getSimpleName(), product.getId(), e.getMessage(), e);
            }
        }
    }
}
