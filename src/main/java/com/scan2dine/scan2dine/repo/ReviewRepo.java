package com.scan2dine.scan2dine.repo;

import com.scan2dine.scan2dine.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepo extends JpaRepository<RestaurantReview, Long> {
    List<RestaurantReview> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<RestaurantReview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
