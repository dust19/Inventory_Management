package com.inventra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Data
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name="unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;   // MUST equal product.supplierToAdminPrice at order time

    @Column(name="sub_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal subTotal;
}