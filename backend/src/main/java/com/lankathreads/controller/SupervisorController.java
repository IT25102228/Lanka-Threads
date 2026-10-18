package com.lankathreads.controller;

import com.lankathreads.model.User;
import com.lankathreads.service.UserService;
import com.lankathreads.model.Worker;
import com.lankathreads.service.WorkerService;
import com.lankathreads.model.ProductionLog;
import com.lankathreads.pattern.ProductionLogBuilder;
import com.lankathreads.service.ProductionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

/**
 * Controller for Production Supervisor:
 * Production Floor dashboard, Worker Shift scheduling, and Daily Production Log recording.
 */
@Controller
@RequestMapping("/supervisor")
public class SupervisorController {

    @Autowired
    private WorkerService workerService;
    
    @Autowired
    private ProductionLogService productionLogService;
    
    @Autowired
    private UserService userService;

    /**
     * Supervisor Dashboard:
     * Calculates line output targets vs actual production, efficiency percentage, and downtime incidents.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        List<ProductionLog> logs = productionLogService.getAllLogs();
        List<Worker> workers = workerService.getAllWorkers();
        
        model.addAttribute("logs", logs);
        model.addAttribute("workers", workers);
        model.addAttribute("totalWorkers", workers.size());
        
        // Step 1: Calculate active production lines, target outputs, and downtime exceptions
        Set<String> activeLines = new HashSet<>();
        int totalTarget = 0;
        int totalActual = 0;
        List<ProductionLog> downtimeLogs = new ArrayList<>();

        for (ProductionLog log : logs) {
            if (log.getProductionLine() != null && !log.getProductionLine().trim().isEmpty()) {
                activeLines.add(log.getProductionLine().trim());
            }
            totalTarget += log.getTargetQuantity();
            totalActual += log.getQuantityProduced();
            
            if (log.getDowntimeMinutes() != null && log.getDowntimeMinutes() > 0) {
                downtimeLogs.add(log);
            }
        }

        // Calculate overall efficiency percentage
        int overallEfficiency = 0;
        if (totalTarget > 0) {
            overallEfficiency = (totalActual * 100) / totalTarget;
        }
        
        model.addAttribute("activeLinesCount", activeLines.size());
        model.addAttribute("totalTarget", totalTarget);
        model.addAttribute("totalActual", totalActual);
        model.addAttribute("overallEfficiency", overallEfficiency);
        model.addAttribute("downtimeLogs", downtimeLogs);
        model.addAttribute("exceptionCount", downtimeLogs.size());
        
        // Step 2: Get supervisor's display name from authentication
        String supervisorName = "Production Desk";
        if (authentication != null) {
            Optional<User> optUser = userService.getUserByUsername(authentication.getName());
            if (optUser.isPresent()) {
                supervisorName = optUser.get().getName();
            }
        }
        model.addAttribute("supervisorName", supervisorName);
        
        return "supervisor/dashboard";
    }

    /**
     * Worker Shift & Line Scheduling page.
     */
    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("workers", workerService.getAllWorkers());
        return "supervisor/schedule";
    }

    /**
     * Updates assigned production line, category, and shift for a worker.
     */
    @PostMapping("/schedule/update")
    public String updateSchedule(Worker worker, RedirectAttributes redirectAttributes) {
        try {
            Worker existing = workerService.getWorkerById(worker.getId());
            if (existing != null) {
                existing.setCategoryAssigned(worker.getCategoryAssigned());
                existing.setShift(worker.getShift());
                existing.setActiveProductionLine(worker.getActiveProductionLine());
                workerService.saveWorker(existing);
                redirectAttributes.addFlashAttribute("success", "Worker schedule updated successfully for " + existing.getName() + ".");
            } else {
                redirectAttributes.addFlashAttribute("error", "Worker not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update schedule: " + e.getMessage());
        }
        return "redirect:/supervisor/schedule";
    }

    /**
     * Daily Production Log entries page.
     */
    @GetMapping("/production-log")
    public String productionLog(Model model) {
        model.addAttribute("logs", productionLogService.getAllLogs());
        return "supervisor/production-log";
    }

    /**
     * Records a new production output log entry with supervisor credentials and date.
     */
    @PostMapping("/production-log/add")
    public String addLog(ProductionLog log, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Builder Pattern: build a validated ProductionLog from the submitted form data
            ProductionLog newLog = new ProductionLogBuilder()
                    .date(LocalDate.now())
                    .category(log.getCategory())
                    .quantityProduced(log.getQuantityProduced())
                    .targetQuantity(log.getTargetQuantity())
                    .wipStage(log.getWipStage())
                    .downtime(log.getDowntimeMinutes(), log.getDowntimeReason())
                    .productionLine(log.getProductionLine())
                    .supervisor(user)
                    .build();

            productionLogService.saveLog(newLog);
            redirectAttributes.addFlashAttribute("success", "Production log recorded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to record log: " + e.getMessage());
        }
        return "redirect:/supervisor/production-log";
    }

    /**
     * Updates an existing production log entry.
     */
    @PostMapping("/production-log/update")
    public String updateLog(ProductionLog log, RedirectAttributes redirectAttributes) {
        try {
            ProductionLog existing = productionLogService.getLogById(log.getId());
            if (existing != null) {
                existing.setProductionLine(log.getProductionLine());
                existing.setCategory(log.getCategory());
                existing.setQuantityProduced(log.getQuantityProduced());
                existing.setTargetQuantity(log.getTargetQuantity());
                existing.setWipStage(log.getWipStage());
                existing.setDowntimeMinutes(log.getDowntimeMinutes());
                existing.setDowntimeReason(log.getDowntimeReason());
                productionLogService.saveLog(existing);
                redirectAttributes.addFlashAttribute("success", "Production log updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Production log not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update production log: " + e.getMessage());
        }
        return "redirect:/supervisor/production-log";
    }
}
