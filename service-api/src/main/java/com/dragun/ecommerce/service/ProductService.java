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
        return productRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }
    
    public ProductResponse getProductDetails(Long id) {
        return getProductById(id);
    }
    
    @Transactional
    public ProductResponse createProduct(Product product) {
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
        
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
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
            .build();
    }
}


