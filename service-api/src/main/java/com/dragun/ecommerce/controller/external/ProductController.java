package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductCategoryResponse;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
import com.dragun.ecommerce.service.ProductCategoryService;
import com.dragun.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/dragun/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;

    /** Categories for the storefront filter and product cards (task-004). Literal path, so it wins over /{id}. */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getCategories() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(categoryService.listPublic()));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        // Return all ACTIVE products for users (no limit - for products listing page)
        List<ProductResponse> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        // Return top 4 ACTIVE products for homepage (sorted by priority)
        List<ProductResponse> products = productService.getActiveProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        // Only return ACTIVE products for users
        ProductResponse product = productService.getActiveProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetails(@PathVariable Long id) {
        // Only return ACTIVE products for users
        ProductResponse product = productService.getActiveProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
