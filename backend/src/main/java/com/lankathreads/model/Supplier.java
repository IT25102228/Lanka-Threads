package com.lankathreads.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String supplierCode; // SUP-001

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactInfo;

    @Column(nullable = true)
    private String address = "Colombo, Sri Lanka";

    @Column(nullable = true)
    private String fabricTypeSupplied = "Cotton";

    @Column(nullable = true)
    private String colourSupplied = "Multi";

    @Column(nullable = true)
    private Double quantitySupplied = 0.0;

    @Column(nullable = true)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, APPROVED, PENDING_INSPECTION

    @Column(nullable = true)
    private String poNumber = "PO-2026-001";

    @Column(nullable = true)
    private LocalDate expectedDeliveryDate = LocalDate.now().plusDays(7);

    public Supplier() {}

    public Supplier(String supplierCode, String name, String contactInfo) {
        this.supplierCode = supplierCode;
        this.name = name;
        this.contactInfo = contactInfo;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFabricTypeSupplied() { return fabricTypeSupplied; }
    public void setFabricTypeSupplied(String fabricTypeSupplied) { this.fabricTypeSupplied = fabricTypeSupplied; }

    public String getColourSupplied() { return colourSupplied; }
    public void setColourSupplied(String colourSupplied) { this.colourSupplied = colourSupplied; }

    public Double getQuantitySupplied() { return quantitySupplied; }
    public void setQuantitySupplied(Double quantitySupplied) { this.quantitySupplied = quantitySupplied; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
}
