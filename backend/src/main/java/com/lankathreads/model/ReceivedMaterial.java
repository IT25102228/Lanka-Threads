package com.lankathreads.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "received_materials")
public class ReceivedMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptId; // e.g. MR-2026-001

    @Column(nullable = false)
    private String poNumber; // e.g. PO-2026-001

    @Column(nullable = false)
    private String supplierCode; // e.g. SUP-001

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = true)
    private String contactInfo;

    @Column(nullable = false)
    private String fabricType;

    @Column(nullable = false)
    private String colour;

    @Column(nullable = false)
    private Double quantityReceived; // kg or meters

    @Column(nullable = false)
    private BigDecimal price; // Total cost LKR

    @Column(nullable = false)
    private String status = "PENDING_INSPECTION"; // PENDING_INSPECTION, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDate receivedDate = LocalDate.now();

    @Column(nullable = true)
    private LocalDate expectedDeliveryDate;

    @Column(nullable = true)
    private String rollNumber = "R-1001";

    @Column(nullable = true)
    private String lotNumber = "LOT-2002";

    @Column(nullable = true, length = 500)
    private String notes;

    public ReceivedMaterial() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public Double getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Double quantityReceived) { this.quantityReceived = quantityReceived; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
