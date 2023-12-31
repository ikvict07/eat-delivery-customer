package com.delivery.customer.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MyFilterChain {
    @Value("${customer.security.login-url}")
    private String loginUrl;

    @Value("${customer.authentication.domain}")
    private String authenticationDomain;

    private TokenFilter tokenFilter;

    @Autowired
    private void setTokenFilter(TokenFilter tokenFilter) {
        this.tokenFilter = tokenFilter;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            if (request.getRequestURI().startsWith("/api/customer-db/")) {
                                config.setAllowedOrigins(List.of(authenticationDomain));
                                config.setAllowedMethods(Arrays.asList("GET", "POST")); // разрешенные методы
                                config.setAllowedHeaders(Arrays.asList("*")); // разрешенные заголовки
                                config.setAllowCredentials(false); // разрешить cookies
                            } else {
                                config.applyPermitDefaultValues(); // для остальных URL применяем значения по умолчанию
                            }
                            return config;
                        }))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers("/api/customer/**").fullyAuthenticated()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}