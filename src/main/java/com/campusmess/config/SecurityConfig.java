package com.campusmess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for modern REST API / SPA interactions
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Enable H2 web console
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/student.html",
                    "/manager.html",
                    "/cook.html",
                    "/admin.html",
                    "/css/**",
                    "/js/**",
                    "/h2-console/**",
                    "/api/auth/**",
                    "/api/health"
                ).permitAll()
                .anyRequest().permitAll() // Allow client-side role handling with server-side validation
            );

        return http.build();
    }
}
