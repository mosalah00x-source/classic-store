package com.classicstore.common.config;

import com.classicstore.common.filter.CorrelationIdFilter;
import com.classicstore.identity.security.CustomAuthenticationEntryPoint;
import com.classicstore.identity.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorrelationIdFilter correlationIdFilter;
    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;
    private final ObjectProvider<CustomAuthenticationEntryPoint> customAuthenticationEntryPointProvider;

    public SecurityConfig(CorrelationIdFilter correlationIdFilter,
                          ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider,
                          ObjectProvider<CustomAuthenticationEntryPoint> customAuthenticationEntryPointProvider) {
        this.correlationIdFilter = correlationIdFilter;
        this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
        this.customAuthenticationEntryPointProvider = customAuthenticationEntryPointProvider;
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
                        .anyRequest().permitAll()
                )
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);

        JwtAuthenticationFilter jwtFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        CustomAuthenticationEntryPoint entryPoint = customAuthenticationEntryPointProvider.getIfAvailable();
        if (entryPoint != null) {
            http.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));
        }

        return http.build();
    }
}
