package com.lankathreads.pattern;

import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy focusing on textile quality compliance and defect rejection rates.
 */
@Component
public class QualityComplianceStrategy implements SupplierEvaluationStrategy {

    @Override
    public String getStrategyKey() {
        return "QUALITY";
    }

    @Override
    public String getDisplayName() {
        return "Quality Acceptance & Defect Rate Strategy";
    }

    @Override
    public SupplierEvaluationResult evaluate(Supplier supplier, List<ReceivedMaterial> batchHistory) {
        if (batchHistory == null || batchHistory.isEmpty()) {
            return new SupplierEvaluationResult(
                    supplier.getSupplierCode(), supplier.getName(), getDisplayName(),
                    70.0, "TIER_2_STANDARD", "blue",
                    "No historical batches on record; assigned default baseline rating.",
                    0, List.of("Awaiting initial fabric intake shipments.")
            );
        }

        long approvedCount = 0;
        long rejectedCount = 0;
        long inspectedCount = 0;

        for (ReceivedMaterial m : batchHistory) {
            if ("APPROVED".equalsIgnoreCase(m.getStatus())) {
                approvedCount++;
                inspectedCount++;
            } else if ("REJECTED".equalsIgnoreCase(m.getStatus())) {
                rejectedCount++;
                inspectedCount++;
            }
        }

        double score = (inspectedCount == 0) ? 75.0 : ((double) approvedCount / inspectedCount) * 100.0;

        String tier;
        String badge;
        String feedback;
        if (score >= 90.0) {
            tier = "TIER_1_PREFERRED";
            badge = "emerald";
            feedback = "High textile quality standard with minimal defects.";
        } else if (score >= 75.0) {
            tier = "TIER_2_STANDARD";
            badge = "blue";
            feedback = "Acceptable fabric consistency conforming to standards.";
        } else if (score >= 50.0) {
            tier = "TIER_3_AT_RISK";
            badge = "amber";
            feedback = "Elevated rejection rate observed. Quality audit required.";
        } else {
            tier = "TIER_4_CRITICAL";
            badge = "rose";
            feedback = "Critical non-compliance. Immediate vendor review required.";
        }

        List<String> observations = new ArrayList<>();
        observations.add(String.format("Approved Batches: %d / %d inspected", approvedCount, inspectedCount));
        observations.add(String.format("Rejected Defect Batches: %d", rejectedCount));
        observations.add(String.format("Pending Inspection: %d", (batchHistory.size() - inspectedCount)));

        return new SupplierEvaluationResult(
                supplier.getSupplierCode(), supplier.getName(), getDisplayName(),
                score, tier, badge, feedback, batchHistory.size(), observations
        );
    }
}
