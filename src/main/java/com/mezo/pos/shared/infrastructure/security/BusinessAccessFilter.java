package com.mezo.pos.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezo.pos.shared.infrastructure.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class BusinessAccessFilter extends OncePerRequestFilter {

    private static final Pattern BUSINESS_URL_PATTERN =
            Pattern.compile("/api/v1/businesses/([0-9a-fA-F\\-]{36})(/.*)?");

    private final BusinessAccessChecker businessAccessChecker;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        Matcher matcher = BUSINESS_URL_PATTERN.matcher(path);

        if (!matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only check access for sub-resource paths (e.g., /businesses/{id}/products)
        // Don't check for direct business CRUD (/businesses/{id} without sub-path beyond /)
        String subPath = matcher.group(2);
        if (subPath == null || subPath.equals("/") || subPath.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Also skip for /businesses/{id}/status type endpoints (owner-only, handled by @PreAuthorize)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID businessId;
        try {
            businessId = UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException e) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());

        if (!businessAccessChecker.belongsToBusiness(userId, businessId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse errorResponse = ErrorResponse.of(403, "FORBIDDEN", "No tienes acceso a este negocio");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
