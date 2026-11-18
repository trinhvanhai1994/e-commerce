package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.request.ProductCategoryRequest;
import com.dragun.ecommerce.model.dto.response.ProductCategoryResponse;
import com.dragun.ecommerce.model.entity.ProductCategory;
import com.dragun.ecommerce.repository.ProductCategoryRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Admin-managed product categories (task-004). Products store the category {@code code}; the code is generated
 * from the name on create and never changes, so renaming a category does not touch any product.
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    static final int CODE_MAX_LENGTH = 50;
    static final int NAME_MAX_LENGTH = 100;

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /** Public list: every category, hidden ones too, so product cards can still show their name. */
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> listPublic() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .map(c -> toResponse(c, null, false))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> listAdmin() {
        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : productRepository.countActiveByCategory()) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .map(c -> toResponse(c, counts.getOrDefault(c.getCode(), 0L), true))
            .toList();
    }

    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request, String username) {
        String name = normalizeName(request.getName());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại");
        }
        String base = generateCode(name);
        String code = base;
        // Two different names can fold to the same code ("Mè đen" and "Me den"): add a numeric suffix
        for (int i = 2; categoryRepository.existsByCode(code); i++) {
            String suffix = "_" + i;
            code = base.substring(0, Math.min(base.length(), CODE_MAX_LENGTH - suffix.length())) + suffix;
        }
        ProductCategory category = ProductCategory.builder()
            .code(code)
            .name(name)
            .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : nextSortOrder())
            .visible(request.getVisible() == null || request.getVisible())
            .updatedBy(username)
            .build();
        return toResponse(categoryRepository.saveAndFlush(category), 0L, true);
    }

    @Transactional
    public ProductCategoryResponse update(Long id, ProductCategoryRequest request, String username) {
        ProductCategory category = findOrThrow(id);
        if (request.getUpdatedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu updatedAt. Hãy tải lại danh mục rồi lưu lại.");
        }
        if (!category.getUpdatedAt().toInstant().equals(request.getUpdatedAt().toInstant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Danh mục đã được cập nhật bởi người khác. Hãy tải lại.");
        }
        String name = normalizeName(request.getName());
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên danh mục đã tồn tại");
        }
        category.setName(name);
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getVisible() != null) {
            category.setVisible(request.getVisible());
        }
        category.setUpdatedBy(username);
        ProductCategory saved = categoryRepository.saveAndFlush(category);
        return toResponse(saved, productRepository.countByCategoryAndDeletedFalse(saved.getCode()), true);
    }

    /** Refused while any non-deleted product still uses the category (owner decision, request-004). */
    @Transactional
    public void delete(Long id) {
        ProductCategory category = findOrThrow(id);
        long inUse = productRepository.countByCategoryAndDeletedFalse(category.getCode());
        if (inUse > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Danh mục đang có " + inUse + " sản phẩm. Hãy chuyển các sản phẩm này sang danh mục khác trước khi xoá.");
        }
        categoryRepository.delete(category);
    }

    /** "Trà thảo mộc" becomes TRA_THAO_MOC: Vietnamese marks removed, đ to D, other characters to underscore. */
    static String generateCode(String name) {
        String folded = Normalizer.normalize(name.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
        String code = folded.toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        if (code.length() > CODE_MAX_LENGTH) {
            code = code.substring(0, CODE_MAX_LENGTH).replaceAll("_+$", "");
        }
        if (code.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên danh mục phải có ít nhất một chữ cái hoặc chữ số");
        }
        return code;
    }

    private String normalizeName(String raw) {
        String name = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên danh mục không được để trống");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên danh mục tối đa 100 ký tự");
        }
        return name;
    }

    private int nextSortOrder() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .mapToInt(ProductCategory::getSortOrder)
            .max()
            .orElse(0) + 1;
    }

    private ProductCategory findOrThrow(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));
    }

    private ProductCategoryResponse toResponse(ProductCategory c, Long productCount, boolean admin) {
        return ProductCategoryResponse.builder()
            .id(c.getId())
            .code(c.getCode())
            .name(c.getName())
            .sortOrder(c.getSortOrder())
            .visible(c.getVisible())
            .productCount(productCount)
            .updatedAt(admin ? c.getUpdatedAt() : null)
            .build();
    }
}
