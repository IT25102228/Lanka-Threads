package com.lankathreads.controller;

import com.lankathreads.util.ValidationUtils;
import com.lankathreads.model.MaterialLog;
import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import com.lankathreads.pattern.SupplierEvaluationResult;
import com.lankathreads.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Supplier Coordination Officer:
 * Supplier relationships, fabric material receipt intake, quality inspections, and shipment history.
 */
@Controller
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // =========================================================================
    // 1. SUPPLIER COORDINATION DASHBOARD
    // =========================================================================

    /**
     * Supplier Officer Dashboard:
     * Summarizes pending quality inspections, approved/rejected batch counts, and recent audit logs.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ReceivedMaterial> pendingMaterials = supplierService.getPendingInspections();
        List<ReceivedMaterial> allMaterials = supplierService.getAllReceivedMaterials();
        List<MaterialLog> allLogs = supplierService.getAllMaterialLogs();

        // Step 1: Count approved and rejected materials
        long approvedCount = 0;
        long rejectedCount = 0;

        for (ReceivedMaterial m : allMaterials) {
            if ("APPROVED".equalsIgnoreCase(m.getStatus())) {
                approvedCount++;
            } else if ("REJECTED".equalsIgnoreCase(m.getStatus())) {
                rejectedCount++;
            }
        }

        // Step 2: Extract top 6 recent logs
        List<MaterialLog> recentLogs = new ArrayList<>();
        for (int i = 0; i < allLogs.size() && i < 6; i++) {
            recentLogs.add(allLogs.get(i));
        }

        model.addAttribute("pendingMaterials", pendingMaterials);
        model.addAttribute("pendingSuppliers", pendingMaterials); // compatibility alias
        model.addAttribute("allMaterialsCount", allMaterials.size());
        model.addAttribute("pendingCount", pendingMaterials.size());
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalSuppliersCount", supplierService.getDistinctSuppliers().size());
        model.addAttribute("recentLogs", recentLogs);
        return "supplier/dashboard";
    }

    // =========================================================================
    // 2. MATERIAL SHIPMENT INTAKE & MANAGEMENT
    // =========================================================================

    /**
     * Material shipment intake and inspection desk.
     */
    @GetMapping("/manage")
    public String manage(Model model) {
        List<ReceivedMaterial> receivedMaterials = supplierService.getAllReceivedMaterials();
        List<Supplier> registeredSuppliers = supplierService.getDistinctSuppliers();
        List<MaterialLog> materialLogs = supplierService.getAllMaterialLogs();

        model.addAttribute("receivedMaterials", receivedMaterials);
        model.addAttribute("suppliers", receivedMaterials); // alias for backwards template compatibility
        model.addAttribute("registeredSuppliers", registeredSuppliers);
        model.addAttribute("materialLogs", materialLogs);

        model.addAttribute("nextReceiptId", supplierService.generateNextReceiptId());
        model.addAttribute("nextPoNumber", supplierService.generateNextPoNumber());
        model.addAttribute("nextSupplierCode", supplierService.generateNextSupplierCode());

        return "supplier/manage";
    }

    /**
     * Records an incoming material batch from a vendor.
     */
    @PostMapping({"/receive", "/add"})
    public String receiveMaterial(ReceivedMaterial material,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String officer = (authentication != null) ? authentication.getName() : "Supplier Coordination Officer";
            ReceivedMaterial saved = supplierService.receiveMaterial(material, officer);
            redirectAttributes.addFlashAttribute("success",
                    "Material Receipt " + saved.getReceiptId() + " (PO: " + saved.getPoNumber() + ") recorded & logged successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to receive material: " + e.getMessage());
        }
        return "redirect:/supplier/manage";
    }

    /**
     * Updates details of an existing material shipment record.
     */
    @PostMapping("/update")
    public String updateReceivedMaterial(ReceivedMaterial material,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        try {
            ReceivedMaterial existing = supplierService.getReceivedMaterialById(material.getId());
            if (existing != null) {
                existing.setFabricType(material.getFabricType());
                existing.setColour(material.getColour());
                existing.setQuantityReceived(material.getQuantityReceived());
                existing.setPrice(material.getPrice());
                existing.setExpectedDeliveryDate(material.getExpectedDeliveryDate());
                existing.setSupplierName(material.getSupplierName());
                existing.setContactInfo(material.getContactInfo());
                if (material.getRollNumber() != null) existing.setRollNumber(material.getRollNumber());
                if (material.getLotNumber() != null) existing.setLotNumber(material.getLotNumber());
                if (material.getNotes() != null) existing.setNotes(material.getNotes());

                String officer = (authentication != null) ? authentication.getName() : "Officer";
                supplierService.receiveMaterial(existing, officer);
                redirectAttributes.addFlashAttribute("success", "Material record " + existing.getReceiptId() + " updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Material record not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update material: " + e.getMessage());
        }
        return "redirect:/supplier/manage";
    }

    /**
     * Deletes a material shipment record.
     */
    @GetMapping("/delete/{id}")
    public String deleteReceivedMaterial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteReceivedMaterial(id);
            redirectAttributes.addFlashAttribute("success", "Material receipt record deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/supplier/manage";
    }

    // =========================================================================
    // 3. MASTER SUPPLIER DIRECTORY
    // =========================================================================

    /**
     * Lists registered suppliers.
     */
    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        List<Supplier> registeredSuppliers = supplierService.getDistinctSuppliers();
        List<ReceivedMaterial> allMaterials = supplierService.getAllReceivedMaterials();

        model.addAttribute("registeredSuppliers", registeredSuppliers);
        model.addAttribute("suppliers", registeredSuppliers);
        model.addAttribute("totalSuppliersCount", registeredSuppliers.size());
        model.addAttribute("allMaterialsCount", allMaterials.size());
        model.addAttribute("nextSupplierCode", supplierService.generateNextSupplierCode());
        model.addAttribute("nextReceiptId", supplierService.generateNextReceiptId());
        model.addAttribute("nextPoNumber", supplierService.generateNextPoNumber());
        model.addAttribute("evaluations", supplierService.evaluateAllSuppliers("BALANCED"));
        model.addAttribute("availableStrategies", supplierService.getAvailableEvaluationStrategies());

        return "supplier/suppliers";
    }

    /**
     * Adds a new vendor profile to the directory.
     */
    @PostMapping("/supplier/add")
    public String addSupplier(Supplier supplier, RedirectAttributes redirectAttributes) {
        try {
            if (supplier.getContactInfo() == null || !ValidationUtils.isValidContactInfo(supplier.getContactInfo())) {
                redirectAttributes.addFlashAttribute("error", "Please provide a valid contact phone number or email address (e.g. 0112345678 / sales@vendor.lk).");
                return "redirect:/supplier/suppliers";
            }
            if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isBlank()) {
                supplier.setSupplierCode(supplierService.generateNextSupplierCode());
            }
            if (supplier.getStatus() == null || supplier.getStatus().isBlank()) {
                supplier.setStatus("ACTIVE");
            }
            supplierService.saveSupplier(supplier);
            redirectAttributes.addFlashAttribute("success", "Supplier " + supplier.getName() + " (" + supplier.getSupplierCode() + ") registered successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to register supplier: " + e.getMessage());
        }
        return "redirect:/supplier/suppliers";
    }

    /**
     * Updates an existing supplier profile.
     */
    @PostMapping("/supplier/update")
    public String updateSupplier(Supplier supplier, RedirectAttributes redirectAttributes) {
        try {
            if (supplier.getContactInfo() != null && !supplier.getContactInfo().isBlank() && !ValidationUtils.isValidContactInfo(supplier.getContactInfo())) {
                redirectAttributes.addFlashAttribute("error", "Please provide a valid contact phone number or email address.");
                return "redirect:/supplier/suppliers";
            }
            Supplier existing = supplierService.getSupplierById(supplier.getId());
            if (existing != null) {
                existing.setName(supplier.getName());
                existing.setContactInfo(supplier.getContactInfo());
                existing.setAddress(supplier.getAddress());
                if (supplier.getFabricTypeSupplied() != null) existing.setFabricTypeSupplied(supplier.getFabricTypeSupplied());
                if (supplier.getStatus() != null) existing.setStatus(supplier.getStatus());

                supplierService.saveSupplier(existing);
                redirectAttributes.addFlashAttribute("success", "Supplier " + existing.getName() + " updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Supplier not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update supplier: " + e.getMessage());
        }
        return "redirect:/supplier/suppliers";
    }

    /**
     * Deletes a supplier from the directory.
     */
    @GetMapping("/supplier/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Supplier removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete supplier: " + e.getMessage());
        }
        return "redirect:/supplier/suppliers";
    }

    // =========================================================================
    // 4. QUALITY INSPECTION
    // =========================================================================

    /**
     * Inspects a material batch (APPROVED / REJECTED) and transfers approved fabric into inventory.
     */
    @PostMapping("/inspect")
    public String inspectMaterial(@RequestParam Long id,
                                  @RequestParam String status,
                                  @RequestParam(required = false) String remarks,
                                  @RequestParam(required = false, defaultValue = "manage") String redirectPage,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String officer = (authentication != null) ? authentication.getName() : "Quality Officer";
            ReceivedMaterial updated = supplierService.inspectMaterial(id, status, officer, remarks);
            if (updated != null) {
                String extraMsg = "APPROVED".equalsIgnoreCase(status) ? " Added to Raw Material Inventory." : "";
                redirectAttributes.addFlashAttribute("success",
                        "Material Receipt " + updated.getReceiptId() + " (" + updated.getPoNumber() + ") marked as " + status + "." + extraMsg);
            } else {
                redirectAttributes.addFlashAttribute("error", "Material receipt not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to inspect material: " + e.getMessage());
        }

        return "dashboard".equalsIgnoreCase(redirectPage) ? "redirect:/supplier/dashboard" : "redirect:/supplier/manage";
    }

    // =========================================================================
    // 5. SUPPLIER EVALUATION (STRATEGY PATTERN)
    // =========================================================================

    /**
     * REST endpoint evaluating an individual supplier's performance via the Strategy Pattern.
     * Strategy can be dynamically switched via query param: "QUALITY", "PUNCTUALITY", "BALANCED".
     *
     * Example: GET /supplier/evaluate/SUP-001?strategy=QUALITY
     */
    @GetMapping("/evaluate/{supplierCode}")
    @ResponseBody
    public SupplierEvaluationResult evaluateSupplier(
            @PathVariable String supplierCode,
            @RequestParam(required = false, defaultValue = "BALANCED") String strategy) {
        return supplierService.evaluateSupplier(supplierCode, strategy);
    }

    /**
     * REST endpoint returning performance scorecard evaluation reports for all suppliers.
     *
     * Example: GET /supplier/evaluate/all?strategy=BALANCED
     */
    @GetMapping("/evaluate/all")
    @ResponseBody
    public List<SupplierEvaluationResult> evaluateAllSuppliers(
            @RequestParam(required = false, defaultValue = "BALANCED") String strategy) {
        return supplierService.evaluateAllSuppliers(strategy);
    }
}
