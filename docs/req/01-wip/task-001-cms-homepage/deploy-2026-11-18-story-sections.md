# Deploy prod: khối 9-11 trang "Câu chuyện Thi Yên" (2026-11-18)

Thay đổi: admin sửa được 3 khối `ingredients` (Thành phần dinh dưỡng), `products` (Sản phẩm nổi bật), `gallery` (Gallery bữa ăn) của trang `/me`. Chi tiết: `analysis-and-spec.md` mục 7c.

## Có cần chạy SQL không

- **Không có migration Flyway mới.** Schema `homepage_sections` giữ nguyên (đã có `page_key`, unique `(page_key, section_key)` từ V19).
- **Không bắt buộc chạy SQL tay.** Khi service-api mới khởi động, `CmsSeedService` tự chèn 3 dòng còn thiếu từ `cms-seed/page-sections.json` (log: `CMS seed cms-seed/page-sections.json inserted=3`). Dòng đã có thì bỏ qua, không ghi đè.
- Nội dung seed bằng đúng nội dung đang hiển thị, nên deploy xong trang `/me` không đổi tới khi admin lưu.

## Thứ tự deploy

1. Deploy service-api trước. FE cũ vẫn chạy bình thường (khối 9-11 vẫn cứng trong code cũ; 3 dòng mới trong API được FE cũ bỏ qua).
2. Deploy service-ui sau.

## Kiểm tra sau deploy

```sql
SELECT page_key, section_key, sort_order, enabled, updated_by, updated_at
FROM homepage_sections
WHERE page_key = 'story' AND section_key IN ('ingredients', 'products', 'gallery')
ORDER BY sort_order;
```

Kỳ vọng: 3 dòng, `sort_order` 9, 10, 11, `updated_by = 'seed'`. Hoặc gọi `GET /api/dragun/content/pages/story` thấy đủ 3 key.

## SQL dự phòng (chỉ chạy khi câu kiểm tra trên trả thiếu dòng)

Ví dụ log có `CMS seed skipped section ...` hoặc seed bị tắt. Câu lệnh idempotent (`ON CONFLICT DO NOTHING`), chạy lại không ảnh hưởng dữ liệu admin đã sửa. Nội dung sinh từ đúng file seed.

