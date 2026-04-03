package com.scan2dine.scan2dine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private Long userId;

    private String username;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
