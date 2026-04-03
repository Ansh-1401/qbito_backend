package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.MenuItem;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.repo.MenuItemRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/menu")
@CrossOrigin(origins = "${app.frontend.url}")
public class MenuItemController {

    private final MenuItemRepo menuItemRepo;
    private final RestaurantRepo restaurantRepo;

    public MenuItemController(MenuItemRepo menuItemRepo, RestaurantRepo restaurantRepo) {
        this.menuItemRepo = menuItemRepo;
        this.restaurantRepo = restaurantRepo;
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<MenuItem>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuItemRepo.findByRestaurantId(restaurantId));
    }

    @PostMapping("/{restaurantId}")
    public ResponseEntity<MenuItem> addMenuItem(@PathVariable Long restaurantId, @RequestBody Map<String, Object> body) {
        Optional<Restaurant> optRestaurant = restaurantRepo.findById(restaurantId);
        if (optRestaurant.isEmpty()) return ResponseEntity.notFound().build();

        MenuItem item = MenuItem.builder()
                .restaurant(optRestaurant.get())
                .name((String) body.get("name"))
                .price(body.get("price") != null ? ((Number) body.get("price")).intValue() : 0)
                .category((String) body.getOrDefault("category", "Main Course"))
                .type((String) body.getOrDefault("type", "veg"))
                .description((String) body.getOrDefault("description", ""))
                .image((String) body.getOrDefault("image", ""))
                .available(true)
                .build();

        return ResponseEntity.ok(menuItemRepo.save(item));
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long itemId, @RequestBody Map<String, Object> body) {
        Optional<MenuItem> optItem = menuItemRepo.findById(itemId);
        if (optItem.isEmpty()) return ResponseEntity.notFound().build();

        MenuItem item = optItem.get();
        if (body.containsKey("name")) item.setName((String) body.get("name"));
        if (body.containsKey("price")) item.setPrice(((Number) body.get("price")).intValue());
        if (body.containsKey("category")) item.setCategory((String) body.get("category"));
        if (body.containsKey("type")) item.setType((String) body.get("type"));
        if (body.containsKey("description")) item.setDescription((String) body.get("description"));
        if (body.containsKey("image")) item.setImage((String) body.get("image"));

        return ResponseEntity.ok(menuItemRepo.save(item));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        if (!menuItemRepo.existsById(itemId)) return ResponseEntity.notFound().build();
        menuItemRepo.deleteById(itemId);
        return ResponseEntity.ok().build();
    }
}
