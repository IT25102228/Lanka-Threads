package com.lankathreads.model;

import com.lankathreads.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "production_logs")
public class ProductionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String category; // Men, Women, Children

    @Column(nullable = false)
    private Integer quantityProduced;

    @Column(nullable = false)
    private Integer targetQuantity = 100; // Target quantity for efficiency calculation

    @Column(nullable = false)
    private String wipStage = "Cutting"; // Cutting, Sewing, Ironing, Packaging

    @Column(nullable = false)
    private Integer downtimeMinutes = 0; // Machine downtime monitoring

    @Column(nullable = false)
    private String downtimeReason = "None"; // Reason for downtime

    @Column(nullable = false)
    private String productionLine = "Line E"; // Line A, Line B, Line C

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantityProduced() {
        return quantityProduced;
    }

    public void setQuantityProduced(Integer quantityProduced) {
        this.quantityProduced = quantityProduced;
    }

    public Integer getTargetQuantity() {
        return targetQuantity;
    }

    public void setTargetQuantity(Integer targetQuantity) {
        this.targetQuantity = targetQuantity;
    }

    public String getWipStage() {
        return wipStage;
    }

    public void setWipStage(String wipStage) {
        this.wipStage = wipStage;
    }

    public Integer getDowntimeMinutes() {
        return downtimeMinutes;
    }

    public void setDowntimeMinutes(Integer downtimeMinutes) {
        this.downtimeMinutes = downtimeMinutes;
    }

    public String getDowntimeReason() {
        return downtimeReason;
    }

    public void setDowntimeReason(String downtimeReason) {
        this.downtimeReason = downtimeReason;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }
}
