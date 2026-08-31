package com.inventra.repository;

import com.inventra.entity.Purchase;
import com.inventra.entity.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findBySupplierId(Long supplierId);
    List<Purchase> findByStatus(PurchaseStatus status);
}
