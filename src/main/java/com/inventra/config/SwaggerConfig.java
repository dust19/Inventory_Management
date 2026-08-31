package com.inventra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventra – Inventory Management System API",
                version = "1.0.0",
                description = """
                        Welcome to the Inventra Inventory Management System API.
                        
                        This interactive Swagger UI allows you to explore and test all available APIs
                        using JWT-based authentication.
                        
                        🔐 How to use this Swagger UI:
                        
                        1️⃣ Authenticate by calling the Login API:
                           POST /auth/login
                        
                        2️⃣ Copy the JWT token returned in the response.
                        
                        3️⃣ Click the Authorize 🔐 button at the top-right corner of this page.
                        
                        4️⃣ Enter the token in the following format:
                           Bearer <your-jwt-token>
                        
                        5️⃣ Click Authorize and start testing secured APIs
                           (Admin, Supplier, User) directly from Swagger UI.
                        """
        ),
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {

                @Tag(
                        name = "Authentication",
                        description = "User registration, login and current-user APIs"
                ),

                @Tag(
                        name = "User Management",
                        description = "User profile management, password update, activation & deletion APIs"
                ),

                @Tag(
                        name = "Supplier Management",
                        description = "Supplier onboarding, update, activation and lookup APIs"
                ),

                @Tag(
                        name = "Category Management",
                        description = "Category creation, update, listing and deletion APIs"
                ),

                @Tag(
                        name = "Product Management",
                        description = "Product creation, supplier stock management, SKU and pricing APIs"
                ),

                @Tag(
                        name = "Purchase Management",
                        description = "Admin purchases from suppliers, stock intake and delivery APIs"
                ),

                @Tag(
                        name = "Inventory Management",
                        description = "Admin inventory stock, reorder levels and product stock tracking APIs"
                ),

                @Tag(
                        name = "Inventory Transactions",
                        description = "Automatic stock IN/OUT transaction logs for purchases and sales"
                ),

                @Tag(
                        name = "Sales",
                        description = "Customer sales, billing, order status and delivery APIs"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication using Bearer Token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}