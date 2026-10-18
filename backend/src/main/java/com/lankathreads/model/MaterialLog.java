package com.lankathreads.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_logs")
public class MaterialLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String receiptId; // e.g. MR-2026-001

    @Column(nullable = false)
    private String supplierCode; // e.g. SUP-001

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = false)
    private String action; // MATERIAL_RECEIVED, QUALITY_APPROVED, QUALITY_REJECTED, INVENTORY_RESTOCKED

    @Column(nullable = false, length = 500)
    private String details;

    @Column(nullable = true)
    private String fabricType;

    @Column(nullable = true)
    private String colour;

    @Column(nullable = true)
    private Double quantity;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String performedBy = "Supplier Coordination Officer";

    public MaterialLog() {}

    public MaterialLog(String receiptId, String supplierCode, String supplierName, String action, String details, String fabricType, String colour, Double quantity, String performedBy) {
        this.receiptId = receiptId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.action = action;
        this.details = details;
        this.fabricType = fabricType;
        this.colour = colour;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.performedBy = performedBy != null ? performedBy : "Supplier Coordination Officer";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
