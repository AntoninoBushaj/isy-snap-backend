package com.isysnap.controller;

import com.isysnap.dto.RestaurantDTO;
import com.isysnap.dto.TableDTO;
import com.isysnap.dto.request.CreateRestaurantRequest;
import com.isysnap.dto.request.CreateTableRequest;
import com.isysnap.dto.request.UpdateRestaurantRequest;
import com.isysnap.dto.response.SuccessResponse;
import com.isysnap.service.RestaurantService;
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
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/getRestaurantInfo/{restaurantId}")
    @Operation(summary = "Get restaurant info", description = "PUBLIC: Get restaurant details")
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable String restaurantId) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/getAllRestaurants")
    @PreAuthorize("hasAuthority('restaurant:read') and hasAuthority('user:read')")
    @Operation(summary = "List all restaurants", description = "ADMIN: Get all restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @PostMapping("/createRestaurant")
    @PreAuthorize("hasAuthority('restaurant:create')")
    @Operation(summary = "Create restaurant", description = "ADMIN: Create new restaurant")
    public ResponseEntity<RestaurantDTO> createRestaurant(@RequestBody CreateRestaurantRequest request) {
        RestaurantDTO restaurant = restaurantService.createRestaurant(request);
        return ResponseEntity.ok(restaurant);
    }

    @PatchMapping("/updateRestaurant/{restaurantId}")
    @PreAuthorize("hasAuthority('restaurant:update') and @restaurantAccessValidator.hasAccessToRestaurant(#restaurantId)")
    @Operation(summary = "Update restaurant", description = "STAFF/ADMIN: Update restaurant details")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable String restaurantId,
            @RequestBody UpdateRestaurantRequest request) {

        RestaurantDTO restaurant = restaurantService.updateRestaurant(restaurantId, request);
        return ResponseEntity.ok(restaurant);
    }

    @GetMapping("/getRestaurantTables/{restaurantId}")
    @PreAuthorize("hasAuthority('restaurant:read') and @restaurantAccessValidator.hasAccessToRestaurant(#restaurantId)")
    @Operation(summary = "Get restaurant tables", description = "STAFF/ADMIN: List all tables for restaurant")
    public ResponseEntity<List<TableDTO>> getRestaurantTables(@PathVariable String restaurantId) {
        List<TableDTO> tables = restaurantService.getRestaurantTables(restaurantId);
        return ResponseEntity.ok(tables);
    }

    @PostMapping("/createTable/{restaurantId}")
    @PreAuthorize("hasAuthority('restaurant:update') and @restaurantAccessValidator.hasAccessToRestaurant(#restaurantId)")
    @Operation(summary = "Create table", description = "STAFF/ADMIN: Add new table to restaurant")
    public ResponseEntity<TableDTO> createTable(
            @PathVariable String restaurantId,
            @RequestBody CreateTableRequest request) {

        TableDTO table = restaurantService.createTable(restaurantId, request);
        return ResponseEntity.ok(table);
    }

    @DeleteMapping("/deleteTable/{tableId}")
    @PreAuthorize("hasAuthority('restaurant:delete')")
    @Operation(summary = "Delete table", description = "ADMIN: Delete table")
    public ResponseEntity<SuccessResponse> deleteTable(@PathVariable String tableId) {
        restaurantService.deleteTable(tableId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @DeleteMapping("/deleteRestaurant/{restaurantId}")
    @PreAuthorize("hasAuthority('restaurant:delete')")
    @Operation(summary = "Delete restaurant", description = "ADMIN: Delete restaurant and all associated data")
    public ResponseEntity<SuccessResponse> deleteRestaurant(@PathVariable String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }
}
