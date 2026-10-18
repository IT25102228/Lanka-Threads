package com.lankathreads.repository;

import com.lankathreads.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByFabricTypeIgnoreCaseAndColourIgnoreCase(String fabricType, String colour);
}
