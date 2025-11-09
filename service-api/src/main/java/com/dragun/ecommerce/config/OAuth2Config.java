package com.dragun.ecommerce.config;

import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 Configuration
 * This configuration is prepared for future integration with third-party OAuth2 providers
 * (e.g., Google, Facebook, GitHub, etc.)
 * 
 * To enable OAuth2, uncomment and configure the client registration in application.yml:
 * 
 * spring:
 *   security:
 *     oauth2:
 *       client:
 *         registration:
 *           google:
 *             client-id: your-client-id
 *             client-secret: your-client-secret
 *             scope: openid,profile,email
 */
@Configuration
public class OAuth2Config {
    
    // OAuth2 configuration will be added here when needed
    // For now, this is a placeholder for future OAuth2 integration
    
}


