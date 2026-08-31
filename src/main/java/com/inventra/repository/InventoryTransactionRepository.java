package com.inventra.repository;

import com.inventra.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProduct_IdOrderByCreatedAtDesc(Long productId);
}
