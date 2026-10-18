package com.lankathreads.pattern;

import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Strategy combining Quality (60%) and Delivery Punctuality (40%).
 */
@Component
@Primary
public class CompositeScorecardStrategy implements SupplierEvaluationStrategy {

    private final QualityComplianceStrategy qualityStrategy = new QualityComplianceStrategy();
    private final DeliveryPunctualityStrategy punctualityStrategy = new DeliveryPunctualityStrategy();

    @Override
    public String getStrategyKey() {
        return "BALANCED";
    }

    @Override
    public String getDisplayName() {
        return "Balanced Textile Supplier Scorecard (Quality 60% + Punctuality 40%)";
    }

    @Override
    public SupplierEvaluationResult evaluate(Supplier supplier, List<ReceivedMaterial> batchHistory) {
        SupplierEvaluationResult qualityResult = qualityStrategy.evaluate(supplier, batchHistory);
        SupplierEvaluationResult punctualityResult = punctualityStrategy.evaluate(supplier, batchHistory);

        double compositeScore = (qualityResult.getScore() * 0.60) + (punctualityResult.getScore() * 0.40);

        String tier;
        String badge;
        if (compositeScore >= 88.0) {
            tier = "TIER_1_PREFERRED";
            badge = "emerald";
        } else if (compositeScore >= 72.0) {
            tier = "TIER_2_STANDARD";
            badge = "blue";
        } else if (compositeScore >= 55.0) {
            tier = "TIER_3_AT_RISK";
            badge = "amber";
        } else {
            tier = "TIER_4_CRITICAL";
            badge = "rose";
        }

        List<String> observations = new ArrayList<>();
        observations.add(String.format("Quality Index: %.1f%% (Weight: 60%%)", qualityResult.getScore()));
        observations.add(String.format("Punctuality Index: %.1f%% (Weight: 40%%)", punctualityResult.getScore()));
        observations.addAll(qualityResult.getKeyObservations());

        return new SupplierEvaluationResult(
                supplier.getSupplierCode(),
                supplier.getName(),
                getDisplayName(),
                compositeScore,
                tier,
                badge,
                "Overall rating synthesizes textile quality approval rates and delivery timeline accuracy.",
                batchHistory != null ? batchHistory.size() : 0,
                observations
        );
    }
}
