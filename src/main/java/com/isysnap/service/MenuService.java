package com.isysnap.service;

import com.isysnap.dto.MenuCategoryDTO;
import com.isysnap.dto.MenuItemDTO;
import com.isysnap.dto.request.CreateMenuItemRequest;
import com.isysnap.dto.request.UpdateMenuItemRequest;
import com.isysnap.entity.MenuCategory;
import com.isysnap.entity.MenuItem;
import com.isysnap.entity.Restaurant;
import com.isysnap.repository.MenuCategoryRepository;
import com.isysnap.repository.MenuItemRepository;
import com.isysnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;

    // ========== MENU ITEMS ==========

    @Transactional(readOnly = true)
    public MenuItemDTO getMenuItemById(String itemId) {
        log.info("Getting menu item by ID: {}", itemId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));

        return MenuItemDTO.fromEntity(menuItem);
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getRestaurantMenu(String restaurantId) {
        log.info("Getting full menu for restaurant: {}", restaurantId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);

        return menuItems.stream()
                .map(MenuItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getAvailableMenuItems(String restaurantId) {
        log.info("Getting available menu items for restaurant: {}", restaurantId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);

        // Filter only available items for public view
        return menuItems.stream()
                .filter(MenuItem::getIsAvailable)
                .map(MenuItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getMenuItemsByCategory(String restaurantId, String categoryId) {
        log.info("Getting menu items for restaurant: {} and category: {}", restaurantId, categoryId);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndCategoryId(restaurantId, categoryId);

        // Filter only available items for public view
        return menuItems.stream()
                .filter(MenuItem::getIsAvailable)
                .map(MenuItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemDTO createMenuItem(CreateMenuItemRequest request) {
        log.info("Creating menu item: {} for restaurant: {}", request.getName(), request.getRestaurantId());

        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + request.getRestaurantId()));

        // Validate category exists
        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        MenuItem menuItem = MenuItem.builder()
                .id(UUID.randomUUID().toString())
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .build();

        MenuItem savedItem = menuItemRepository.save(menuItem);

        log.info("Created menu item: {}", savedItem.getId());

        return MenuItemDTO.fromEntity(savedItem);
    }

    @Transactional
    public MenuItemDTO updateMenuItem(String itemId, UpdateMenuItemRequest request) {
        log.info("Updating menu item: {}", itemId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));

        if (request.getName() != null) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);

        log.info("Updated menu item: {}", itemId);

        return MenuItemDTO.fromEntity(updatedItem);
    }

    @Transactional
    public void updateMenuItemAvailability(String itemId, boolean available) {
        log.info("Updating availability for item: {} to {}", itemId, available);

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));

        menuItem.setIsAvailable(available);
        menuItemRepository.save(menuItem);

        log.info("Updated availability for item: {}", itemId);
    }

    @Transactional
    public void deleteMenuItem(String itemId) {
        log.info("Deleting menu item: {}", itemId);

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));

        menuItemRepository.delete(menuItem);

        log.info("Deleted menu item: {}", itemId);
    }

    // ========== CATEGORIES ==========

    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getRestaurantCategories(String restaurantId) {
        log.info("Getting categories for restaurant: {}", restaurantId);

        List<MenuCategory> categories = menuCategoryRepository.findByRestaurantIdOrderBySortOrderAsc(restaurantId);

        return categories.stream()
                .map(MenuCategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuCategoryDTO createCategory(String restaurantId, String name, Integer sortOrder) {
        log.info("Creating category: {} for restaurant: {}", name, restaurantId);

        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        MenuCategory category = MenuCategory.builder()
                .id(UUID.randomUUID().toString())
                .restaurant(restaurant)
                .name(name)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(category);

        log.info("Created category: {}", savedCategory.getId());

        return MenuCategoryDTO.fromEntity(savedCategory);
    }
}