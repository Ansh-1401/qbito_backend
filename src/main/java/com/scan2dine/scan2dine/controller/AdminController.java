package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.MenuItem;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.repo.MenuItemRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "${app.frontend.url}")
public class AdminController {

    private final RestaurantRepo restaurantRepo;
    private final MenuItemRepo menuItemRepo;

    public AdminController(RestaurantRepo restaurantRepo, MenuItemRepo menuItemRepo) {
        this.restaurantRepo = restaurantRepo;
        this.menuItemRepo = menuItemRepo;
    }

    // ✅ Create Restaurant
    @PostMapping("/restaurants")
    public Restaurant createRestaurant(@RequestBody Restaurant restaurant) {
        return restaurantRepo.save(restaurant);
    }

    // ✅ Update Restaurant
    @PutMapping("/restaurants/{id}")
    public Restaurant updateRestaurant(@PathVariable Long id, @RequestBody Restaurant updated) {
        Restaurant r = restaurantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        r.setName(updated.getName());
        r.setSlug(updated.getSlug());
        r.setPlace(updated.getPlace());
        r.setTags(updated.getTags());
        r.setAvgPrice(updated.getAvgPrice());
        r.setCategory(updated.getCategory());
        r.setRating(updated.getRating());
        r.setEta(updated.getEta());
        r.setAddress(updated.getAddress());
        r.setOpenTime(updated.getOpenTime());
        r.setCover(updated.getCover());

        return restaurantRepo.save(r);
    }

    // ✅ Delete Restaurant
    @DeleteMapping("/restaurants/{id}")
    public String deleteRestaurant(@PathVariable Long id) {
        restaurantRepo.deleteById(id);
        return "Restaurant deleted ✅";
    }

    // ✅ Add Menu Item
    @PostMapping("/restaurants/{restaurantId}/menu")
    public MenuItem addMenuItem(@PathVariable Long restaurantId, @RequestBody MenuItem item) {
        Restaurant r = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        item.setRestaurant(r);
        return menuItemRepo.save(item);
    }

    // ✅ Update Menu Item
    @PutMapping("/menu/{menuId}")
    public MenuItem updateMenuItem(@PathVariable Long menuId, @RequestBody MenuItem updated) {
        MenuItem item = menuItemRepo.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        item.setName(updated.getName());
        item.setPrice(updated.getPrice());
        item.setCategory(updated.getCategory());
        item.setType(updated.getType());
        item.setDescription(updated.getDescription());
        item.setImage(updated.getImage());
        item.setAvailable(updated.getAvailable());

        return menuItemRepo.save(item);
    }

    // ✅ Delete Menu Item
    @DeleteMapping("/menu/{menuId}")
    public String deleteMenuItem(@PathVariable Long menuId) {
        menuItemRepo.deleteById(menuId);
        return "Menu item deleted ✅";
    }
}
