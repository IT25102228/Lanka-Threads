package com.lankathreads.pattern;

import com.lankathreads.model.Inventory; // Make sure this matches your model import

public interface InventoryStrategy {
    double calculateValue(Inventory item);
}