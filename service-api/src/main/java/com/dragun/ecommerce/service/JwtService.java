package com.dragun.ecommerce.service;

import com.dragun.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public String generateToken(UserDetails userDetails) {
        return jwtTokenProvider.generateToken(userDetails);
    }
    
    public String extractUsername(String token) {
        return jwtTokenProvider.extractUsername(token);
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        return jwtTokenProvider.validateToken(token, userDetails);
    }
}


