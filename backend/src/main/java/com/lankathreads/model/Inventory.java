package com.lankathreads.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fabricType;

    @Column(nullable = false)
    private String colour;

    @Column(nullable = false)
    private Double quantityAvailable = 0.0; // In meters or kg

    @Column(nullable = false)
    private Double reorderLevel = 100.0;

    @Column(nullable = false)
    private String status = "Approved"; // Pending, Approved, Rejected

    @Column(nullable = false)
    private String rollNumber = "R-1001"; // Fabric roll tracking number

    @Column(nullable = false)
    private String lotNumber = "LOT-2002"; // Lot tracking number

    @Column(nullable = false)
    private String requisitionLine = "None"; // Requisition & movement line target

    @Column(nullable = false)
    private String category = "Men"; // Men, Women, Children

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFabricType() { return fabricType; }
    public void setFabricType(String fabricType) { this.fabricType = fabricType; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    public Double getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(Double quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    public Double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Double reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getRequisitionLine() { return requisitionLine; }
    public void setRequisitionLine(String requisitionLine) { this.requisitionLine = requisitionLine; }

    public String getCategory() { return category != null ? category : "Men"; }
    public void setCategory(String category) { this.category = category; }
}