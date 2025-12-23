package com.isysnap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for IsySnap Backend API
 * Configures JWT authentication for protected endpoints
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define JWT security scheme
        SecurityScheme jwtScheme = new SecurityScheme()
                .name("bearerAuth")
                .description("Enter your JWT token obtained from /api/auth/login endpoint. Format: just paste the token (no 'Bearer ' prefix needed)")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        return new OpenAPI()
                .info(new Info()
                        .title("IsySnap API")
                        .version("1.0")
                        .description("""
                                IsySnap Backend API - Restaurant ordering system

                                ## Authentication Flow:

                                ### For ADMIN/STAFF Users:
                                1. Register: `POST /api/auth/register` with role "ADMIN" or "STAFF"
                                2. Login: `POST /api/auth/login` to get JWT token
                                3. Click **Authorize** 🔓 button (top right) and paste the token
                                4. Now you can call protected endpoints

                                ### For Customers (QR Code Flow):
                                1. **Authorize QR**: `POST /api/qr/authorizeQr` with QR signature
                                   - Returns a JWT `sessionToken` containing sessionId + guestId
                                2. Click **Authorize** 🔓 and paste the `sessionToken` JWT
                                3. Now you can:
                                   - Add items to cart: `POST /api/orders/addItemToCart`
                                   - View cart: `GET /api/orders/getCart`
                                   - Create checkout: `POST /api/payments/createCheckout`

                                **Note**: All customer endpoints get guestId/sessionId from JWT (not from request body)

                                ## Roles:
                                - **ADMIN**: Full access to all orders and restaurants
                                - **STAFF**: Access to assigned restaurant only
                                - **CUSTOMER**: Access via QR code JWT (sessionToken)
                                """)
                        .contact(new Contact()
                                .name("IsySnap Team")
                                .email("support@isysnap.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", jwtScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
