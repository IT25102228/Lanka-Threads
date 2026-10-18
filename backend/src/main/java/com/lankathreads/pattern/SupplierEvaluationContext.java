package com.lankathreads.pattern;

import com.lankathreads.model.ReceivedMaterial;
import com.lankathreads.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class managing registered SupplierEvaluationStrategy implementations and delegating evaluation calls.
 */
@Component
public class SupplierEvaluationContext {

    private final Map<String, SupplierEvaluationStrategy> strategies = new HashMap<>();
    private final SupplierEvaluationStrategy defaultStrategy;

    @Autowired
    public SupplierEvaluationContext(List<SupplierEvaluationStrategy> strategyList,
                                     SupplierEvaluationStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        if (strategyList != null) {
            for (SupplierEvaluationStrategy s : strategyList) {
                strategies.put(s.getStrategyKey().toUpperCase(), s);
            }
        }
    }

    /**
     * Resolves the strategy by key or falls back to the default balanced scorecard.
     */
    public SupplierEvaluationStrategy resolveStrategy(String strategyKey) {
        if (strategyKey == null || strategyKey.isBlank()) {
            return defaultStrategy;
        }
        return strategies.getOrDefault(strategyKey.trim().toUpperCase(), defaultStrategy);
    }

    /**
     * Executes evaluation using the chosen strategy.
     */
    public SupplierEvaluationResult evaluate(Supplier supplier,
                                            List<ReceivedMaterial> history,
                                            String strategyKey) {
        SupplierEvaluationStrategy strategy = resolveStrategy(strategyKey);
        return strategy.evaluate(supplier, history);
    }

    /**
     * Returns all registered strategy options for UI dropdowns.
     */
    public Map<String, String> getAvailableStrategies() {
        Map<String, String> map = new HashMap<>();
        strategies.forEach((k, v) -> map.put(k, v.getDisplayName()));
        return map;
    }
}
