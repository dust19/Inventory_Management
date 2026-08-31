package com.inventra.repository;

import com.inventra.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
     List<SaleItem> findBySaleId(Long saleId);

     @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
     long sumQuantityByProductId(@Param("productId") Long productId);

     @Query("SELECT COALESCE(SUM(si.subtotal), 0.0) FROM SaleItem si WHERE si.product.id = :productId")
     double sumRevenueByProductId(@Param("productId") Long productId);
}
