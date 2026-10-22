package com.classicstore.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectProvider<JwtService> jwtServiceProvider;

    public JwtAuthenticationFilter(ObjectProvider<JwtService> jwtServiceProvider) {
        this.jwtServiceProvider = jwtServiceProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        JwtService jwtService = jwtServiceProvider.getIfAvailable();
        if (jwtService != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                JwtService.TokenClaims claims = jwtService.validateAndParseToken(token);
                if (claims.isValid(JwtService.TYPE_ACCESS)) {
                    UserPrincipal principal = new UserPrincipal(claims.userId(), claims.role());
                    String roleAuthority = "ROLE_" + (claims.role() != null ? claims.role() : "CUSTOMER");
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(roleAuthority),
                                    new SimpleGrantedAuthority(claims.role() != null ? claims.role() : "CUSTOMER")
                            )
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
