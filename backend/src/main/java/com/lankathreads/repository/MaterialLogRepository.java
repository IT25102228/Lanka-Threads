package com.lankathreads.repository;

import com.lankathreads.model.MaterialLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialLogRepository extends JpaRepository<MaterialLog, Long> {
    List<MaterialLog> findAllByOrderByTimestampDesc();
    List<MaterialLog> findByReceiptIdOrderByTimestampDesc(String receiptId);
}
