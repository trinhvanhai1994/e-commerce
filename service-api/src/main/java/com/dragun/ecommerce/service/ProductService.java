package com.dragun.ecommerce.service;

import com.dragun.ecommerce.exception.ResourceNotFoundException;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<ProductResponse> getAllProducts() {
        // For admin: return all non-deleted products (including INACTIVE)
        // Sort by priority ASC (lower priority number = higher priority, shown first)
        return productRepository.findByDeletedFalse().stream()
            .sorted((p1, p2) -> {
                int priority1 = p1.getPriority() != null ? p1.getPriority() : 999;
                int priority2 = p2.getPriority() != null ? p2.getPriority() : 999;
                return Integer.compare(priority1, priority2);
            })
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getActiveProducts() {
        // For homepage: return only ACTIVE and non-deleted products
        // Sort by priority ASC (lower priority number = higher priority, shown first)
        // Limit to top 4 products for homepage
        return productRepository.findByDeletedFalseAndStatus("ACTIVE").stream()
            .sorted((p1, p2) -> {
                int priority1 = p1.getPriority() != null ? p1.getPriority() : 999;
                int priority2 = p2.getPriority() != null ? p2.getPriority() : 999;
                return Integer.compare(priority1, priority2);
            })
            .limit(4)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getAllActiveProducts() {
        // For products page: return all ACTIVE and non-deleted products (no limit)
        // Sort by priority ASC (lower priority number = higher priority, shown first)
        return productRepository.findByDeletedFalseAndStatus("ACTIVE").stream()
            .sorted((p1, p2) -> {
                int priority1 = p1.getPriority() != null ? p1.getPriority() : 999;
                int priority2 = p2.getPriority() != null ? p2.getPriority() : 999;
                return Integer.compare(priority1, priority2);
            })
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Check if product is deleted
        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        return mapToResponse(product);
    }
    
    public ProductResponse getActiveProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // For users: only return ACTIVE and non-deleted products
        if (Boolean.TRUE.equals(product.getDeleted()) || !"ACTIVE".equals(product.getStatus())) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        return mapToResponse(product);
    }
    
    public ProductResponse getProductDetails(Long id) {
        return getActiveProductById(id);
    }
    
    @Transactional
    public ProductResponse createProduct(Product product) {
        // Ensure default values for new products
        if (product.getDeleted() == null) {
            product.setDeleted(false);
        }
        if (product.getStatus() == null || product.getStatus().isEmpty()) {
            product.setStatus("ACTIVE");
        }
        if (product.getPriority() == null) {
            product.setPriority(999);
        }
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, Product product) {
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Update fields
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setOldPrice(product.getOldPrice());
        existingProduct.setShortDesc(product.getShortDesc());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setMainImage(product.getMainImage());
        existingProduct.setGallery(product.getGallery());
        existingProduct.setStock(product.getStock());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setBenefits(product.getBenefits());
        existingProduct.setIngredients(product.getIngredients());
        existingProduct.setSpecifications(product.getSpecifications());
        existingProduct.setTechnology(product.getTechnology());
        existingProduct.setStorage(product.getStorage());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setRating(product.getRating());
        existingProduct.setReviewCount(product.getReviewCount());
        // Update status if provided
        if (product.getStatus() != null) {
            existingProduct.setStatus(product.getStatus());
        }
        // Update priority if provided
        if (product.getPriority() != null) {
            existingProduct.setPriority(product.getPriority());
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Soft delete: set deleted flag to true
        product.setDeleted(true);
        productRepository.save(product);
    }
    
    @Transactional
    public ProductResponse toggleStatus(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Toggle between ACTIVE and INACTIVE
        if ("ACTIVE".equals(product.getStatus())) {
            product.setStatus("INACTIVE");
        } else {
            product.setStatus("ACTIVE");
        }
        
        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }
    
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .oldPrice(product.getOldPrice())
            .shortDesc(product.getShortDesc())
            .description(product.getDescription())
            .mainImage(product.getMainImage())
            .gallery(product.getGallery())
            .stock(product.getStock())
            .category(product.getCategory())
            .quantity(product.getQuantity())
            .benefits(product.getBenefits())
            .ingredients(product.getIngredients())
            .specifications(product.getSpecifications())
            .technology(product.getTechnology())
            .storage(product.getStorage())
            .discount(product.getDiscount())
            .rating(product.getRating())
            .reviewCount(product.getReviewCount())
            .deleted(product.getDeleted())
            .status(product.getStatus())
            .priority(product.getPriority())
            .build();
    }
}


