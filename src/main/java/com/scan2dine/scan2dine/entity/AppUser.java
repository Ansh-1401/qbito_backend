package com.scan2dine.scan2dine.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // CUSTOMER, RESTAURANT_ADMIN, SUPER_ADMIN

    // Links restaurant admins to their restaurant
    private Long restaurantId;
}
