package com.inventra.repository;

import com.inventra.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
     List<Sale> findBySoldByIdOrderBySaleDateDesc(Long soldById);

     @Query("""
                 SELECT s
                 FROM Sale s
                 JOIN Staff st ON st.user.id = s.soldBy.id
                 WHERE st.manager.id = :managerId
                 ORDER BY s.saleDate DESC
             """)
     List<Sale> findSalesByManagerId(Long managerId);

     List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

     List<Sale> findByCustomerId(Long customerId);

     @Query("""
                 SELECT DISTINCT s FROM Sale s
                 LEFT JOIN FETCH s.items i
                 LEFT JOIN FETCH i.product p
                 WHERE s.saleDate BETWEEN :start AND :end
             """)
     List<Sale> findWithItemsBetween(
             @Param("start") LocalDateTime start,
             @Param("end") LocalDateTime end
     );

     @Query("""
                 SELECT DISTINCT s FROM Sale s
                 LEFT JOIN FETCH s.items i
                 LEFT JOIN FETCH i.product p
             """)
     List<Sale> findAllWithItems();
}