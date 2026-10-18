package com.lankathreads.repository;

import com.lankathreads.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);
}
