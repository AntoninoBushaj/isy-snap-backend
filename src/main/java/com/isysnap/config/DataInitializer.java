package com.isysnap.config;

import com.isysnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


//@Component  // ← Disabilitato temporaneamente per evitare errori con dati vecchi
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;

    @Override
    public void run(String... args) {
        long count = restaurantRepository.count();
        if (count > 0) {
            log.info("Found {} restaurants. Skipping demo initializer.", count);
            return;
        }
        log.warn("No restaurants found. Execute docker/init-data.sql to load sample data.");
    }
}
