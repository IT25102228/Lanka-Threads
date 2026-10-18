package com.lankathreads.service;

import com.lankathreads.model.Inventory;
import com.lankathreads.pattern.InventoryStrategy;
import com.lankathreads.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    @Autowired
    private Map<String, InventoryStrategy> strategyMap;

    @Autowired
    private InventoryRepository inventoryRepository;

    public double calculateTotalValue(Inventory item, String strategyType) {
        InventoryStrategy strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            strategy = strategyMap.get("standard"); // fallback default
        }
        return strategy.calculateValue(item);
    }

    public void receiveApprovedStock(String fabricType, String colour, Double quantityReceived, String lotNumber, String rollNumber) {
        Inventory item = new Inventory();
        item.setFabricType(fabricType);
        item.setColour(colour);
        item.setQuantityAvailable(quantityReceived != null ? quantityReceived : 0.0);
        item.setReorderLevel(100.0);
        item.setLotNumber(lotNumber != null && !lotNumber.isBlank() ? lotNumber : "LOT-" + System.currentTimeMillis());
        item.setRollNumber(rollNumber != null && !rollNumber.isBlank() ? rollNumber : "ROL-" + System.currentTimeMillis());
        item.setStatus("Approved");
        item.setRequisitionLine("None");
        item.setCategory("Men");

        inventoryRepository.save(item);
    }

    // Methods required by InventoryController
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public void saveInventory(Inventory inventory) {
        if (inventory != null) {
            if (inventory.getReorderLevel() == null) {
                inventory.setReorderLevel(100.0);
            }
            if (inventory.getQuantityAvailable() == null) {
                inventory.setQuantityAvailable(0.0);
            }
            if (inventory.getStatus() == null || inventory.getStatus().isBlank()) {
                inventory.setStatus("Approved");
            }
            if (inventory.getRollNumber() == null || inventory.getRollNumber().isBlank()) {
                inventory.setRollNumber("ROL-" + System.currentTimeMillis());
            }
            if (inventory.getLotNumber() == null || inventory.getLotNumber().isBlank()) {
                inventory.setLotNumber("LOT-" + System.currentTimeMillis());
            }
            if (inventory.getRequisitionLine() == null || inventory.getRequisitionLine().isBlank()) {
                inventory.setRequisitionLine("None");
            }
            if (inventory.getCategory() == null || inventory.getCategory().isBlank()) {
                inventory.setCategory("Men");
            }
            inventoryRepository.save(inventory);
        }
    }

    public void deleteInventory(Long id) {
        if (id != null) {
            inventoryRepository.deleteById(id);
        }
    }
}