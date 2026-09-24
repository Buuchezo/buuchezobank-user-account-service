package com.buuchezo.useraccountservice.security;

import com.buuchezo.useraccountservice.repository.AdminRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailsService;
    private final AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        String username;
        String authenticationType;

        try {
            username = jwtService.extractUsername(token);
            authenticationType = jwtService.extractAuthenticationType(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            if ("ADMIN".equals(authenticationType)) {

                authenticateAdmin(token, username, request);

            } else {

                authenticateCustomer(token, username, request);

            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateAdmin(
            String token,
            String email,
            HttpServletRequest request
    ) {

        adminRepository.findByEmail(email).ifPresent(admin -> {

            if (!admin.isEnabled()) {
                return;
            }

            if (!jwtService.isTokenValid(token, admin.getEmail())) {
                return;
            }

            var authorities = List.of(
                    new SimpleGrantedAuthority("ADMIN")
            );

            var authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            admin.getEmail(),
                            null,
                            authorities
                    );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authenticationToken);
        });
    }

    private void authenticateCustomer(
            String token,
            String email,
            HttpServletRequest request
    ) {

        var userDetails =
                customUserDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(
                token,
                userDetails.getUsername()
        )) {
            return;
        }

        var authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authenticationToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authenticationToken);
    }
}
