package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.HomepageSectionRequest;
import com.dragun.ecommerce.model.dto.response.HomepageSectionResponse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Story page (/me) sections 9-11 (ingredients, products, gallery) seeded from cms-seed/page-sections.json
 * by CmsSeedService at startup, then edited through the admin endpoint. Each test rolls back.
 */
@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("ContentService story page")
class ContentServicePageTest {

    @Autowired
    private ContentService contentService;

    private HomepageSectionResponse section(String key) {
        return contentService.getAllSectionsAdmin("story").stream()
            .filter(s -> key.equals(s.getSectionKey()))
            .findFirst()
            .orElseThrow();
    }

    private HomepageSectionRequest request(HomepageSectionResponse current, Map<String, Object> extra) {
        HomepageSectionRequest req = new HomepageSectionRequest();
        req.setEnabled(true);
        req.setSortOrder(current.getSortOrder());
        req.setExtraJson(extra);
        req.setUpdatedAt(current.getUpdatedAt());
        return req;
    }

    private static Map<String, Object> product(Object productId, boolean useDefault, String image) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", "SP");
        m.put("body", "Mô tả");
        m.put("tags", List.of("A", "B"));
        m.put("productId", productId);
        m.put("useDefaultImage", useDefault);
        m.put("image", image);
        return m;
    }

    private static List<Map<String, Object>> gallery(int count) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("alt", "Ảnh " + i);
            m.put("useDefaultImage", false);
            m.put("image", "/images/cms/meal_tile/" + i + ".png");
            items.add(m);
        }
        return items;
    }

    @Test
    @DisplayName("seed tạo sẵn 3 khối mới với nội dung đang hiển thị, public trả về đủ")
    void seedsNewSections() {
        Map<String, HomepageSectionResponse> page = contentService.getPublicPageSections("story");

        assertThat(page).containsKeys("ingredients", "products", "gallery");
        assertThat(page.get("ingredients").getExtraJson()).containsEntry("title", "Thành phần dinh dưỡng");
        assertThat((List<?>) page.get("products").getExtraJson().get("items")).hasSize(2);
        assertThat((List<?>) page.get("gallery").getExtraJson().get("items")).hasSize(4);
    }

    @Test
    @DisplayName("lưu thành phần: chữ mới hiện ở API public")
    void updatesIngredients() {
        HomepageSectionResponse current = section("ingredients");
        Map<String, Object> extra = new HashMap<>(current.getExtraJson());
        extra.put("title", "Thành phần mới");
        extra.put("tabs", List.of("T1", "T2", "T3"));

        HomepageSectionResponse saved = contentService.updateSection("story", "ingredients", request(current, extra), "admin");

        assertThat(saved.getExtraJson()).containsEntry("title", "Thành phần mới");
        assertThat(contentService.getPublicPageSections("story").get("ingredients").getExtraJson())
            .containsEntry("tabs", List.of("T1", "T2", "T3"));
    }

    @Test
    @DisplayName("lưu sản phẩm: ảnh mặc định xoá path, ảnh riêng giữ path")
    void updatesProductsWithImageRule() {
        HomepageSectionResponse current = section("products");
        Map<String, Object> extra = new HashMap<>(current.getExtraJson());
        extra.put("items", List.of(
            product(1, true, "/images/cms/x.png"),
            product(3, false, "/images/cms/product_main/y.png")));

        HomepageSectionResponse saved = contentService.updateSection("story", "products", request(current, extra), "admin");

        List<?> items = (List<?>) saved.getExtraJson().get("items");
        assertThat(((Map<?, ?>) items.get(0)).get("image")).isNull();
        assertThat(((Map<?, ?>) items.get(1)).get("image")).isEqualTo("/images/cms/product_main/y.png");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("quá 4 sản phẩm: 400")
    void rejectsTooManyProducts() {
        HomepageSectionResponse current = section("products");
        Map<String, Object> extra = new HashMap<>(current.getExtraJson());
        extra.put("items", List.of(product(1, true, null), product(2, true, null),
            product(3, true, null), product(4, true, null), product(5, true, null)));

        assertThatThrownBy(() -> contentService.updateSection("story", "products", request(current, extra), "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("ID sản phẩm không phải số nguyên dương: 400")
    void rejectsBadProductId() {
        HomepageSectionResponse current = section("products");
        Map<String, Object> extra = new HashMap<>(current.getExtraJson());
        extra.put("items", List.of(product("abc", true, null)));

        assertThatThrownBy(() -> contentService.updateSection("story", "products", request(current, extra), "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("ID sản phẩm");
    }

    @Test
    @DisplayName("gallery: 12 ảnh được, 13 ảnh 400")
    void galleryLimit() {
        HomepageSectionResponse current = section("gallery");
        Map<String, Object> ok = new HashMap<>(current.getExtraJson());
        ok.put("items", gallery(12));
        HomepageSectionResponse saved = contentService.updateSection("story", "gallery", request(current, ok), "admin");
        assertThat((List<?>) saved.getExtraJson().get("items")).hasSize(12);

        Map<String, Object> tooMany = new HashMap<>(saved.getExtraJson());
        tooMany.put("items", gallery(13));
        assertThatThrownBy(() -> contentService.updateSection("story", "gallery", request(saved, tooMany), "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Gallery tối đa 12");
    }

    @Test
    @DisplayName("gallery: ảnh riêng là URL ngoài: 400")
    void galleryRejectsExternalImage() {
        HomepageSectionResponse current = section("gallery");
        Map<String, Object> extra = new HashMap<>(current.getExtraJson());
        List<Map<String, Object>> items = gallery(1);
        items.get(0).put("image", "https://evil.example/x.png");
        extra.put("items", items);

        assertThatThrownBy(() -> contentService.updateSection("story", "gallery", request(current, extra), "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("/images/");
    }

    @Test
    @DisplayName("updatedAt lệch: 409")
    void staleUpdatedAtConflicts() {
        HomepageSectionResponse current = section("gallery");
        HomepageSectionRequest req = request(current, new HashMap<>(current.getExtraJson()));
        req.setUpdatedAt(OffsetDateTime.parse("2000-01-01T00:00:00Z"));

        assertThatThrownBy(() -> contentService.updateSection("story", "gallery", req, "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("key không thuộc trang story: 400")
    void unknownKeyRejected() {
        HomepageSectionResponse current = section("gallery");
        assertThatThrownBy(() -> contentService.updateSection("story", "meal_gallery", request(current, Map.of()), "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
