package com.tablesnap.controller;

import com.tablesnap.dto.response.MenuItemResponse;
import com.tablesnap.entity.MenuItem;
import com.tablesnap.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable String restaurantId) {
        List<MenuItem> menuItems = menuService.getMenuItems(restaurantId);

        List<MenuItemResponse> response = menuItems.stream()
                .map(item -> MenuItemResponse.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .image(item.getImage())
                        .category(item.getCategory())
                        .available(item.getAvailable())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}