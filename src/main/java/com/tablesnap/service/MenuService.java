package com.tablesnap.service;

import com.tablesnap.entity.MenuItem;
import com.tablesnap.exception.RestaurantNotFoundException;
import com.tablesnap.repository.MenuItemRepository;
import com.tablesnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public List<MenuItem> getMenuItems(String restaurantId) {
        log.info("Getting menu items for restaurant: {}", restaurantId);

        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        return menuItemRepository.findByRestaurantId(restaurantId);
    }
}