```sql
BEGIN;

INSERT INTO homepage_sections (page_key, section_key, extra_json, enabled, sort_order, use_default_desktop, use_default_mobile, updated_at, updated_by)
VALUES ('story', 'ingredients', $json${
  "title": "Thành phần dinh dưỡng",
  "subtitle": "Khám phá những thành phần tự nhiên và dinh dưỡng trong mỗi sản phẩm Thi Yên",
  "tabs": [
    "THÀNH PHẦN",
    "THÔNG TIN CHUNG",
    "BẢNG CÔNG BỐ"
  ],
  "composition": {
    "heading": "Thành phần tự nhiên",
    "items": [
      {
        "name": "BỘT NGŨ HẮC MÈ ĐEN",
        "body": "Hỗn hợp bột hạt (Yến mạch, Đậu đen, Hạt vừng, Gạo lứt đen, Diêm mạch đen, Óc chó), Erythritol, Prebiotic (Fructo Oligosaccharides), Bột khoai mỡ, Dâu tằm đen, Muối NaCl."
      },
      {
        "name": "BỘT NGŨ SẮC HỒNG ĐẬU",
        "body": "Hỗn hợp bột hạt (Yến mạch, Đậu đỏ, Diêm mạch đỏ, Hạt sen), Táo đỏ, Isomalto oligosaccharide, Fructose, Bột sữa dừa, Chiết xuất củ dền, Konjac gum (INS 425), Muối NaCl."
      }
    ],
    "note": "Có chứa: gluten, các loại hạt (tree nuts)."
  },
  "info": {
    "heading": "Thông tin sản phẩm",
    "groups": [
      {
        "title": "Thông tin cơ bản",
        "items": [
          "Khối lượng tịnh: 600g",
          "Xuất xứ: Việt Nam"
        ]
      },
      {
        "title": "Bảo quản & Sử dụng",
        "items": [
          "Bảo quản: Nơi khô ráo, thoáng mát",
          "Hạn sử dụng: Xem trên bao bì"
        ]
      }
    ]
  },
  "facts": {
    "heading": "Bảng dinh dưỡng",
    "noteLabel": "(**)",
    "note": "Hàm lượng các vitamin và khoáng chất không thấp hơn 80% giá trị ghi trên nhãn.",
    "valueTitle": "Giá trị dinh dưỡng",
    "valueBody": "Giá trị dinh dưỡng trung bình trong 100ml sản phẩm"
  }
}$json$::jsonb, TRUE, 9, TRUE, TRUE, date_trunc('microseconds', NOW()), 'seed')
ON CONFLICT (page_key, section_key) DO NOTHING;

INSERT INTO homepage_sections (page_key, section_key, extra_json, enabled, sort_order, use_default_desktop, use_default_mobile, updated_at, updated_by)
VALUES ('story', 'products', $json${
  "title": "Sản phẩm nổi bật",
  "subtitle": "Khám phá những sản phẩm được yêu thích nhất từ Thi Yên",
  "items": [
    {
      "name": "Bột Ngũ Hắc Mè Đen",
      "body": "Kết hợp 5 loại hạt đen bổ dưỡng: mè đen, gạo lứt đen, đậu đen, diêm mạch đen và dâu tằm đen",
      "tags": [
        "Thuần tự nhiên",
        "Bổ dưỡng"
      ],
      "productId": 1,
      "imageAlt": "Bột Ngũ Hắc Mè Đen - Sản phẩm dinh dưỡng từ 5 loại hạt đen",
      "image": null,
      "useDefaultImage": true
    },
    {
      "name": "Bột Ngũ Sắc Hồng Đậu",
      "body": "Kết hợp 5 nguyên liệu đỏ tự nhiên, giàu dưỡng chất: táo đỏ, củ dền, đậu đỏ, diêm mạch đỏ, hạt sen đỏ",
      "tags": [
        "Bổ khí huyết",
        "Dưỡng nhan"
      ],
      "productId": 3,
      "imageAlt": "Bột Ngũ Sắc Hồng Đậu - Sản phẩm dinh dưỡng từ 5 loại hạt đỏ",
      "image": null,
      "useDefaultImage": true
    }
  ],
  "ctaTitle": "Sẵn sàng bắt đầu hành trình sống lành?",
  "ctaBody": "Khám phá thêm nhiều sản phẩm dinh dưỡng khác từ Thi Yên",
  "ctaButton": "Xem tất cả sản phẩm"
}$json$::jsonb, TRUE, 10, TRUE, TRUE, date_trunc('microseconds', NOW()), 'seed')
ON CONFLICT (page_key, section_key) DO NOTHING;

INSERT INTO homepage_sections (page_key, section_key, extra_json, enabled, sort_order, use_default_desktop, use_default_mobile, updated_at, updated_by)
VALUES ('story', 'gallery', $json${
  "title": "BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG",
  "items": [
    {
      "alt": "Thêm mô tả 1",
      "image": null,
      "useDefaultImage": true
    },
    {
      "alt": "Thêm mô tả 2",
      "image": null,
      "useDefaultImage": true
    },
    {
      "alt": "Thêm mô tả 3",
      "image": null,
      "useDefaultImage": true
    },
    {
      "alt": "Thêm mô tả 4",
      "image": null,
      "useDefaultImage": true
    }
  ]
}$json$::jsonb, TRUE, 11, TRUE, TRUE, date_trunc('microseconds', NOW()), 'seed')
ON CONFLICT (page_key, section_key) DO NOTHING;

COMMIT;
```

## Rollback

- Code cũ không đọc 3 dòng này, để nguyên không ảnh hưởng.
- Nếu muốn xoá (mất nội dung admin đã sửa ở 3 khối):

```sql
DELETE FROM homepage_sections
WHERE page_key = 'story' AND section_key IN ('ingredients', 'products', 'gallery');
```
