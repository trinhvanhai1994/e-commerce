package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.ProductCategoryRequest;
import com.dragun.ecommerce.model.dto.response.ProductCategoryResponse;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.ProductCategoryRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Category rules on a real PostgreSQL (Flyway V23 seeds ME_DEN, HONG_DAU, COMBO). Each test rolls back. */
@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("ProductCategoryService")
class ProductCategoryServiceTest {

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private static ProductCategoryRequest request(String name, Integer sortOrder, Boolean visible) {
        ProductCategoryRequest r = new ProductCategoryRequest();
        r.setName(name);
        r.setSortOrder(sortOrder);
        r.setVisible(visible);
        return r;
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(expected));
    }

    @Test
    @DisplayName("V23 seeds the three original categories in order")
    void seed_matchesOldHardcodedList() {
        assertThat(categoryService.listPublic()).extracting(ProductCategoryResponse::getCode)
            .containsSubsequence("ME_DEN", "HONG_DAU", "COMBO");
    }

    @Test
    @DisplayName("create generates the code from the Vietnamese name and appends to the end of the list")
    void create_generatesCode() {
        ProductCategoryResponse created = categoryService.create(request("  Trà   thảo mộc Đặc biệt ", null, null), "admin");

        assertThat(created.getCode()).isEqualTo("TRA_THAO_MOC_DAC_BIET");
        assertThat(created.getName()).isEqualTo("Trà thảo mộc Đặc biệt");
        assertThat(created.getVisible()).isTrue();
        assertThat(created.getProductCount()).isZero();
        int maxBefore = categoryService.listAdmin().stream()
            .filter(c -> !c.getId().equals(created.getId()))
            .mapToInt(ProductCategoryResponse::getSortOrder).max().orElse(0);
        assertThat(created.getSortOrder()).isGreaterThan(maxBefore);
    }

    @Test
    @DisplayName("a name folding to an existing code gets a numeric suffix; the same name is a conflict")
    void create_codeCollisionAndDuplicateName() {
        ProductCategoryResponse plain = categoryService.create(request("Me den", null, null), "admin");
        assertThat(plain.getCode()).isEqualTo("ME_DEN_2");

        assertStatus(() -> categoryService.create(request("mè ĐEN", null, null), "admin"), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("blank or symbol-only names are rejected")
    void create_invalidName() {
        assertStatus(() -> categoryService.create(request("   ", null, null), "admin"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> categoryService.create(request("!!!", null, null), "admin"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("update renames, reorders and hides without changing the code; stale updatedAt is 409")
    void update_withOptimisticLock() {
        ProductCategoryResponse created = categoryService.create(request("Ngũ cốc", 50, true), "admin");

        ProductCategoryRequest edit = request("Ngũ cốc dinh dưỡng", 7, false);
        edit.setUpdatedAt(created.getUpdatedAt());
        ProductCategoryResponse updated = categoryService.update(created.getId(), edit, "admin");

        assertThat(updated.getCode()).isEqualTo("NGU_COC");
        assertThat(updated.getName()).isEqualTo("Ngũ cốc dinh dưỡng");
        assertThat(updated.getSortOrder()).isEqualTo(7);
        assertThat(updated.getVisible()).isFalse();

        // Hidden categories stay in the public list (product cards still need their name), flagged invisible
        assertThat(categoryService.listPublic()).filteredOn(c -> c.getCode().equals("NGU_COC"))
            .singleElement().satisfies(c -> {
                assertThat(c.getVisible()).isFalse();
                assertThat(c.getUpdatedAt()).isNull();
                assertThat(c.getProductCount()).isNull();
            });

        ProductCategoryRequest stale = request("Tên khác", null, null);
        stale.setUpdatedAt(created.getUpdatedAt().minusSeconds(5));
        assertStatus(() -> categoryService.update(created.getId(), stale, "admin"), HttpStatus.CONFLICT);

        ProductCategoryRequest missing = request("Tên khác", null, null);
        assertStatus(() -> categoryService.update(created.getId(), missing, "admin"), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("renaming to another category's name is a conflict")
    void update_duplicateName() {
        ProductCategoryResponse created = categoryService.create(request("Hạt điều", null, null), "admin");
        ProductCategoryRequest edit = request("combo", null, null);
        edit.setUpdatedAt(created.getUpdatedAt());
        assertStatus(() -> categoryService.update(created.getId(), edit, "admin"), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("unknown id is 404 for update and delete")
    void notFound() {
        ProductCategoryRequest edit = request("X", null, null);
        edit.setUpdatedAt(OffsetDateTime.now());
        assertStatus(() -> categoryService.update(-1L, edit, "admin"), HttpStatus.NOT_FOUND);
        assertStatus(() -> categoryService.delete(-1L), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("delete is refused while a non-deleted product uses the category, allowed once it is moved")
    void delete_blockedWhileInUse() {
        ProductCategoryResponse created = categoryService.create(request("Quà tặng", null, null), "admin");
        Product product = productRepository.findByDeletedFalse().get(0);
        String original = product.getCategory();
        product.setCategory(created.getCode());
        productRepository.saveAndFlush(product);

        assertThat(categoryService.listAdmin()).filteredOn(c -> c.getId().equals(created.getId()))
            .singleElement().extracting(ProductCategoryResponse::getProductCount).isEqualTo(1L);
        assertStatus(() -> categoryService.delete(created.getId()), HttpStatus.CONFLICT);

        product.setCategory(original);
        productRepository.saveAndFlush(product);
        categoryService.delete(created.getId());

        assertThat(categoryRepository.findById(created.getId())).isEmpty();
        List<String> codes = categoryService.listPublic().stream().map(ProductCategoryResponse::getCode).toList();
        assertThat(codes).doesNotContain(created.getCode());
    }
}
