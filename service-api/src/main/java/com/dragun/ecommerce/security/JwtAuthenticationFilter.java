package com.dragun.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return isPublicPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();
        if (jwt.startsWith("\"") && jwt.endsWith("\"") && jwt.length() > 1) {
            jwt = jwt.substring(1, jwt.length() - 1);
        }

        try {
            String username = jwtTokenProvider.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else if (isAdminPath(request.getRequestURI())) {
                    writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token expired or invalid. Please login again.");
                    return;
                }
            }
        } catch (Exception e) {
            log.debug("JWT authentication failed for {}: {}", request.getRequestURI(), e.getMessage());
            if (isAdminPath(request.getRequestURI())) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token. Use the value of data.token from login (Bearer token only).");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    static boolean isPublicPath(String path) {
        return path.startsWith("/api/public/")
                || path.startsWith("/api/dragun/products/")
                || path.startsWith("/api/extend/orders")
                || path.startsWith("/provinces")
                || path.startsWith("/districts/")
                || path.startsWith("/wards/")
                || path.startsWith("/images/")
                || path.equals("/api/dragun/admin/login")
                || path.equals("/api/thiyen/admin/login");
    }

    static boolean isAdminPath(String path) {
        return (path.startsWith("/api/thiyen/admin/") || path.startsWith("/api/dragun/admin/"))
                && !path.endsWith("/login");
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"success\":false,\"message\":\"" + escaped + "\"}");
    }
}
