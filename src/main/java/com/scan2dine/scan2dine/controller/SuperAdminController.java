package com.scan2dine.scan2dine.controller;

import com.scan2dine.scan2dine.entity.AppUser;
import com.scan2dine.scan2dine.entity.CustomerOrder;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.repo.AppUserRepo;
import com.scan2dine.scan2dine.repo.CustomerOrderRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin(origins = "${app.frontend.url}")
public class SuperAdminController {

    private final RestaurantRepo restaurantRepo;
    private final AppUserRepo userRepo;
    private final CustomerOrderRepo orderRepo;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminController(RestaurantRepo restaurantRepo, AppUserRepo userRepo,
                                CustomerOrderRepo orderRepo, PasswordEncoder passwordEncoder) {
        this.restaurantRepo = restaurantRepo;
        this.userRepo = userRepo;
        this.orderRepo = orderRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // =========== STATS ===========
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalRestaurants = restaurantRepo.count();
        long totalUsers = userRepo.count();
        long totalOrders = orderRepo.count();

        return ResponseEntity.ok(Map.of(
                "totalRestaurants", totalRestaurants,
                "totalUsers", totalUsers,
                "totalOrders", totalOrders
        ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<com.scan2dine.scan2dine.dto.AnalyticsResponse> getAnalytics() {
        com.scan2dine.scan2dine.dto.AnalyticsResponse res = new com.scan2dine.scan2dine.dto.AnalyticsResponse();
        res.setTotalRestaurants(restaurantRepo.count());
        res.setTotalUsers(userRepo.count());
        res.setTotalOrders(orderRepo.count());

        List<CustomerOrder> paidOrders = orderRepo.findByPaymentDoneTrue();
        double totalRev = paidOrders.stream().mapToDouble(CustomerOrder::getTotalAmount).sum();
        res.setTotalRevenue(totalRev);

        // Calculate top restaurants
        Map<Long, Integer> restOrderCount = new HashMap<>();
        for (CustomerOrder o : orderRepo.findAll()) {
            restOrderCount.put(o.getRestaurantId(), restOrderCount.getOrDefault(o.getRestaurantId(), 0) + 1);
        }

        List<Map<String, Object>> topRestData = new ArrayList<>();
        List<Restaurant> allRest = restaurantRepo.findAll();
        for (Restaurant r : allRest) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", r.getName());
            map.put("orders", restOrderCount.getOrDefault(r.getId(), 0));
            topRestData.add(map);
        }
        // sort descending by orders
        topRestData.sort((a, b) -> Integer.compare((int) b.get("orders"), (int) a.get("orders")));
        if (topRestData.size() > 5) topRestData = topRestData.subList(0, 5);
        res.setTopRestaurants(topRestData);

        // Timeline (mocking for 7 days or aggregating simply by day)
        // Group by Day of Week 
        Map<java.time.DayOfWeek, Double> dayRev = new EnumMap<>(java.time.DayOfWeek.class);
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) dayRev.put(d, 0.0);
        
        for (CustomerOrder o : paidOrders) {
            java.time.DayOfWeek dow = o.getCreatedAt().getDayOfWeek();
            dayRev.put(dow, dayRev.get(dow) + o.getTotalAmount());
        }

        List<Map<String, Object>> timeline = new ArrayList<>();
        // Java DayOfWeek is 1=Mon, 7=Sun
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) {
            Map<String, Object> map = new HashMap<>();
            String dayStr = d.name().substring(0, 1) + d.name().substring(1, 3).toLowerCase();
            map.put("name", dayStr);
            map.put("revenue", dayRev.get(d));
            timeline.add(map);
        }
        res.setRevenueTimeline(timeline);

        return ResponseEntity.ok(res);
    }

    // =========== RESTAURANTS ===========
    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantRepo.findAll());
    }

    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantRepo.save(restaurant));
    }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant updated) {
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
        return ResponseEntity.ok(restaurantRepo.save(r));
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        restaurantRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Restaurant deleted"));
    }

    // =========== USERS ===========
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (AppUser u : userRepo.findAll()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("restaurantId", u.getRestaurantId());
            users.add(m);
        }
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (userRepo.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username exists"));
        }

        AppUser user = AppUser.builder()
                .username(username)
                .email(body.getOrDefault("email", ""))
                .password(passwordEncoder.encode(body.getOrDefault("password", "password123")))
                .role(body.getOrDefault("role", "CUSTOMER"))
                .restaurantId(body.get("restaurantId") != null ? Long.parseLong(body.get("restaurantId")) : null)
                .build();

        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "User created", "id", user.getId()));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.containsKey("role")) user.setRole(body.get("role"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("restaurantId")) {
            user.setRestaurantId(body.get("restaurantId") != null ? Long.parseLong(body.get("restaurantId")) : null);
        }
        if (body.containsKey("password") && !body.get("password").isEmpty()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }

        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "User updated"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    // =========== ALL ORDERS ===========
    @GetMapping("/orders")
    public ResponseEntity<List<CustomerOrder>> getAllOrders() {
        return ResponseEntity.ok(orderRepo.findAll());
    }
}
