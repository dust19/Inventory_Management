package com.inventra.repository;

import com.inventra.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByUserId(Long userId);

    List<Supplier> findByActiveTrue();

    List<Supplier> findByCategoryId(Long categoryId);
}
