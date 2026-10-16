package com.campusmess.controller;

import com.campusmess.entity.InventoryItem;
import com.campusmess.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryApiController {

    private final InventoryRepository inventoryRepository;

    public InventoryApiController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllInventory(@RequestParam(required = false) String category) {
        List<InventoryItem> items;
        if (category != null && !category.trim().isEmpty()) {
            items = inventoryRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            items = inventoryRepository.findAllByOrderByItemNameAsc();
        }

        List<InventoryItem> lowStock = inventoryRepository.findLowStockItems();

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalItems", items.size());
        response.put("lowStockCount", lowStock.size());
        response.put("lowStockItems", lowStock);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItem>> getLowStockItems() {
        return ResponseEntity.ok(inventoryRepository.findLowStockItems());
    }

    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> request) {
        String itemName = request.get("itemName") != null ? request.get("itemName").toString().trim() : null;
        String category = request.get("category") != null ? request.get("category").toString().trim() : "General";
        String unit = request.get("unit") != null ? request.get("unit").toString().trim() : "kg";

        if (itemName == null || itemName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Item name is required."));
        }

        BigDecimal quantity = new BigDecimal(request.getOrDefault("quantity", "0").toString());
        BigDecimal minimumStock = new BigDecimal(request.getOrDefault("minimumStock", "10").toString());
        String supplier = request.getOrDefault("supplier", "Campus Kitchen Vendor").toString();
        String lastRestocked = request.getOrDefault("lastRestocked", LocalDate.now().toString()).toString();

        InventoryItem item = new InventoryItem(itemName, category, quantity, unit, minimumStock, supplier, lastRestocked);
        InventoryItem saved = inventoryRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Inventory item added successfully.",
                "item", saved
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InventoryItem item = itemOpt.get();
        if (request.containsKey("itemName")) item.setItemName(request.get("itemName").toString().trim());
        if (request.containsKey("category")) item.setCategory(request.get("category").toString().trim());
        if (request.containsKey("quantity")) item.setQuantity(new BigDecimal(request.get("quantity").toString()));
        if (request.containsKey("unit")) item.setUnit(request.get("unit").toString().trim());
        if (request.containsKey("minimumStock")) item.setMinimumStock(new BigDecimal(request.get("minimumStock").toString()));
        if (request.containsKey("lastRestocked")) item.setLastRestocked(request.get("lastRestocked").toString());
        item.setUpdatedAt(LocalDateTime.now());

        InventoryItem saved = inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of("success", true, "message", "Inventory item updated successfully.", "item", saved));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<?> adjustQuantity(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!request.containsKey("delta")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Delta amount is required."));
        }

        BigDecimal delta = new BigDecimal(request.get("delta").toString());
        InventoryItem item = itemOpt.get();
        BigDecimal newQty = item.getQuantity().add(delta);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            newQty = BigDecimal.ZERO;
        }

        item.setQuantity(newQty);
        item.setUpdatedAt(LocalDateTime.now());
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            item.setLastRestocked(LocalDate.now().toString());
        }

        InventoryItem saved = inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock adjusted successfully. New stock: " + newQty + " " + item.getUnit(),
                "item", saved
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        if (!inventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        inventoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Inventory item deleted successfully."));
    }
}
