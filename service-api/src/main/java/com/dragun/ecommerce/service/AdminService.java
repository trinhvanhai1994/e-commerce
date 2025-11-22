package com.dragun.ecommerce.service;

import com.dragun.ecommerce.exception.UnauthorizedException;
import com.dragun.ecommerce.integration.pancake.service.PancakeProductSyncService;
import com.dragun.ecommerce.model.dto.request.LoginRequest;
import com.dragun.ecommerce.model.dto.request.ProductRequest;
import com.dragun.ecommerce.model.dto.response.LoginResponse;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.entity.User;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final PancakeProductSyncService pancakeProductSyncService;
    
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
        ProductResponse response;
        
        if (request.getId() != null) {
            // Update existing product
            product.setId(request.getId());
            response = productService.updateProduct(request.getId(), product);
        } else {
            // Create new product
            response = productService.createProduct(product);
        }
        
        // Sync to Pancake POS if requested
        if (Boolean.TRUE.equals(request.getSyncToPancake())) {
            try {
                // Get the saved product entity
                Product savedProduct = productRepository.findById(response.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found after save"));
                
                // Sync to Pancake
                pancakeProductSyncService.syncProductToPancake(savedProduct);
                log.info("Product {} synced to Pancake POS successfully", response.getId());
            } catch (Exception e) {
                log.error("Failed to sync product {} to Pancake POS: {}", response.getId(), e.getMessage(), e);
                // Don't throw exception - product is already saved, just log the error
                // The frontend can check the response for sync status
            }
        }
        
        return response;
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        productService.deleteProduct(id);
    }
    
    @Transactional
    public ProductResponse toggleProductStatus(Long id) {
        return productService.toggleStatus(id);
    }
    
    private Product mapToEntity(ProductRequest request) {
        Product.ProductBuilder builder = Product.builder()
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
            .reviewCount(request.getReviewCount());
        
        // Set status if provided, otherwise default to ACTIVE
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            builder.status(request.getStatus());
        } else {
            builder.status("ACTIVE");
        }
        
        // Set priority if provided, otherwise default to 999
        if (request.getPriority() != null) {
            builder.priority(request.getPriority());
        } else {
            builder.priority(999);
        }
        
        return builder.build();
    }
}

