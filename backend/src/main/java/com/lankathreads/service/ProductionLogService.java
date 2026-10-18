package com.lankathreads.service;

import com.lankathreads.model.ProductionLog;
import com.lankathreads.repository.ProductionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling daily factory production logging and efficiency records.
 */
@SuppressWarnings("null")
@Service
public class ProductionLogService {

    @Autowired
    private ProductionLogRepository productionLogRepository;

    /**
     * Retrieves all recorded production logs.
     */
    public List<ProductionLog> getAllLogs() {
        return productionLogRepository.findAll();
    }

    /**
     * Finds a production log by ID.
     */
    @Nullable
    public ProductionLog getLogById(Long id) {
        return productionLogRepository.findById(id).orElse(null);
    }

    /**
     * Saves or updates a production log entry.
     */
    public ProductionLog saveLog(ProductionLog log) {
        return productionLogRepository.save(log);
    }
}
