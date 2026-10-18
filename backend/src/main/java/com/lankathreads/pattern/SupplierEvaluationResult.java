package com.lankathreads.pattern;

import java.util.List;

/**
 * Result DTO returned by a SupplierEvaluationStrategy.
 */
public class SupplierEvaluationResult {

    private final String supplierCode;
    private final String supplierName;
    private final String strategyName;
    private final double score;              // 0.0 to 100.0
    private final String ratingTier;         // TIER_1_PREFERRED, TIER_2_STANDARD, TIER_3_AT_RISK, TIER_4_CRITICAL
    private final String statusBadgeColor;   // emerald, blue, amber, rose
    private final String summaryFeedback;
    private final int totalBatchesEvaluated;
    private final List<String> keyObservations;

    public SupplierEvaluationResult(String supplierCode, String supplierName, String strategyName,
                                    double score, String ratingTier, String statusBadgeColor,
                                    String summaryFeedback, int totalBatchesEvaluated,
                                    List<String> keyObservations) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.strategyName = strategyName;
        this.score = Math.round(score * 10.0) / 10.0;
        this.ratingTier = ratingTier;
        this.statusBadgeColor = statusBadgeColor;
        this.summaryFeedback = summaryFeedback;
        this.totalBatchesEvaluated = totalBatchesEvaluated;
        this.keyObservations = keyObservations;
    }

    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public String getStrategyName() { return strategyName; }
    public double getScore() { return score; }
    public String getRatingTier() { return ratingTier; }
    public String getStatusBadgeColor() { return statusBadgeColor; }
    public String getSummaryFeedback() { return summaryFeedback; }
    public int getTotalBatchesEvaluated() { return totalBatchesEvaluated; }
    public List<String> getKeyObservations() { return keyObservations; }
}
