package com.inventra.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/reports/**").hasAnyRole("ADMIN","MANAGER")
                        .requestMatchers("/api/products/admin/**").hasAnyRole("ADMIN","STAFF")
                        .requestMatchers("/api/products/supplier/**").hasAnyRole("SUPPLIER","ADMIN","MANAGER")
                        .requestMatchers("/api/products/**").hasAnyRole("STAFF", "ADMIN", "SUPPLIER")
                        .requestMatchers("/api/purchases/**").hasAnyRole("ADMIN","MANAGER","SUPPLIER")
                        .requestMatchers("/api/sales/**").hasAnyRole("STAFF", "ADMIN","MANAGER")
                        .requestMatchers("/api/managers/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN","MANAGER","STAFF")
                        .requestMatchers("/api/inventory-transactions/**").hasAnyRole("ADMIN","MANAGER","STAFF")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:3000"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}