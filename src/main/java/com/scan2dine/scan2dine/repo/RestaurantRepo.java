package com.scan2dine.scan2dine.repo;

import com.scan2dine.scan2dine.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findBySlug(String slug);
}
