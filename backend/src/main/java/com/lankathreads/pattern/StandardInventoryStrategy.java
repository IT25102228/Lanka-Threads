package com.lankathreads.pattern;

import com.lankathreads.model.Inventory;
import org.springframework.stereotype.Component;

@Component("standard")
public class StandardInventoryStrategy implements InventoryStrategy {
    @Override
    public double calculateValue(Inventory item) {
        // Assuming a standard unit price of 150.0 per unit
        double unitPrice = 150.0;
        double quantity = item.getQuantityAvailable() != null ? item.getQuantityAvailable() : 0.0;
        return unitPrice * quantity;
    }
}