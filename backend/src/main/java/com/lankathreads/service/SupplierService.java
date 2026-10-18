package com.lankathreads.service;

import com.lankathreads.service.InventoryService;
import com.lankathreads.model.MaterialLog;
import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import com.lankathreads.repository.MaterialLogRepository;
import com.lankathreads.repository.ReceivedMaterialRepository;
import com.lankathreads.repository.SupplierRepository;
import com.lankathreads.pattern.SupplierEvaluationContext;
import com.lankathreads.pattern.SupplierEvaluationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service managing Suppliers, Fabric Material Shipments, Quality Inspections, and Audit Logs.
 */
@SuppressWarnings("null")
@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ReceivedMaterialRepository receivedMaterialRepository;

    @Autowired
    private MaterialLogRepository materialLogRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SupplierEvaluationContext evaluationContext;

    // =========================================================================
    // 1. MASTER SUPPLIERS
    // =========================================================================

    /**
     * Retrieves all supplier profiles.
     */
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    /**
     * Retrieves a list of unique suppliers keyed by Supplier Code.
     */
    public List<Supplier> getDistinctSuppliers() {
        List<Supplier> all = supplierRepository.findAll();
        Map<String, Supplier> map = new LinkedHashMap<>();
        for (Supplier s : all) {
            if (s.getSupplierCode() != null && !s.getSupplierCode().trim().isEmpty()) {
                String code = s.getSupplierCode().trim().toUpperCase();
                if (!map.containsKey(code)) {
                    map.put(code, s);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Saves or updates a master supplier record.
     */
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    /**
     * Finds a supplier by database primary key ID.
     */
    @Nullable
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a supplier by database primary key ID.
     */
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    /**
     * Ensures a supplier entry exists in master supplier table; creates it if absent.
     */
    public Supplier ensureSupplierExists(String supplierCode, String name, String contactInfo) {
        if (supplierCode == null || supplierCode.isBlank()) {
            supplierCode = generateNextSupplierCode();
        }
        final String finalCode = supplierCode.trim().toUpperCase();
        Optional<Supplier> existing = supplierRepository.findBySupplierCode(finalCode);
        if (existing.isPresent()) {
            Supplier s = existing.get();
            if (name != null && !name.isBlank()) s.setName(name);
            if (contactInfo != null && !contactInfo.isBlank()) s.setContactInfo(contactInfo);
            return supplierRepository.save(s);
        } else {
            Supplier s = new Supplier(finalCode, name != null ? name : "Vendor " + finalCode, contactInfo != null ? contactInfo : "N/A");
            return supplierRepository.save(s);
        }
    }

    // =========================================================================
    // 2. RECEIVED MATERIAL BATCHES & INTAKE
    // =========================================================================

    /**
     * Retrieves all recorded fabric material shipments.
     */
    public List<ReceivedMaterial> getAllReceivedMaterials() {
        return receivedMaterialRepository.findAll();
    }

    /**
     * Retrieves material shipments currently awaiting quality inspection.
     */
    public List<ReceivedMaterial> getPendingInspections() {
        return receivedMaterialRepository.findByStatus("PENDING_INSPECTION");
    }

    /**
     * Finds a received material record by database primary key ID.
     */
    @Nullable
    public ReceivedMaterial getReceivedMaterialById(Long id) {
        return receivedMaterialRepository.findById(id).orElse(null);
    }

    /**
     * Records new material delivery from a supplier and logs the event:
     * - Auto-generates Receipt ID (MR-YYYY-001) and PO Number (PO-YYYY-001)
     * - Generates tracking Roll & Lot numbers
     * - Syncs supplier with master supplier directory
     * - Logs intake in Material Audit Log
     */
    @Transactional
    public ReceivedMaterial receiveMaterial(ReceivedMaterial material, String performedBy) {
        // Step 1: Generate missing tracking IDs
        if (material.getReceiptId() == null || material.getReceiptId().isBlank()) {
            material.setReceiptId(generateNextReceiptId());
        }
        if (material.getPoNumber() == null || material.getPoNumber().isBlank()) {
            material.setPoNumber(generateNextPoNumber());
        }
        if (material.getSupplierCode() == null || material.getSupplierCode().isBlank()) {
            material.setSupplierCode(generateNextSupplierCode());
        }
        if (material.getReceivedDate() == null) {
            material.setReceivedDate(LocalDate.now());
        }
        if (material.getExpectedDeliveryDate() == null) {
            material.setExpectedDeliveryDate(material.getReceivedDate().plusDays(7));
        }
        if (material.getStatus() == null || material.getStatus().isBlank()) {
            material.setStatus("PENDING_INSPECTION");
        }

        if (material.getRollNumber() == null || material.getRollNumber().isBlank()) {
            material.setRollNumber("R-" + (int)(1000 + Math.random() * 9000));
        }
        if (material.getLotNumber() == null || material.getLotNumber().isBlank()) {
            material.setLotNumber("LOT-" + (int)(100 + Math.random() * 900));
        }

        // Step 2: Ensure master supplier record exists
        ensureSupplierExists(material.getSupplierCode(), material.getSupplierName(), material.getContactInfo());

        // Step 3: Save material batch
        ReceivedMaterial saved = receivedMaterialRepository.save(material);

        // Step 4: Write to Material Intake Log
        String logDetails = String.format("Material batch received (Receipt: %s, PO: %s) - %s kg of %s (%s) from %s [%s]",
                saved.getReceiptId(), saved.getPoNumber(), saved.getQuantityReceived(),
                saved.getFabricType(), saved.getColour(), saved.getSupplierName(), saved.getSupplierCode());

        logMaterialAction(
                saved.getReceiptId(),
                saved.getSupplierCode(),
                saved.getSupplierName(),
                "MATERIAL_RECEIVED",
                logDetails,
                saved.getFabricType(),
                saved.getColour(),
                saved.getQuantityReceived(),
                performedBy != null ? performedBy : "Supplier Coordination Officer"
        );

        return saved;
    }

    /**
     * Conducts quality inspection (APPROVED or REJECTED):
     * - If APPROVED: automatically transfers material into Factory Raw Material Inventory
     * - Logs quality inspection audit record
     */
    @Transactional
    public ReceivedMaterial inspectMaterial(Long id, String status, String officerUsername, String remarks) {
        ReceivedMaterial mat = receivedMaterialRepository.findById(id).orElse(null);
        if (mat == null) return null;

        // Step 1: Update status
        mat.setStatus(status);
        ReceivedMaterial updated = receivedMaterialRepository.save(mat);

        // Step 2: Log inspection action
        String action = "APPROVED".equalsIgnoreCase(status) ? "QUALITY_APPROVED" : "QUALITY_REJECTED";
        String logDetails = String.format("Quality Inspection %s for Receipt %s (%s kg %s %s). Remarks: %s",
                status, mat.getReceiptId(), mat.getQuantityReceived(), mat.getFabricType(), mat.getColour(),
                remarks != null && !remarks.isBlank() ? remarks : "Standard Inspection Passed");

        logMaterialAction(
                mat.getReceiptId(),
                mat.getSupplierCode(),
                mat.getSupplierName(),
                action,
                logDetails,
                mat.getFabricType(),
                mat.getColour(),
                mat.getQuantityReceived(),
                officerUsername != null ? officerUsername : "Quality Inspection Officer"
        );

        // Step 3: If approved, automatically transfer stock to Raw Materials Inventory
        if ("APPROVED".equalsIgnoreCase(status)) {
            inventoryService.receiveApprovedStock(
                    mat.getFabricType(),
                    mat.getColour(),
                    mat.getQuantityReceived(),
                    mat.getLotNumber(),
                    mat.getRollNumber()
            );

            logMaterialAction(
                    mat.getReceiptId(),
                    mat.getSupplierCode(),
                    mat.getSupplierName(),
                    "INVENTORY_RESTOCKED",
                    String.format("Added %s kg of %s (%s) to Active Raw Material Inventory",
                            mat.getQuantityReceived(), mat.getFabricType(), mat.getColour()),
                    mat.getFabricType(),
                    mat.getColour(),
                    mat.getQuantityReceived(),
                    "Inventory System"
            );
        }

        return updated;
    }

    /**
     * Deletes a material receipt record.
     */
    public void deleteReceivedMaterial(Long id) {
        receivedMaterialRepository.deleteById(id);
    }

    // =========================================================================
    // 3. MATERIAL AUDIT LOGS
    // =========================================================================

    /**
     * Retrieves all material intake and inspection audit logs (newest first).
     */
    public List<MaterialLog> getAllMaterialLogs() {
        return materialLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Appends an entry into the Material Audit Log.
     */
    public MaterialLog logMaterialAction(String receiptId, String supplierCode, String supplierName, String action,
                                        String details, String fabricType, String colour, Double quantity, String performedBy) {
        MaterialLog log = new MaterialLog(receiptId, supplierCode, supplierName, action, details, fabricType, colour, quantity, performedBy);
        return materialLogRepository.save(log);
    }

    // =========================================================================
    // 4. SEQUENTIAL ID GENERATORS
    // =========================================================================

    /**
     * Helper to extract the numeric suffix from strings like "SUP-005" or "MR-2026-012".
     */
    private int extractTrailingNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] parts = text.trim().split("-");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Generates next supplier code: SUP-001, SUP-002, etc.
     */
    public synchronized String generateNextSupplierCode() {
        List<Supplier> suppliers = supplierRepository.findAll();
        int max = 0;
        for (Supplier s : suppliers) {
            int num = extractTrailingNumber(s.getSupplierCode());
            if (num > max) {
                max = num;
            }
        }
        return String.format("SUP-%03d", max + 1);
    }

    /**
     * Generates next material receipt ID: MR-2026-001, MR-2026-002, etc.
     */
    public synchronized String generateNextReceiptId() {
        List<ReceivedMaterial> materials = receivedMaterialRepository.findAll();
        int year = LocalDate.now().getYear();
        int max = 0;
        for (ReceivedMaterial m : materials) {
            int num = extractTrailingNumber(m.getReceiptId());
            if (num > max) {
                max = num;
            }
        }
        return String.format("MR-%d-%03d", year, max + 1);
    }

    /**
     * Generates next Purchase Order number: PO-2026-001, PO-2026-002, etc.
     */
    public synchronized String generateNextPoNumber() {
        List<ReceivedMaterial> materials = receivedMaterialRepository.findAll();
        int year = LocalDate.now().getYear();
        int max = 0;
        for (ReceivedMaterial m : materials) {
            int num = extractTrailingNumber(m.getPoNumber());
            if (num > max) {
                max = num;
            }
        }
        for (Supplier s : supplierRepository.findAll()) {
            int num = extractTrailingNumber(s.getPoNumber());
            if (num > max) {
                max = num;
            }
        }
        return String.format("PO-%d-%03d", year, max + 1);
    }

    // =========================================================================
    // 5. SUPPLIER EVALUATION (STRATEGY PATTERN)
    // =========================================================================

    /**
     * Evaluates a single supplier's performance using the specified Strategy.
     * Uses the Strategy Pattern to select Quality, Punctuality, or Balanced evaluation.
     *
     * @param supplierCode The vendor's code (e.g. SUP-001)
     * @param strategyKey  Strategy key: "QUALITY", "PUNCTUALITY", or "BALANCED"
     * @return Evaluation report containing calculated score, rating tier, and metrics breakdown
     */
    public SupplierEvaluationResult evaluateSupplier(String supplierCode, String strategyKey) {
        Supplier supplier = supplierRepository.findBySupplierCode(supplierCode)
                .orElseGet(() -> new Supplier(supplierCode, "Vendor " + supplierCode, "N/A"));
        List<ReceivedMaterial> history = receivedMaterialRepository.findBySupplierCode(supplierCode);
        return evaluationContext.evaluate(supplier, history, strategyKey);
    }

    /**
     * Evaluates all registered suppliers using the chosen evaluation strategy.
     */
    public List<SupplierEvaluationResult> evaluateAllSuppliers(String strategyKey) {
        List<Supplier> suppliers = getDistinctSuppliers();
        List<SupplierEvaluationResult> results = new ArrayList<>();
        for (Supplier s : suppliers) {
            List<ReceivedMaterial> history = receivedMaterialRepository.findBySupplierCode(s.getSupplierCode());
            results.add(evaluationContext.evaluate(s, history, strategyKey));
        }
        return results;
    }

    /**
     * Retrieves all registered evaluation strategies for UI selection.
     */
    public Map<String, String> getAvailableEvaluationStrategies() {
        return evaluationContext.getAvailableStrategies();
    }
}
