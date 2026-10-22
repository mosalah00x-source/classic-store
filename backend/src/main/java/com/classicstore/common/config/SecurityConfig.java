package com.classicstore.common.config;

import com.classicstore.common.exception.ApiErrorResponse;
import com.classicstore.common.filter.CorrelationIdFilter;
import com.classicstore.identity.security.CustomAuthenticationEntryPoint;
import com.classicstore.identity.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;
    private final ObjectProvider<CustomAuthenticationEntryPoint> customAuthenticationEntryPointProvider;
    private final ObjectMapper objectMapper;

    public SecurityConfig(CorrelationIdFilter correlationIdFilter,
                          ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider,
                          ObjectProvider<CustomAuthenticationEntryPoint> customAuthenticationEntryPointProvider,
                          ObjectMapper objectMapper) {
        this.correlationIdFilter = correlationIdFilter;
        this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
        this.customAuthenticationEntryPointProvider = customAuthenticationEntryPointProvider;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/api/v1/test/**", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/me/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);

        JwtAuthenticationFilter jwtFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        CustomAuthenticationEntryPoint entryPoint = customAuthenticationEntryPointProvider.getIfAvailable();
        http.exceptionHandling(ex -> {
            if (entryPoint != null) {
                ex.authenticationEntryPoint(entryPoint);
            }
            ex.accessDeniedHandler((request, response, accessDeniedException) -> {
                String traceId = MDC.get("traceId");
                if (traceId == null) {
                    traceId = "";
                }
                ApiErrorResponse errorResponse = new ApiErrorResponse(
                        "FORBIDDEN",
                        "غير مسموح بإجراء هذه العملية",
                        "Access denied",
                        traceId,
                        Map.of()
                );
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), errorResponse);
            });
        });

        return http.build();
    }
}
