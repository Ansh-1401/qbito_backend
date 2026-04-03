package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.MenuItem;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.repo.MenuItemRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "${app.frontend.url}")
public class PublicRestaurantController {

    private final RestaurantRepo restaurantRepo;
    private final MenuItemRepo menuItemRepo;

    public PublicRestaurantController(RestaurantRepo restaurantRepo, MenuItemRepo menuItemRepo) {
        this.restaurantRepo = restaurantRepo;
        this.menuItemRepo = menuItemRepo;
    }

    // ✅ list restaurants (for homepage)
    @GetMapping
    public List<Restaurant> allRestaurants() {
        return restaurantRepo.findAll();
    }

    // ✅ restaurant details by slug
    @GetMapping("/slug/{slug}")
    public Restaurant restaurantBySlug(@PathVariable String slug) {
        return restaurantRepo.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    // ✅ menu by restaurant slug
    @GetMapping("/slug/{slug}/menu")
    public List<MenuItem> menuBySlug(@PathVariable String slug) {
        Restaurant r = restaurantRepo.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return menuItemRepo.findByRestaurantId(r.getId());
    }
}
