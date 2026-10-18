package com.lankathreads.controller;

import com.lankathreads.model.Inventory;
import com.lankathreads.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Inventory Officer:
 * Warehouse stock dashboard, stock roll management, and low stock monitoring.
 */
@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Inventory Dashboard:
     * Computes healthy vs low stock roll counts.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Inventory> inventories = inventoryService.getAllInventory();
        model.addAttribute("inventories", inventories);
        
        // Count healthy and low stock items
        long healthyCount = 0;
        long lowStockCount = 0;

        for (Inventory item : inventories) {
            if (item.getQuantityAvailable() != null && item.getReorderLevel() != null) {
                if (item.getQuantityAvailable() > item.getReorderLevel()) {
                    healthyCount++;
                } else {
                    lowStockCount++;
                }
            }
        }
                
        model.addAttribute("healthyCount", healthyCount);
        model.addAttribute("lowStockCount", lowStockCount);
        return "inventory/dashboard";
    }

    /**
     * Stock Management Page: lists all inventory items for adding/updating.
     */
    @GetMapping("/manage")
    public String manage(Model model) {
        model.addAttribute("inventories", inventoryService.getAllInventory());
        return "inventory/manage";
    }

    /**
     * Adds a new fabric stock roll to inventory.
     */
    @PostMapping("/add")
    public String addInventory(Inventory inventory, RedirectAttributes redirectAttributes) {
        // Validate non-negative quantities
        if (inventory.getQuantityAvailable() == null || inventory.getReorderLevel() == null ||
            inventory.getQuantityAvailable() < 0 || inventory.getReorderLevel() < 0) {
            redirectAttributes.addFlashAttribute("error", "Quantities cannot be negative.");
            return "redirect:/inventory/manage";
        }
        try {
            inventoryService.saveInventory(inventory);
            redirectAttributes.addFlashAttribute("success", "Fabric stock roll " + inventory.getRollNumber() + " added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add inventory: " + e.getMessage());
        }
        return "redirect:/inventory/manage";
    }

    /**
     * Updates an existing fabric stock roll in inventory.
     */
    @PostMapping("/update")
    public String updateInventory(Inventory inventory, RedirectAttributes redirectAttributes) {
        // Validate non-negative quantities
        if (inventory.getQuantityAvailable() == null || inventory.getReorderLevel() == null ||
            inventory.getQuantityAvailable() < 0 || inventory.getReorderLevel() < 0) {
            redirectAttributes.addFlashAttribute("error", "Quantities cannot be negative.");
            return "redirect:/inventory/manage";
        }
        try {
            inventoryService.saveInventory(inventory);
            redirectAttributes.addFlashAttribute("success", "Fabric stock roll " + inventory.getRollNumber() + " updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update inventory: " + e.getMessage());
        }
        return "redirect:/inventory/manage";
    }

    /**
     * Deletes a fabric stock roll from inventory.
     */
    @GetMapping("/delete/{id}")
    public String deleteInventory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inventoryService.deleteInventory(id);
            redirectAttributes.addFlashAttribute("success", "Inventory stock record deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete inventory record: " + e.getMessage());
        }
        return "redirect:/inventory/manage";
    }
}
