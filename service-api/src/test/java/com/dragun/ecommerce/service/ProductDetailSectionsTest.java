package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.ProductRequest;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Task-006: 9 mục ở trang chi tiết sản phẩm lưu qua API admin sản phẩm, seed V25. */
@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Product detail sections (task-006)")
class ProductDetailSectionsTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private ProductRequest request(Long id) {
        ProductRequest r = new ProductRequest();
        r.setId(id);
        r.setName("Sản phẩm test task-006");
        r.setPrice(new BigDecimal("100000"));
        r.setStock(10);
        r.setCategory("ME_DEN");
        return r;
    }

    @Test
    @DisplayName("tạo rồi sửa: lưu và đọc lại đủ 9 mục qua API public")
    void savesAllSections() {
        ProductRequest create = request(null);
        create.setVideoUrl("  https://www.youtube.com/watch?v=abc123  ");
        create.setSpecifications("Tên sản phẩm: A\nQuy cách: 600g");
        create.setIngredients("Mè đen; Óc chó");
        create.setBenefits("# Nhóm 1\nCông dụng 1");
        create.setTargetUsers("> Chú thích\nNgười lớn");
        create.setHighlights("Ưu điểm 1");
        create.setTechnology("Nghiền mịn");
        create.setStorage("Nơi khô ráo");
        create.setShelfLife("12 tháng");
        ProductResponse created = adminService.saveProduct(create);

        ProductResponse pub = productService.getProductDetails(created.getId());
        assertThat(pub.getVideoUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(pub.getSpecifications()).isEqualTo("Tên sản phẩm: A\nQuy cách: 600g");
        assertThat(pub.getIngredients()).isEqualTo("Mè đen; Óc chó");
        assertThat(pub.getBenefits()).isEqualTo("# Nhóm 1\nCông dụng 1");
        assertThat(pub.getTargetUsers()).isEqualTo("> Chú thích\nNgười lớn");
        assertThat(pub.getHighlights()).isEqualTo("Ưu điểm 1");
        assertThat(pub.getTechnology()).isEqualTo("Nghiền mịn");
        assertThat(pub.getStorage()).isEqualTo("Nơi khô ráo");
        assertThat(pub.getShelfLife()).isEqualTo("12 tháng");

        ProductRequest update = request(created.getId());
        update.setVideoUrl("/video/intro.mp4");
        update.setTargetUsers("Người ăn chay");
        update.setHighlights("Ưu điểm mới");
        update.setShelfLife("8 tháng");
        adminService.saveProduct(update);

        ProductResponse after = productService.getProductDetails(created.getId());
        assertThat(after.getVideoUrl()).isEqualTo("/video/intro.mp4");
        assertThat(after.getTargetUsers()).isEqualTo("Người ăn chay");
        assertThat(after.getHighlights()).isEqualTo("Ưu điểm mới");
        assertThat(after.getShelfLife()).isEqualTo("8 tháng");
        // Mục không gửi khi sửa thì bị xoá (form admin luôn gửi đủ), để trang ẩn mục đó
        assertThat(after.getBenefits()).isNull();
    }

    @Test
    @DisplayName("video để trống thành null (trang ẩn khối video)")
    void blankVideoBecomesNull() {
        ProductRequest r = request(null);
        r.setVideoUrl("   ");
        ProductResponse saved = adminService.saveProduct(r);
        assertThat(productRepository.findById(saved.getId()).orElseThrow().getVideoUrl()).isNull();
    }

    @Test
    @DisplayName("link video sai: 400, không lưu")
    void rejectsBadVideoUrl() {
        long before = productRepository.count();
        for (String bad : List.of("http://youtube.com/watch?v=x", "javascript:alert(1)", "video/intro.mp4",
            "/images/a.png", "https://a.com/x y.mp4", "https://a.com/" + "x".repeat(1000))) {
            ProductRequest r = request(null);
            r.setVideoUrl(bad);
            assertThatThrownBy(() -> adminService.saveProduct(r))
                .as(bad)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                    e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
        assertThat(productRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("seed V25 bằng đúng nội dung trang đang hiển thị")
    void seedMatchesCurrentPage() {
        ProductResponse meDen = productService.getProductById(1L);
        assertThat(meDen.getVideoUrl()).isEqualTo("/video/intro.mp4");
        assertThat(meDen.getSpecifications().split("\n")).containsExactly(
            "Tên sản phẩm: Bột Ngũ Hắc Mè Đen", "Quy cách: Lon thiếc 600g",
            "Hạn sử dụng: 10 tháng kể từ ngày sản xuất", "Xuất xứ: Việt Nam");
        assertThat(meDen.getBenefits().split("\n")).hasSize(5);
        assertThat(meDen.getTargetUsers().split("\n")[0]).isEqualTo("> Phù hợp với người lớn và trẻ em trên 03 tuổi");
        assertThat(meDen.getHighlights().split("\n")[3])
            .isEqualTo("Hàm lượng natri gần như bằng 0, phù hợp với người muốn ăn nhạt, kiểm soát huyết áp.");
        assertThat(meDen.getShelfLife()).isEqualTo("10 tháng từ ngày sản xuất");
        assertThat(meDen.getBenefits()).doesNotContain("\r").doesNotContain("•");

        ProductResponse hongDau = productService.getProductById(3L);
        assertThat(hongDau.getVideoUrl()).isEqualTo("/video/hongdau.mp4");
        assertThat(hongDau.getHighlights().split("\n")[3]).isEqualTo("Giàu sắt, kẽm và phytoestrogen tự nhiên");
        assertThat(hongDau.getShelfLife()).isEqualTo("8 tháng từ ngày sản xuất");

        ProductResponse combo = productService.getProductById(5L);
        List<String> comboBenefits = List.of(combo.getBenefits().split("\n"));
        assertThat(comboBenefits).hasSize(11);
        assertThat(comboBenefits.get(0)).isEqualTo("# 1. BỘT NGŨ HẮC MÈ ĐEN");
        assertThat(comboBenefits.get(6)).isEqualTo("# 2. BỘT NGŨ SẮC HỒNG ĐẬU");
        assertThat(combo.getSpecifications()).isNull();
        assertThat(combo.getIngredients()).isNull();
        assertThat(combo.getHighlights()).isNull();
        assertThat(combo.getShelfLife()).isEqualTo("8 tháng từ ngày sản xuất. Nên dùng sau khi mở nắp trong vòng 4 tuần");
    }
}
