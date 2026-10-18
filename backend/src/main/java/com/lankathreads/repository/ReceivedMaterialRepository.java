package com.lankathreads.repository;

import com.lankathreads.model.ReceivedMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivedMaterialRepository extends JpaRepository<ReceivedMaterial, Long> {
    Optional<ReceivedMaterial> findByReceiptId(String receiptId);
    List<ReceivedMaterial> findByStatus(String status);
    List<ReceivedMaterial> findBySupplierCode(String supplierCode);
}
