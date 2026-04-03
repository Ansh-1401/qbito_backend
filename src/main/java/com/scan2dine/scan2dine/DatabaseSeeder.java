package com.scan2dine.scan2dine;

import com.scan2dine.scan2dine.entity.AppUser;
import com.scan2dine.scan2dine.entity.MenuItem;
import com.scan2dine.scan2dine.entity.Restaurant;
import com.scan2dine.scan2dine.repo.AppUserRepo;
import com.scan2dine.scan2dine.repo.MenuItemRepo;
import com.scan2dine.scan2dine.repo.RestaurantRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final RestaurantRepo restaurantRepo;
    private final MenuItemRepo menuItemRepo;
    private final AppUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RestaurantRepo restaurantRepo, MenuItemRepo menuItemRepo,
                          AppUserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.restaurantRepo = restaurantRepo;
        this.menuItemRepo = menuItemRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepo.count() == 0) {
            System.out.println("==============================================");
            System.out.println("No restaurants found in DB! Seeding demo data...");
            System.out.println("==============================================");

            // 1. Pizza Plaza
            Restaurant pizzaHub = Restaurant.builder()
                    .name("Pizza Plaza")
                    .slug("pizza-hub")
                    .address("Digiha, Bahraich")
                    .tags("Burgers • Pizza • Shakes")
                    .rating(4.6)
                    .eta("20-30 min")
                    .avgPrice(200)
                    .category("Pizza")
                    .openTime("11:00 AM - 11:00 PM")
                    .cover("https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=481&auto=format&fit=crop")
                    .build();

            restaurantRepo.save(pizzaHub);

            menuItemRepo.save(MenuItem.builder()
                    .restaurant(pizzaHub)
                    .name("Margherita Pizza")
                    .price(199)
                    .category("Pizza")
                    .type("veg")
                    .description("Classic cheese & tomato base")
                    .image("https://images.unsplash.com/photo-1601924582970-9238bcb495d9?q=80&w=1200&auto=format&fit=crop")
                    .available(true)
                    .build());

            menuItemRepo.save(MenuItem.builder()
                    .restaurant(pizzaHub)
                    .name("Chicken Pepperoni Pizza")
                    .price(349)
                    .category("Pizza")
                    .type("nonveg")
                    .description("Pepperoni + cheese overload")
                    .image("https://images.unsplash.com/photo-1590947132387-155cc02f3212?q=80&w=1200&auto=format&fit=crop")
                    .available(true)
                    .build());

            // 2. Sanjha Chulha
            Restaurant demoRestro = Restaurant.builder()
                    .name("Sanjha Chulha")
                    .slug("demo")
                    .address("Bahraich, India")
                    .tags("Family Restaurant")
                    .rating(4.1)
                    .eta("10-20 min")
                    .avgPrice(200)
                    .category("Dinner")
                    .openTime("10:00 AM - 11:00 PM")
                    .cover("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1400&q=80")
                    .build();

            restaurantRepo.save(demoRestro);

            menuItemRepo.save(MenuItem.builder()
                    .restaurant(demoRestro)
                    .name("Paneer Butter Masala")
                    .price(220)
                    .category("Main Course")
                    .type("veg")
                    .description("Creamy buttery paneer gravy")
                    .image("https://images.unsplash.com/photo-1604908177225-6f268d5e6d86?q=80&w=1200&auto=format&fit=crop")
                    .available(true)
                    .build());

            menuItemRepo.save(MenuItem.builder()
                    .restaurant(demoRestro)
                    .name("Chicken Curry")
                    .price(260)
                    .category("Main Course")
                    .type("nonveg")
                    .description("Spicy chicken curry (desi style)")
                    .image("https://images.unsplash.com/photo-1604909052772-4e1b324d1b7a?q=80&w=1200&auto=format&fit=crop")
                    .available(true)
                    .build());
                    
            // 3. Anna Madrasi
            Restaurant annaMadrasi = Restaurant.builder()
                    .name("Anna Madrasi")
                    .slug("south-tadka")
                    .address("Pani-tanki, Bahraich")
                    .tags("Dosa • Idli • Vada")
                    .rating(4.5)
                    .eta("20-30 min")
                    .avgPrice(150)
                    .category("South")
                    .openTime("08:00 AM - 10:00 PM")
                    .cover("https://media.istockphoto.com/id/1418100766/photo/group-of-south-indian-food-like-masala-dosa-idli-wada-or-vada-sambar-served-over-banana-leaf.webp?a=1&b=1&s=612x612&w=0&k=20&c=KsHdbNKgFOeQjNDt4Z6B6lLFKpkj-I7yIANRjNRTH5w=")
                    .build();
                    
            restaurantRepo.save(annaMadrasi);
            
            menuItemRepo.save(MenuItem.builder()
                    .restaurant(annaMadrasi)
                    .name("Masala Dosa")
                    .price(130)
                    .category("Main Course")
                    .type("veg")
                    .description("Crispy dosa with spicy potato filling")
                    .image("https://images.unsplash.com/photo-1589301760014-d929f39ce9b1?w=500&auto=format&fit=crop&q=60")
                    .available(true)
                    .build());

            System.out.println("==============================================");
            System.out.println("Seeding completed successfully.");
            System.out.println("==============================================");
        }

        // Seed default users
        if (userRepo.count() == 0) {
            System.out.println("Seeding default users...");

            userRepo.save(AppUser.builder()
                    .username("admin")
                    .email("admin@scan2dine.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("SUPER_ADMIN")
                    .build());

            userRepo.save(AppUser.builder()
                    .username("restoadmin")
                    .email("resto@scan2dine.com")
                    .password(passwordEncoder.encode("resto123"))
                    .role("RESTAURANT_ADMIN")
                    .restaurantId(1L)
                    .build());

            System.out.println("Default users seeded: admin/admin123 (SUPER_ADMIN), restoadmin/resto123 (RESTAURANT_ADMIN)");
        }
    }
}
