package com.isysnap.controller;

import com.isysnap.dto.MenuCategoryDTO;
import com.isysnap.dto.MenuItemDTO;
import com.isysnap.dto.request.CreateMenuItemRequest;
import com.isysnap.dto.request.UpdateAvailabilityRequest;
import com.isysnap.dto.request.UpdateMenuItemRequest;
import com.isysnap.dto.response.SuccessResponse;
import com.isysnap.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Menu items management")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/getRestaurantMenu/{restaurantId}")
    @Operation(summary = "Get restaurant menu", description = "PUBLIC: Get all available menu items")
    public ResponseEntity<List<MenuItemDTO>> getRestaurantMenu(@PathVariable String restaurantId) {
        List<MenuItemDTO> menuItems = menuService.getAvailableMenuItems(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/getMenuByCategory/{restaurantId}/{categoryId}")
    @Operation(summary = "Get menu by category", description = "PUBLIC: Get menu items by category")
    public ResponseEntity<List<MenuItemDTO>> getMenuByCategory(
            @PathVariable String restaurantId,
            @PathVariable String categoryId) {

        List<MenuItemDTO> menuItems = menuService.getMenuItemsByCategory(restaurantId, categoryId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/getMenuCategories/{restaurantId}")
    @Operation(summary = "Get categories", description = "PUBLIC: Get all menu categories for restaurant")
    public ResponseEntity<List<MenuCategoryDTO>> getCategories(@PathVariable String restaurantId) {
        List<MenuCategoryDTO> categories = menuService.getRestaurantCategories(restaurantId);
        return ResponseEntity.ok(categories);
    }

    // STAFF APIs

    @PostMapping("/createMenuItem")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Create menu item", description = "STAFF: Add new menu item")
    public ResponseEntity<MenuItemDTO> createMenuItem(@RequestBody CreateMenuItemRequest request) {
        MenuItemDTO menuItem = menuService.createMenuItem(request);
        return ResponseEntity.ok(menuItem);
    }

    @PatchMapping("/updateMenuItem/{itemId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Update menu item", description = "STAFF: Update menu item details")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable String itemId,
            @RequestBody UpdateMenuItemRequest request) {

        MenuItemDTO menuItem = menuService.updateMenuItem(itemId, request);
        return ResponseEntity.ok(menuItem);
    }

    @PatchMapping("/updateMenuItemAvailability/{itemId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Update item availability", description = "STAFF: Toggle menu item availability")
    public ResponseEntity<SuccessResponse> updateAvailability(
            @PathVariable String itemId,
            @RequestBody UpdateAvailabilityRequest request) {

        menuService.updateMenuItemAvailability(itemId, request.getAvailable());
        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @DeleteMapping("/deleteMenuItem/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete menu item", description = "ADMIN: Delete menu item")
    public ResponseEntity<SuccessResponse> deleteMenuItem(@PathVariable String itemId) {
        menuService.deleteMenuItem(itemId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }
}
