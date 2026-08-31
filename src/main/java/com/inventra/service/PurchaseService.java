package com.inventra.service;

import com.inventra.dto.request.PurchaseItemRequestDTO;
import com.inventra.dto.request.PurchaseRequestDTO;
import com.inventra.dto.response.PurchaseItemResponseDTO;
import com.inventra.dto.response.PurchaseResponseDTO;
import com.inventra.entity.*;
import com.inventra.entity.enums.PurchaseStatus;
import com.inventra.entity.enums.TransactionType;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepo;
    private final ProductRepository productRepo;
    private final SupplierRepository supplierRepo;
    private final UserRepository userRepo;
    private final InventoryTransactionService inventoryTxService;

    // ================= CREATE (ADMIN) =================
    @Transactional
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO dto) {

        if (dto.getSupplierId() == null)
            throw new IllegalArgumentException("SupplierId is required");
        if (dto.getItems() == null || dto.getItems().isEmpty())
            throw new IllegalArgumentException("Items are required");

        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setStatus(PurchaseStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseItemRequestDTO itemDto : dto.getItems()) {

            if (itemDto.getProductId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0)
                throw new IllegalArgumentException("Invalid purchase item");

            Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (!product.getSupplier().getId().equals(supplier.getId()))
                throw new IllegalArgumentException("Product does not belong to this supplier");

            BigDecimal unitPrice = product.getSupplierToAdminPrice();

            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchase);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(unitPrice);

            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            item.setSubTotal(subTotal);

            purchase.getItems().add(item);
            total = total.add(subTotal);
        }

        purchase.setTotalAmount(total);
        return toDTO(purchaseRepo.save(purchase));
    }

    // ================= DELIVER (SUPPLIER only) =================
    // Supplier marks the order as delivered — no inventory change yet
    @Transactional
    public PurchaseResponseDTO markAsDelivered(Long purchaseId) {

        Purchase purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        if (purchase.getStatus() == PurchaseStatus.DELIVERED)
            return toDTO(purchase);

        if (purchase.getStatus() == PurchaseStatus.CANCELED)
            throw new IllegalStateException("Canceled purchase cannot be delivered");

        if (purchase.getStatus() == PurchaseStatus.CONFIRMED)
            throw new IllegalStateException("Purchase already confirmed");

        // CHECK supplier stock BEFORE delivery
        for (PurchaseItem item : purchase.getItems()) {

            Product product = item.getProduct();

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient supplier stock for product: " + product.getSku()
                );
            }
        }

        purchase.setStatus(PurchaseStatus.DELIVERED);

        return toDTO(purchaseRepo.save(purchase));
    }
    // ================= CONFIRM (ADMIN or MANAGER) =================
    // Confirms delivery → decreases supplier stock → updates inventory → STOCK_IN
    @Transactional
    public PurchaseResponseDTO confirmDelivery(Long purchaseId, Long confirmedByUserId) {

        Purchase purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        if (purchase.getStatus() != PurchaseStatus.DELIVERED)
            throw new IllegalStateException("Only DELIVERED purchases can be confirmed");

        User confirmedBy = userRepo.findById(confirmedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + confirmedByUserId));

        for (PurchaseItem item : purchase.getItems()) {

            Product product = item.getProduct();
            int qty = item.getQuantity();

            // decrease supplier stock
            product.setQuantity(product.getQuantity() - qty);
            productRepo.save(product);

            // increase inventory + log STOCK_IN
            inventoryTxService.process(product.getId(), TransactionType.STOCK_IN, qty);
        }

        purchase.setStatus(PurchaseStatus.CONFIRMED);
        purchase.setConfirmedBy(confirmedBy);
        purchase.setConfirmedAt(LocalDateTime.now());

        return toDTO(purchaseRepo.save(purchase));
    }

    // ================= CANCEL (ADMIN or SUPPLIER) =================
    @Transactional
    public PurchaseResponseDTO cancelPurchase(Long purchaseId, Long userId) {

        Purchase purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (purchase.getStatus() == PurchaseStatus.CONFIRMED)
            throw new IllegalStateException("Confirmed purchase cannot be canceled");

        if (purchase.getStatus() == PurchaseStatus.CANCELED)
            throw new IllegalStateException("Purchase is already canceled");

        purchase.setStatus(PurchaseStatus.CANCELED);
        purchase.setConfirmedBy(user);
        purchase.setConfirmedAt(LocalDateTime.now());

        return toDTO(purchaseRepo.save(purchase));
    }
    // ================= GET ALL =================
    public List<PurchaseResponseDTO> getAllPurchases() {
        return purchaseRepo.findAll().stream().map(this::toDTO).toList();
    }

    // ================= GET BY SUPPLIER =================
    public List<PurchaseResponseDTO> getBySupplier(Long supplierId) {
        return purchaseRepo.findBySupplierId(supplierId).stream().map(this::toDTO).toList();
    }

    // ================= GET BY STATUS =================
    public List<PurchaseResponseDTO> getByStatus(PurchaseStatus status) {
        return purchaseRepo.findByStatus(status).stream().map(this::toDTO).toList();
    }

    // ================= GET BY ID =================
    public PurchaseResponseDTO getById(Long id) {
        return toDTO(purchaseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found")));
    }

    // ================= MAPPER =================
    private PurchaseResponseDTO toDTO(Purchase purchase) {

        PurchaseResponseDTO dto = new PurchaseResponseDTO();

        dto.setId(purchase.getId());
        dto.setSupplierId(purchase.getSupplier().getId());
        dto.setSupplierName(purchase.getSupplier().getUser().getName()); // add this
        dto.setStatus(purchase.getStatus().name());
        dto.setTotalAmount(purchase.getTotalAmount());

        // show confirmation details only for CONFIRMED or CANCELED
        if (purchase.getStatus() == PurchaseStatus.CONFIRMED
                || purchase.getStatus() == PurchaseStatus.CANCELED) {

            dto.setConfirmedAt(purchase.getConfirmedAt());

            if (purchase.getConfirmedBy() != null) {
                dto.setConfirmedById(purchase.getConfirmedBy().getId());
                dto.setConfirmedByName(purchase.getConfirmedBy().getName());
            }
        } else {
            dto.setConfirmedAt(null);
            dto.setConfirmedById(null);
            dto.setConfirmedByName(null);
        }

        dto.setItems(
                purchase.getItems().stream().map(item -> {
                    PurchaseItemResponseDTO i = new PurchaseItemResponseDTO();
                    i.setProductId(item.getProduct().getId());
                    i.setProductName(item.getProduct().getName());
                    i.setQuantity(item.getQuantity());
                    i.setUnitPrice(item.getUnitPrice());
                    i.setSubTotal(item.getSubTotal());
                    return i;
                }).toList()
        );

        return dto;
    }
}