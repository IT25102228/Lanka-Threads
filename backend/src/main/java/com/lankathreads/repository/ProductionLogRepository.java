package com.lankathreads.repository;

import com.lankathreads.model.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
}
