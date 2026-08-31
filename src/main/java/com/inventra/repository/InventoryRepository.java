package com.inventra.repository;

import com.inventra.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

     Optional<Inventory> findByProductId(Long productId);

     @Query("""
    SELECT c.id,
           c.name,
           COUNT(i.id)
    FROM Category c
    LEFT JOIN c.products p
    LEFT JOIN Inventory i ON i.product.id = p.id
    GROUP BY c.id, c.name
    ORDER BY c.name
""")
     List<Object[]> getProductCountByCategory();

     boolean existsByProduct_Id(Long productId);
}
