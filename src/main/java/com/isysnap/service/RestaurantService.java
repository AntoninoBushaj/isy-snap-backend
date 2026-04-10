package com.isysnap.service;

import com.isysnap.dto.RestaurantDTO;
import com.isysnap.dto.TableDTO;
import com.isysnap.dto.request.CreateRestaurantRequest;
import com.isysnap.dto.request.CreateTableRequest;
import com.isysnap.dto.request.UpdateRestaurantRequest;
import com.isysnap.entity.DiningSession;
import com.isysnap.entity.MenuItem;
import com.isysnap.entity.Order;
import com.isysnap.entity.Restaurant;
import com.isysnap.entity.RestaurantTable;
import com.isysnap.repository.DiningSessionGuestRepository;
import com.isysnap.repository.DiningSessionRepository;
import com.isysnap.repository.MenuCategoryRepository;
import com.isysnap.repository.MenuItemOptionRepository;
import com.isysnap.repository.MenuItemRepository;
import com.isysnap.repository.OrderItemOptionRepository;
import com.isysnap.repository.OrderItemRepository;
import com.isysnap.repository.OrderRepository;
import com.isysnap.repository.OrderStatusHistoryRepository;
import com.isysnap.repository.PaymentItemRepository;
import com.isysnap.repository.PaymentRepository;
import com.isysnap.repository.RestaurantRepository;
import com.isysnap.repository.RestaurantTableRepository;
import com.isysnap.repository.RestaurantUserRepository;
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
    private final DiningSessionRepository diningSessionRepository;
    private final DiningSessionGuestRepository diningSessionGuestRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemOptionRepository menuItemOptionRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantUserRepository restaurantUserRepository;

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
        log.info("Deleting restaurant: {} — starting cascade cleanup", restaurantId);

        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        // 1. Get all sessions for this restaurant
        List<DiningSession> sessions = diningSessionRepository.findByRestaurantIdOrderByOpenedAtDesc(restaurantId);
        List<String> sessionIds = sessions.stream().map(DiningSession::getId).collect(Collectors.toList());

        if (!sessionIds.isEmpty()) {
            // 2. Delete payment_items and payments first (PaymentItem has FK to OrderItem)
            sessionIds.forEach(sid -> {
                paymentRepository.findByDiningSessionId(sid).forEach(payment -> {
                    paymentItemRepository.findByPaymentId(payment.getId())
                            .forEach(paymentItemRepository::delete);
                    paymentRepository.delete(payment);
                });
            });

            // 3. Get all orders for these sessions
            List<Order> orders = sessionIds.stream()
                    .flatMap(sid -> orderRepository.findByDiningSessionId(sid).stream())
                    .collect(Collectors.toList());
            List<String> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

            if (!orderIds.isEmpty()) {
                // 4. Delete order_item_options
                orderIds.forEach(orderId ->
                        orderItemOptionRepository.deleteByOrderItemOrderId(orderId));

                // 5. Delete order_status_history
                orderIds.forEach(orderId ->
                        orderStatusHistoryRepository.deleteByOrderId(orderId));

                // 6. Delete order_items
                orderIds.forEach(orderId ->
                        orderItemRepository.findByOrderId(orderId)
                                .forEach(orderItemRepository::delete));

                // 7. Delete orders
                orders.forEach(orderRepository::delete);
            }

            // 8. Delete dining session guests
            sessionIds.forEach(sid ->
                    diningSessionGuestRepository.findByDiningSessionId(sid)
                            .forEach(diningSessionGuestRepository::delete));

            // 9. Delete dining sessions
            sessions.forEach(diningSessionRepository::delete);
        }

        // 11. Delete menu item options and menu items
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);
        menuItems.forEach(item -> {
            menuItemOptionRepository.deleteByMenuItemId(item.getId());
            menuItemRepository.delete(item);
        });

        // 12. Delete menu categories
        menuCategoryRepository.findByRestaurantIdOrderBySortOrderAsc(restaurantId)
                .forEach(menuCategoryRepository::delete);

        // 13. Delete restaurant tables
        restaurantTableRepository.findByRestaurantId(restaurantId)
                .forEach(restaurantTableRepository::delete);

        // 14. Delete restaurant user assignments
        restaurantUserRepository.findActiveByRestaurantId(restaurantId)
                .forEach(restaurantUserRepository::delete);

        // 15. Delete the restaurant itself
        restaurantRepository.deleteById(restaurantId);

        log.info("Deleted restaurant: {} and all associated data", restaurantId);
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