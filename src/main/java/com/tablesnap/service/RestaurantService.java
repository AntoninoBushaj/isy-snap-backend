package com.tablesnap.service;

import com.tablesnap.entity.Restaurant;
import com.tablesnap.exception.RestaurantNotFoundException;
import com.tablesnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> findAll() {
        log.info("Finding all restaurants");
        return restaurantRepository.findAll();
    }

    public Restaurant findById(String restaurantId) {
        log.info("Finding restaurant: {}", restaurantId);
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }
}