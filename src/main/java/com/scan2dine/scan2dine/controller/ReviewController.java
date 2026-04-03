package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.AppUser;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.entity.RestaurantReview;
import com.scan2dine.scan2dine.repo.AppUserRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import com.scan2dine.scan2dine.repo.ReviewRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "${app.frontend.url}")
public class ReviewController {

    private final ReviewRepo reviewRepo;
    private final RestaurantRepo restaurantRepo;
    private final AppUserRepo userRepo;

    public ReviewController(ReviewRepo reviewRepo, RestaurantRepo restaurantRepo, AppUserRepo userRepo) {
        this.reviewRepo = reviewRepo;
        this.restaurantRepo = restaurantRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantReview>> getRestaurantReviews(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewRepo.findByRestaurantIdOrderByCreatedAtDesc(restaurantId));
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody RestaurantReview review) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Must be logged in to leave a review"));
        }

        String username = auth.getName();
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (review.getRating() < 1 || review.getRating() > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }

        review.setUserId(user.getId());
        review.setUsername(user.getUsername());
        RestaurantReview saved = reviewRepo.save(review);

        // Dynamically update Restaurant Rating Average
        Restaurant restaurant = restaurantRepo.findById(review.getRestaurantId()).orElse(null);
        if (restaurant != null) {
            List<RestaurantReview> allReviews = reviewRepo.findByRestaurantIdOrderByCreatedAtDesc(restaurant.getId());
            double newAvg = allReviews.stream().mapToInt(RestaurantReview::getRating).average().orElse(0.0);
            
            // Round to 1 decimal place locally
            newAvg = Math.round(newAvg * 10.0) / 10.0;
            
            restaurant.setRating(newAvg);
            restaurantRepo.save(restaurant);
        }

        return ResponseEntity.ok(saved);
    }
}
