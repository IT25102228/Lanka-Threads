package com.lankathreads.pattern;

import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════════════════════════
 * DESIGN PATTERN: STRATEGY PATTERN (Behavioral Design Pattern)
 * Context: Lanka Threads Supplier Performance Evaluation
 * ════════════════════════════════════════════════════════════════════════════════
 */
public interface SupplierEvaluationStrategy {

    /**
     * Unique key for strategy identification (e.g., "QUALITY", "PUNCTUALITY", "BALANCED").
     */
    String getStrategyKey();

    /**
     * Human-readable display title for UI/Dashboard views.
     */
    String getDisplayName();

    /**
     * Evaluates a supplier based on their received material batch history.
     *
     * @param supplier The master supplier profile
     * @param batchHistory All fabric material batches delivered by this supplier
     * @return SupplierEvaluationResult containing rating score, tier, and breakdown
     */
    SupplierEvaluationResult evaluate(Supplier supplier, List<ReceivedMaterial> batchHistory);
}
