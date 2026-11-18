package com.dragun.ecommerce.config;

import com.dragun.ecommerce.security.JwtAuthenticationFilter;
import com.dragun.ecommerce.util.Constants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                // Customer-only writes under public prefixes: must come before PUBLIC_ENDPOINTS
                .requestMatchers(HttpMethod.POST, "/api/dragun/products/*/reviews").hasRole("CUSTOMER")
                .requestMatchers("/api/dragun/account/**").hasRole("CUSTOMER")
                // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(Constants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/api/dragun/admin/login").permitAll()
                // Admin endpoints - require authentication
                .requestMatchers(Constants.ADMIN_ENDPOINTS).hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // No or expired token on a protected route: 401 (the storefront then refreshes or shows the login popup).
            // A valid token without the role still gets 403.
            // Both write the JSON body here: going through /error would re-run the chain without the token
            // (JwtAuthenticationFilter runs once per request) and turn every 403 into a 401.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> writeJsonError(response,
                    HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục."))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeJsonError(response,
                    HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.")))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    private static void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"data\":null}");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


