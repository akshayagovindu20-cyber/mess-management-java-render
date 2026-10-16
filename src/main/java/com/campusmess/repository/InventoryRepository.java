package com.campusmess.repository;

import com.campusmess.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByCategoryIgnoreCase(String category);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minimumStock")
    List<InventoryItem> findLowStockItems();

    List<InventoryItem> findAllByOrderByItemNameAsc();
}
