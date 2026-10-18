package com.lankathreads.controller;

import com.lankathreads.model.Order;
import com.lankathreads.service.OrderService;
import com.lankathreads.model.Inventory;
import com.lankathreads.service.InventoryService;
import com.lankathreads.model.Worker;
import com.lankathreads.service.WorkerService;
import com.lankathreads.model.ProductionLog;
import com.lankathreads.service.ProductionLogService;
import com.lankathreads.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for Factory Manager:
 * Factory-wide analytics dashboard and factory worker roster management.
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductionLogService productionLogService;

    @Autowired
    private SupplierService supplierService;

    /**
     * Factory Manager Dashboard:
     * Calculates and presents high-level KPIs across Workers, Production, Inventory, and Sales.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // --- 1. Worker Workforce Analytics ---
        List<Worker> allWorkers = workerService.getAllWorkers();
        model.addAttribute("totalWorkers", allWorkers.size());
        model.addAttribute("workers", allWorkers);

        long sewingCount = 0;
        long checkingCount = 0;
        long ironingCount = 0;
        long cuttingCount = 0;
        long packingCount = 0;
        long morningCount = 0;
        long eveningCount = 0;
        long nightCount = 0;

        for (Worker worker : allWorkers) {
            String skill = worker.getSkill();
            String shift = worker.getShift();

            if ("Sewing".equalsIgnoreCase(skill)) sewingCount++;
            if ("Checking".equalsIgnoreCase(skill)) checkingCount++;
            if ("Ironing".equalsIgnoreCase(skill)) ironingCount++;
            if ("Cutting".equalsIgnoreCase(skill)) cuttingCount++;
            if ("Packing".equalsIgnoreCase(skill)) packingCount++;

            if ("Morning".equalsIgnoreCase(shift)) morningCount++;
            if ("Evening".equalsIgnoreCase(shift)) eveningCount++;
            if ("Night".equalsIgnoreCase(shift)) nightCount++;
        }

        model.addAttribute("sewingCount", sewingCount);
        model.addAttribute("checkingCount", checkingCount);
        model.addAttribute("ironingCount", ironingCount);
        model.addAttribute("cuttingCount", cuttingCount);
        model.addAttribute("packingCount", packingCount);
        model.addAttribute("morningCount", morningCount);
        model.addAttribute("eveningCount", eveningCount);
        model.addAttribute("nightCount", nightCount);

        // --- 2. Production Logs Analytics ---
        List<ProductionLog> logs = productionLogService.getAllLogs();
        model.addAttribute("logs", logs);

        int totalProduction = 0;
        int menProduction = 0;
        int womenProduction = 0;
        int childrenProduction = 0;

        for (ProductionLog log : logs) {
            totalProduction += log.getQuantityProduced();
            if ("Men".equalsIgnoreCase(log.getCategory())) menProduction += log.getQuantityProduced();
            if ("Women".equalsIgnoreCase(log.getCategory())) womenProduction += log.getQuantityProduced();
            if ("Children".equalsIgnoreCase(log.getCategory())) childrenProduction += log.getQuantityProduced();
        }

        model.addAttribute("totalProduction", totalProduction);
        model.addAttribute("menProduction", menProduction);
        model.addAttribute("womenProduction", womenProduction);
        model.addAttribute("childrenProduction", childrenProduction);

        // --- 3. Inventory Stock Analytics ---
        double totalFabricStock = 0.0;
        List<Inventory> lowStockList = new ArrayList<>();
        List<Inventory> allInventory = inventoryService.getAllInventory();

        for (Inventory item : allInventory) {
            if (item.getQuantityAvailable() != null) {
                totalFabricStock += item.getQuantityAvailable();
                if (item.getReorderLevel() != null && item.getQuantityAvailable() <= item.getReorderLevel()) {
                    lowStockList.add(item);
                }
            }
        }
        model.addAttribute("fabricStock", totalFabricStock);
        model.addAttribute("lowStock", lowStockList);
        model.addAttribute("inventoryList", allInventory);

        // --- 4. Sales & Customer Orders Analytics ---
        BigDecimal totalSales = BigDecimal.ZERO;
        long pendingOrdersCount = 0;
        List<Order> allOrders = orderService.getAllOrders();

        for (Order order : allOrders) {
            String status = order.getStatus();
            if (status != null && !status.equalsIgnoreCase("Cancelled")) {
                if (order.getTotalAmount() != null) {
                    totalSales = totalSales.add(order.getTotalAmount());
                }
            }
            if (status != null && status.toLowerCase().contains("pending")) {
                pendingOrdersCount++;
            }
        }

        model.addAttribute("totalSales", totalSales);
        model.addAttribute("orderCount", allOrders.size());
        model.addAttribute("pendingOrdersCount", pendingOrdersCount);
        model.addAttribute("supplierCount", supplierService.getAllSuppliers().size());
        
        return "manager/dashboard";
    }

    /**
     * Factory Worker Management Page:
     * Lists all workers and provides the next sequential Worker ID for new registrations.
     */
    @GetMapping("/workers")
    public String workers(Model model) {
        List<Worker> allWorkers = workerService.getAllWorkers();
        model.addAttribute("workers", allWorkers);
        model.addAttribute("totalWorkers", allWorkers.size());
        model.addAttribute("totalPersonnel", allWorkers.size());
        
        // Count unique shifts and skills
        Set<String> distinctShifts = new HashSet<>();
        Set<String> distinctSkills = new HashSet<>();

        for (Worker w : allWorkers) {
            if (w.getShift() != null && !w.getShift().trim().isEmpty()) {
                distinctShifts.add(w.getShift().trim());
            }
            if (w.getSkill() != null && !w.getSkill().trim().isEmpty()) {
                distinctSkills.add(w.getSkill().trim());
            }
        }

        int activeShiftsCount = distinctShifts.isEmpty() ? 3 : distinctShifts.size();
        int skillsUpdatedRecently = distinctSkills.isEmpty() ? 12 : distinctSkills.size() * 2;

        model.addAttribute("shiftsInProgress", activeShiftsCount);
        model.addAttribute("skillsUpdatedRecently", skillsUpdatedRecently);
        model.addAttribute("nextWorkerId", workerService.generateNextWorkerId());
        return "manager/workers";
    }

    /**
     * Adds a new worker to the factory roster.
     */
    @PostMapping("/workers/add")
    public String addWorker(Worker worker, RedirectAttributes redirectAttributes) {
        try {
            // Validate mobile number if provided
            if (worker.getMobileNumber() != null && !worker.getMobileNumber().trim().isEmpty()) {
                if (!workerService.isValidMobileNumber(worker.getMobileNumber())) {
                    redirectAttributes.addFlashAttribute("error", "Invalid mobile number format. Please enter a valid Sri Lankan mobile number (e.g. +94 77 123 4567 or 0771234567).");
                    return "redirect:/manager/workers";
                }
            }
            workerService.saveWorker(worker);
            redirectAttributes.addFlashAttribute("success", "Worker '" + worker.getName() + "' added successfully with ID " + worker.getWorkerId() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add worker: " + e.getMessage());
        }
        return "redirect:/manager/workers";
    }

    /**
     * Updates details of an existing factory worker.
     */
    @PostMapping("/workers/update")
    public String updateWorker(Worker worker, RedirectAttributes redirectAttributes) {
        try {
            // Validate mobile number if provided
            if (worker.getMobileNumber() != null && !worker.getMobileNumber().trim().isEmpty()) {
                if (!workerService.isValidMobileNumber(worker.getMobileNumber())) {
                    redirectAttributes.addFlashAttribute("error", "Invalid mobile number format. Please enter a valid Sri Lankan mobile number (e.g. +94 77 123 4567 or 0771234567).");
                    return "redirect:/manager/workers";
                }
            }
            workerService.saveWorker(worker);
            redirectAttributes.addFlashAttribute("success", "Worker '" + worker.getName() + "' (" + worker.getWorkerId() + ") updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update worker: " + e.getMessage());
        }
        return "redirect:/manager/workers";
    }

    /**
     * Deletes a worker from the system.
     */
    @GetMapping("/workers/delete/{id}")
    public String deleteWorker(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Worker worker = workerService.getWorkerById(id);
            String name = (worker != null) ? worker.getName() : "Worker";
            workerService.deleteWorker(id);
            redirectAttributes.addFlashAttribute("success", name + " deleted successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete worker because they are associated with existing records (e.g., production logs).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete worker: " + e.getMessage());
        }
        return "redirect:/manager/workers";
    }
}
