package com.isysnap.config;

import com.isysnap.security.JwtAuthenticationFilter;
import com.isysnap.security.SessionGuestAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SessionGuestAuthenticationFilter sessionGuestAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Swagger/OpenAPI endpoints are public
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ========== AUTH ENDPOINTS ==========
                        // Login is public (anyone can attempt login)
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Register requires authentication (ADMIN only - validated by @PreAuthorize)
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").authenticated()
                        // Logout requires authentication (any authenticated user)
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // ========== DEV UTILS (TEMPORARY - Remove in production) ==========
                        .requestMatchers("/api/dev/**").permitAll()  // Development utilities

                        // ========== QR CODE FLOW (Public - No Authentication) ==========
                        // QR code authorization
                        .requestMatchers(HttpMethod.POST, "/api/qr/authorizeQr").permitAll()  // QR code authorization

                        // Session management
                        .requestMatchers(HttpMethod.POST, "/api/sessions/startSession").permitAll()  // Start session for guests (QR code)
                        .requestMatchers(HttpMethod.GET, "/api/sessions/getSession/**").permitAll()  // Get session info

                        // Menu viewing
                        .requestMatchers(HttpMethod.GET, "/api/menu/getRestaurantMenu/**").permitAll()  // View restaurant menus (QR code)
                        .requestMatchers(HttpMethod.GET, "/api/menu/getMenuCategories/**").permitAll()  // Get menu categories

                        // Order management (cart)
                        .requestMatchers(HttpMethod.POST, "/api/orders/addItemToCart").permitAll()  // Add items to cart
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/updateCartItem/**").permitAll()  // Update cart items
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/removeCartItem/**").permitAll()  // Remove cart items
                        .requestMatchers(HttpMethod.GET, "/api/orders/getCart").permitAll()  // View cart
                        .requestMatchers(HttpMethod.GET, "/api/orders/getOrderHistory").permitAll()  // View order history

                        // Payment processing
                        .requestMatchers(HttpMethod.POST, "/api/payments/createCheckout").permitAll()  // Create checkout
                        .requestMatchers(HttpMethod.GET, "/api/payments/getPaymentStatus/**").permitAll()  // Check payment status
                        .requestMatchers(HttpMethod.POST, "/api/payments/processWebhook/**").permitAll()  // Payment webhooks
                        .requestMatchers(HttpMethod.POST, "/api/payments/confirmPayment/**").permitAll()  // Manual confirmation (testing)

                        // ========== ALL OTHER ENDPOINTS (Authenticated with specific roles) ==========
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(sessionGuestAuthenticationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}