package com.lankathreads.pattern;

import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy focusing on lead time adherence and on-time shipment arrival.
 */
@Component
public class DeliveryPunctualityStrategy implements SupplierEvaluationStrategy {

    @Override
    public String getStrategyKey() {
        return "PUNCTUALITY";
    }

    @Override
    public String getDisplayName() {
        return "On-Time Delivery Lead Punctuality Strategy";
    }

    @Override
    public SupplierEvaluationResult evaluate(Supplier supplier, List<ReceivedMaterial> batchHistory) {
        if (batchHistory == null || batchHistory.isEmpty()) {
            return new SupplierEvaluationResult(
                    supplier.getSupplierCode(), supplier.getName(), getDisplayName(),
                    70.0, "TIER_2_STANDARD", "blue",
                    "No delivery history; evaluated at standard baseline.",
                    0, List.of("No past shipments recorded.")
            );
        }

        long onTimeCount = 0;
        long lateCount = 0;
        long totalWithSchedule = 0;

        for (ReceivedMaterial m : batchHistory) {
            if (m.getReceivedDate() != null && m.getExpectedDeliveryDate() != null) {
                totalWithSchedule++;
                if (!m.getReceivedDate().isAfter(m.getExpectedDeliveryDate())) {
                    onTimeCount++;
                } else {
                    lateCount++;
                }
            }
        }

        double score = (totalWithSchedule == 0) ? 80.0 : ((double) onTimeCount / totalWithSchedule) * 100.0;

        String tier = score >= 90.0 ? "TIER_1_PREFERRED" : (score >= 70.0 ? "TIER_2_STANDARD" : "TIER_3_AT_RISK");
        String badge = score >= 90.0 ? "emerald" : (score >= 70.0 ? "blue" : "amber");
        String feedback = score >= 85.0 ? "Consistent on-time deliveries meeting factory deadlines."
                : "Intermittent delivery delays impacting production scheduling.";

        List<String> observations = new ArrayList<>();
        observations.add(String.format("On-Time Deliveries: %d", onTimeCount));
        observations.add(String.format("Delayed Deliveries: %d", lateCount));

        return new SupplierEvaluationResult(
                supplier.getSupplierCode(), supplier.getName(), getDisplayName(),
                score, tier, badge, feedback, batchHistory.size(), observations
        );
    }
}
