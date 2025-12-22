package com.tablesnap.controller;

import com.tablesnap.dto.response.RestaurantResponse;
import com.tablesnap.entity.Restaurant;
import com.tablesnap.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.findAll();

        List<RestaurantResponse> responses = restaurants.stream()
                .map(restaurant -> RestaurantResponse.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .description(restaurant.getDescription())
                        .address(restaurant.getAddress())
                        .phone(restaurant.getPhone())
                        .logo(restaurant.getLogo())
                        .image(restaurant.getImage())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable String id) {
        Restaurant restaurant = restaurantService.findById(id);

        RestaurantResponse response = RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .logo(restaurant.getLogo())
                .image(restaurant.getImage())
                .build();

        return ResponseEntity.ok(response);
    }
}
