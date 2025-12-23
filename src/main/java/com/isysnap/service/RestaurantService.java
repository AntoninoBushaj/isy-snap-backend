package com.isysnap.service;

import com.isysnap.dto.RestaurantDTO;
import com.isysnap.dto.TableDTO;
import com.isysnap.dto.request.CreateRestaurantRequest;
import com.isysnap.dto.request.CreateTableRequest;
import com.isysnap.dto.request.UpdateRestaurantRequest;
import com.isysnap.entity.Restaurant;
import com.isysnap.entity.RestaurantTable;
import com.isysnap.repository.RestaurantRepository;
import com.isysnap.repository.RestaurantTableRepository;
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
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    // ========== RESTAURANT CRUD ==========

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantById(String restaurantId) {
        log.info("Getting restaurant by ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        return RestaurantDTO.fromEntity(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        log.info("Getting all restaurants");

        List<Restaurant> restaurants = restaurantRepository.findAll();

        return restaurants.stream()
                .map(RestaurantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantDTO createRestaurant(CreateRestaurantRequest request) {
        log.info("Creating restaurant: {}", request.getName());

        Restaurant restaurant = Restaurant.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .logo(request.getLogo())
                .status("ACTIVE")
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        log.info("Created restaurant: {}", savedRestaurant.getId());

        return RestaurantDTO.fromEntity(savedRestaurant);
    }

    @Transactional
    public RestaurantDTO updateRestaurant(String restaurantId, UpdateRestaurantRequest request) {
        log.info("Updating restaurant: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(request.getPhone());
        }
        if (request.getLogo() != null) {
            restaurant.setLogo(request.getLogo());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        log.info("Updated restaurant: {}", restaurantId);

        return RestaurantDTO.fromEntity(updatedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(String restaurantId) {
        log.info("Deleting restaurant: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        restaurantRepository.delete(restaurant);

        log.info("Deleted restaurant: {}", restaurantId);
    }

    // ========== TABLE MANAGEMENT (Parent-Child) ==========

    @Transactional
    public TableDTO createTable(String restaurantId, CreateTableRequest request) {
        log.info("Creating table {} for restaurant: {}", request.getCode(), restaurantId);

        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        // Generate unique QR slug
        String qrSlug = "qr-" + UUID.randomUUID().toString().substring(0, 13);

        RestaurantTable table = RestaurantTable.builder()
                .id(UUID.randomUUID().toString())
                .restaurant(restaurant)
                .code(request.getCode())
                .qrSlug(qrSlug)
                .isActive(true)
                .build();

        RestaurantTable savedTable = restaurantTableRepository.save(table);

        log.info("Created table: {} with QR slug: {}", savedTable.getId(), qrSlug);

        return TableDTO.fromEntity(savedTable);
    }

    @Transactional(readOnly = true)
    public List<TableDTO> getRestaurantTables(String restaurantId) {
        log.info("Getting tables for restaurant: {}", restaurantId);

        List<RestaurantTable> tables = restaurantTableRepository.findByRestaurantId(restaurantId);

        return tables.stream()
                .map(TableDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableDTO getTableById(String tableId) {
        log.info("Getting table by ID: {}", tableId);

        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found: " + tableId));

        return TableDTO.fromEntity(table);
    }

    @Transactional(readOnly = true)
    public TableDTO getTableByQrSlug(String qrSlug) {
        log.info("Getting table by QR slug: {}", qrSlug);

        RestaurantTable table = restaurantTableRepository.findByQrSlug(qrSlug)
                .orElseThrow(() -> new RuntimeException("Table not found for QR slug: " + qrSlug));

        return TableDTO.fromEntity(table);
    }

    @Transactional
    public void deleteTable(String tableId) {
        log.info("Deleting table: {}", tableId);

        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found: " + tableId));

        restaurantTableRepository.delete(table);

        log.info("Deleted table: {}", tableId);
    }
}