package com.isysnap.config;

import com.isysnap.security.CustomAccessDeniedHandler;
import com.isysnap.security.CustomAuthenticationEntryPoint;
import com.isysnap.security.JwtAuthenticationFilter;
import com.isysnap.security.RateLimitingFilter;
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
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

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
                        .requestMatchers(HttpMethod.POST, "/api/auth/registerUser").authenticated()
                        // Logout requires authentication (any authenticated user)
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // ========== QR CODE FLOW (Public - No Authentication) ==========
                        // QR code authorization
                        .requestMatchers(HttpMethod.POST, "/api/qr/authorizeQr").permitAll()  // QR code authorization

                        // Session management
                        .requestMatchers(HttpMethod.POST, "/api/sessions/startSession").permitAll()  // Start session for guests (QR code)
                        .requestMatchers(HttpMethod.GET, "/api/sessions/getSession/**").permitAll()  // Get session info

                        // Restaurant info (public — needed after QR scan)
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/getRestaurantInfo/**").permitAll()

                        // Menu viewing (all public — guests need to browse before ordering)
                        .requestMatchers(HttpMethod.GET, "/api/menu/getRestaurantMenu/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu/getMenuByCategory/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu/getMenuCategories/**").permitAll()

                        // Order management (cart) — requires valid guest session JWT (ROLE_GUEST)
                        .requestMatchers(HttpMethod.POST, "/api/orders/addItemToCart").hasRole("GUEST")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/updateCartItem/**").hasRole("GUEST")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/removeCartItem/**").hasRole("GUEST")
                        .requestMatchers(HttpMethod.GET, "/api/orders/getCart").hasRole("GUEST")
                        .requestMatchers(HttpMethod.GET, "/api/orders/getOrderHistory").hasRole("GUEST")

                        // Payment processing — checkout requires guest session JWT; webhooks are public (machine-to-machine)
                        .requestMatchers(HttpMethod.POST, "/api/payments/createCheckout").hasRole("GUEST")
                        .requestMatchers(HttpMethod.GET, "/api/payments/getPaymentStatus/**").hasRole("GUEST")
                        .requestMatchers(HttpMethod.POST, "/api/payments/processWebhook/**").permitAll()  // Payment provider webhooks (no user auth)
                        .requestMatchers(HttpMethod.POST, "/api/payments/confirmPayment/**").permitAll()  // Manual confirmation (testing only)

                        // ========== ALL OTHER ENDPOINTS (Authenticated with specific roles) ==========
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(sessionGuestAuthenticationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS");
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isBlank()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOriginsEnv.split(",")));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "http://localhost:5173"
            ));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin",
                "X-Requested-With", "Cache-Control"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}