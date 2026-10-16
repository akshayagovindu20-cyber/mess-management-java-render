package com.campusmess.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 30)
    private String unit = "kg";

    @Column(name = "minimum_stock", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumStock = BigDecimal.TEN;

    @Column(length = 150)
    private String supplier;

    @Column(name = "last_restocked", length = 30)
    private String lastRestocked;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InventoryItem() {}

    public InventoryItem(String itemName, String category, BigDecimal quantity, String unit, BigDecimal minimumStock, String supplier) {
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.supplier = supplier;
        this.lastRestocked = java.time.LocalDate.now().toString();
        this.updatedAt = LocalDateTime.now();
    }

    public InventoryItem(String itemName, String category, BigDecimal quantity, String unit, BigDecimal minimumStock, String supplier, String lastRestocked) {
        this.itemName = itemName;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.supplier = supplier;
        this.lastRestocked = lastRestocked;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return quantity != null && minimumStock != null && quantity.compareTo(minimumStock) <= 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getMinimumStock() { return minimumStock; }
    public void setMinimumStock(BigDecimal minimumStock) { this.minimumStock = minimumStock; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getLastRestocked() { return lastRestocked != null ? lastRestocked : updatedAt.toLocalDate().toString(); }
    public void setLastRestocked(String lastRestocked) { this.lastRestocked = lastRestocked; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
