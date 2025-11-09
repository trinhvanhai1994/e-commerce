package com.dragun.ecommerce.service;

import com.dragun.ecommerce.exception.UnauthorizedException;
import com.dragun.ecommerce.model.dto.request.LoginRequest;
import com.dragun.ecommerce.model.dto.request.ProductRequest;
import com.dragun.ecommerce.model.dto.response.LoginResponse;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.entity.User;
import com.dragun.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ProductService productService;
    
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Sai tài khoản hoặc mật khẩu!");
        }
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        UserDetails userDetails = user;
        
        String token = jwtService.generateToken(userDetails);
        
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
            .id(user.getId())
            .name(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
        
        return LoginResponse.builder()
            .token(token)
            .user(userInfo)
            .build();
    }
    
    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Product product = mapToEntity(request);
        
        if (request.getId() != null) {
            // Update existing product
            product.setId(request.getId());
            return productService.updateProduct(request.getId(), product);
        } else {
            // Create new product
            return productService.createProduct(product);
        }
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        productService.deleteProduct(id);
    }
    
    private Product mapToEntity(ProductRequest request) {
        return Product.builder()
            .name(request.getName())
            .price(request.getPrice())
            .oldPrice(request.getOldPrice())
            .shortDesc(request.getShortDesc())
            .description(request.getDescription())
            .mainImage(request.getMainImage())
            .gallery(request.getGallery())
            .stock(request.getStock())
            .category(request.getCategory())
            .quantity(request.getQuantity())
            .benefits(request.getBenefits())
            .ingredients(request.getIngredients())
            .specifications(request.getSpecifications())
            .technology(request.getTechnology())
            .storage(request.getStorage())
            .discount(request.getDiscount())
            .rating(request.getRating())
            .reviewCount(request.getReviewCount())
            .build();
    }
}

