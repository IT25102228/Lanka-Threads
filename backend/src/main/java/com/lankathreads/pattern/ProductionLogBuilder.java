package com.lankathreads.pattern;

import com.lankathreads.model.User;
import com.lankathreads.model.ProductionLog;

import java.time.LocalDate;

/**
 * ============================================================
 *  DESIGN PATTERN : BUILDER (Creational Pattern)
 * ============================================================
 *
 * PROBLEM
 *   ProductionLog has many fields (10). Some are mandatory (category,
 *   quantityProduced, supervisor, date) and some are optional with default
 *   values (target, WIP stage, downtime, production line).
 *   Creating it with many setter calls in the controller is messy, easy to
 *   get wrong, and nothing checks that the object is valid before saving.
 *
 * SOLUTION
 *   Move the construction logic into a separate Builder class:
 *     1. Each field is set through a fluent method (method chaining).
 *     2. Optional fields keep their default value if not supplied.
 *     3. build() validates the data and only then returns the finished
 *        ProductionLog. An invalid log can never be created.
 *
 * PARTICIPANTS (for viva)
 *   Product  -> ProductionLog          (the complex object being created)
 *   Builder  -> ProductionLogBuilder   (this class)
 *   Client   -> SupervisorController   (uses the builder in addLog())
 *
 * USAGE
 *   ProductionLog log = new ProductionLogBuilder()
 *           .date(LocalDate.now())
 *           .category("Men")
 *           .quantityProduced(80)
 *           .targetQuantity(100)
 *           .wipStage("Sewing")
 *           .downtime(15, "Machine breakdown")
 *           .productionLine("Line B")
 *           .supervisor(user)
 *           .build();
 */
public class ProductionLogBuilder {

    // ---- Mandatory fields (no default, must be provided) ----
    private LocalDate date;
    private String category;
    private Integer quantityProduced;
    private User supervisor;

    // ---- Optional fields (defaults are the same as in ProductionLog) ----
    private Integer targetQuantity = 100;
    private String wipStage = "Cutting";
    private Integer downtimeMinutes = 0;
    private String downtimeReason = "None";
    private String productionLine = "Line E";

    // ------------------------------------------------------------
    // Builder steps. Each returns "this" so calls can be chained.
    // Optional setters ignore null/blank so the default is kept.
    // ------------------------------------------------------------

    public ProductionLogBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public ProductionLogBuilder category(String category) {
        this.category = category;
        return this;
    }

    public ProductionLogBuilder quantityProduced(Integer quantityProduced) {
        this.quantityProduced = quantityProduced;
        return this;
    }

    public ProductionLogBuilder supervisor(User supervisor) {
        this.supervisor = supervisor;
        return this;
    }

    public ProductionLogBuilder targetQuantity(Integer targetQuantity) {
        if (targetQuantity != null) {
            this.targetQuantity = targetQuantity;
        }
        return this;
    }

    public ProductionLogBuilder wipStage(String wipStage) {
        if (wipStage != null && !wipStage.trim().isEmpty()) {
            this.wipStage = wipStage.trim();
        }
        return this;
    }

    public ProductionLogBuilder productionLine(String productionLine) {
        if (productionLine != null && !productionLine.trim().isEmpty()) {
            this.productionLine = productionLine.trim();
        }
        return this;
    }

    /** Downtime minutes and the reason are related, so they are set together. */
    public ProductionLogBuilder downtime(Integer minutes, String reason) {
        if (minutes != null) {
            this.downtimeMinutes = minutes;
        }
        if (reason != null && !reason.trim().isEmpty()) {
            this.downtimeReason = reason.trim();
        }
        return this;
    }

    // ------------------------------------------------------------
    // Final step: validate, then create the product.
    // ------------------------------------------------------------

    /**
     * Validates all values and returns the finished ProductionLog.
     *
     * @throws IllegalArgumentException if any business rule is broken
     */
    public ProductionLog build() {
        validate();

        // If there was no downtime, the reason is always "None"
        if (downtimeMinutes == 0) {
            downtimeReason = "None";
        }

        ProductionLog log = new ProductionLog();
        log.setDate(date);
        log.setCategory(category.trim());
        log.setQuantityProduced(quantityProduced);
        log.setTargetQuantity(targetQuantity);
        log.setWipStage(wipStage);
        log.setDowntimeMinutes(downtimeMinutes);
        log.setDowntimeReason(downtimeReason);
        log.setProductionLine(productionLine);
        log.setSupervisor(supervisor);
        return log;
    }

    /** Business rules that every production log must satisfy. */
    private void validate() {
        if (date == null) {
            throw new IllegalArgumentException("Production date is required.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (quantityProduced == null || quantityProduced < 0) {
            throw new IllegalArgumentException("Quantity produced must be 0 or more.");
        }
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException("Target quantity must be greater than 0.");
        }
        if (downtimeMinutes < 0) {
            throw new IllegalArgumentException("Downtime minutes cannot be negative.");
        }
        if (supervisor == null) {
            throw new IllegalArgumentException("A supervisor is required.");
        }
    }
}
