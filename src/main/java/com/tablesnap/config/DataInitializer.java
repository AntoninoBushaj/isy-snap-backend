package com.tablesnap.config;

import com.tablesnap.entity.MenuItem;
import com.tablesnap.entity.Restaurant;
import com.tablesnap.repository.MenuItemRepository;
import com.tablesnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing mock data...");

        // Create restaurant
        Restaurant restaurant = Restaurant.builder()
                .id("restaurant-001")
                .name("La Bella Vita")
                .description("Authentic Italian cuisine with a modern twist")
                .address("Via Roma 123, Milano")
                .phone("+39 02 1234 5678")
                .logo("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=200&h=200&fit=crop")
                .menuItems(new ArrayList<>())
                .build();

        restaurantRepository.save(restaurant);
        log.info("Created restaurant: {}", restaurant.getName());

        // Create menu items
        createMenuItem(restaurant, "item-001", "Margherita Pizza",
                "Classic pizza with tomato, mozzarella, and fresh basil",
                new BigDecimal("12.99"),
                "https://images.unsplash.com/photo-1604068549290-dea0e4a305ca?w=400&h=300&fit=crop",
                "Pizza", true);

        createMenuItem(restaurant, "item-002", "Quattro Formaggi",
                "Four cheese pizza: mozzarella, gorgonzola, parmesan, and fontina",
                new BigDecimal("15.99"),
                "https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?w=400&h=300&fit=crop",
                "Pizza", true);

        createMenuItem(restaurant, "item-003", "Diavola",
                "Spicy pizza with salami, hot peppers, and mozzarella",
                new BigDecimal("14.99"),
                "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400&h=300&fit=crop",
                "Pizza", true);

        createMenuItem(restaurant, "item-004", "Carbonara",
                "Traditional pasta with eggs, pecorino cheese, guanciale, and black pepper",
                new BigDecimal("13.99"),
                "https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop",
                "Pasta", true);

        createMenuItem(restaurant, "item-005", "Amatriciana",
                "Pasta with tomato sauce, guanciale, and pecorino cheese",
                new BigDecimal("12.99"),
                "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400&h=300&fit=crop",
                "Pasta", true);

        createMenuItem(restaurant, "item-006", "Cacio e Pepe",
                "Simple pasta with pecorino cheese and black pepper",
                new BigDecimal("11.99"),
                "https://images.unsplash.com/photo-1598866594230-a7c12756260f?w=400&h=300&fit=crop",
                "Pasta", true);

        createMenuItem(restaurant, "item-007", "Tiramisu",
                "Classic Italian dessert with coffee-soaked ladyfingers and mascarpone cream",
                new BigDecimal("6.99"),
                "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400&h=300&fit=crop",
                "Desserts", true);

        createMenuItem(restaurant, "item-008", "Panna Cotta",
                "Creamy Italian dessert with berry sauce",
                new BigDecimal("5.99"),
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=300&fit=crop",
                "Desserts", true);

        createMenuItem(restaurant, "item-009", "Gelato",
                "Artisanal Italian ice cream (3 scoops)",
                new BigDecimal("7.99"),
                "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=400&h=300&fit=crop",
                "Desserts", true);

        createMenuItem(restaurant, "item-010", "Espresso",
                "Strong Italian coffee",
                new BigDecimal("2.50"),
                "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=400&h=300&fit=crop",
                "Beverages", true);

        createMenuItem(restaurant, "item-011", "Cappuccino",
                "Coffee with steamed milk and foam",
                new BigDecimal("3.50"),
                "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400&h=300&fit=crop",
                "Beverages", true);

        createMenuItem(restaurant, "item-012", "Prosecco",
                "Italian sparkling wine (glass)",
                new BigDecimal("6.00"),
                "https://images.unsplash.com/photo-1547595628-c61a29f496f0?w=400&h=300&fit=crop",
                "Beverages", true);

        log.info("Mock data initialization completed!");
    }

    private void createMenuItem(Restaurant restaurant, String id, String name,
                                String description, BigDecimal price, String image,
                                String category, boolean available) {
        MenuItem menuItem = MenuItem.builder()
                .id(id)
                .restaurant(restaurant)
                .name(name)
                .description(description)
                .price(price)
                .image(image)
                .category(category)
                .available(available)
                .build();

        menuItemRepository.save(menuItem);
        log.info("Created menu item: {}", name);
    }
}