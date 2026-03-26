package com.uktc.schoolInventory.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class SessionUserFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SessionUserFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        logger.info("SessionUserFilter: path={} sessionId={}", path, (request.getSession(false) != null ? request.getSession(false).getId() : "none"));
        // Allow unauthenticated access to login and register endpoints
        if (path.equals("/auth/register") || path.equals("/auth/login")) {
            logger.info("Allowing unauthenticated access to {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        // Allow if authenticated by Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication: {}", authentication);
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            logger.info("Allowing access for authenticated user: {}", authentication.getPrincipal());
            filterChain.doFilter(request, response);
            return;
        }

        // Also check for manual session attributes set by AuthController
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
                logger.info("Allowing access for user with session attribute: {}", userEmail);
                filterChain.doFilter(request, response);
                return;
            }
        }

        logger.warn("Blocking request: not authenticated");
        // Otherwise, block
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No logged user in session");
    }
}
