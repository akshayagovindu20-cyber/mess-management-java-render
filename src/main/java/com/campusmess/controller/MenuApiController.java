package com.campusmess.controller;

import com.campusmess.entity.Menu;
import com.campusmess.repository.MenuRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuApiController {

    private final MenuRepository menuRepository;

    public MenuApiController(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @GetMapping
    public ResponseEntity<List<Menu>> getMenus(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (date != null && !date.trim().isEmpty()) {
            return ResponseEntity.ok(menuRepository.findByDateOrderByMealType(date.trim()));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(menuRepository.findByDateBetweenOrderByDateAsc(startDate.trim(), endDate.trim()));
        }
        return ResponseEntity.ok(menuRepository.findAllByOrderByDateAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenuById(@PathVariable Long id) {
        return menuRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMenu(@RequestBody Map<String, String> request) {
        String date = request.get("date");
        String mealType = request.get("mealType");
        String items = request.get("items");
        String description = request.getOrDefault("description", "");

        if (date == null || date.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Date is required."));
        }
        if (mealType == null || mealType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Meal type is required."));
        }
        if (items == null || items.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Food items are required."));
        }

        // Check for existing menu for same date and meal type
        Optional<Menu> existing = menuRepository.findByDateAndMealTypeIgnoreCase(date.trim(), mealType.trim());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "A menu for " + mealType + " on " + date + " already exists. Please edit it instead."));
        }

        Menu menu = new Menu(date.trim(), mealType.trim().toUpperCase(), items.trim(), description.trim());
        Menu saved = menuRepository.save(menu);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Menu created successfully.");
        resp.put("menu", saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Optional<Menu> menuOpt = menuRepository.findById(id);
        if (menuOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Menu menu = menuOpt.get();
        if (request.containsKey("date")) menu.setDate(request.get("date").trim());
        if (request.containsKey("mealType")) menu.setMealType(request.get("mealType").trim().toUpperCase());
        if (request.containsKey("items")) menu.setItems(request.get("items").trim());
        if (request.containsKey("description")) menu.setDescription(request.get("description").trim());
        menu.setUpdatedAt(LocalDateTime.now());

        Menu updated = menuRepository.save(menu);
        return ResponseEntity.ok(Map.of("success", true, "message", "Menu updated successfully.", "menu", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        if (!menuRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        menuRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Menu deleted successfully."));
    }
}
