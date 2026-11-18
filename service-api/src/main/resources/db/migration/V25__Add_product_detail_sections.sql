-- Task-006: admin sửa 9 mục ở trang chi tiết sản phẩm.
-- Thêm 4 cột mới; 5 cột có sẵn (specifications, ingredients, benefits, technology, storage) được dùng lại.
-- Mỗi giá trị là văn bản nhiều dòng: dòng "# " là tiêu đề con, dòng "> " là chú thích, còn lại mỗi dòng một mục.
ALTER TABLE products ADD COLUMN IF NOT EXISTS video_url VARCHAR(1000);
ALTER TABLE products ADD COLUMN IF NOT EXISTS target_users TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS highlights TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS shelf_life TEXT;

-- V4 chèn sản phẩm với id cố định mà không đẩy sequence, nên admin thêm sản phẩm mới trên DB mới bị trùng khoá.
-- Chỉ đẩy sequence lên tới id lớn nhất, không bao giờ kéo lùi.
SELECT setval(pg_get_serial_sequence('products', 'id'),
              GREATEST((SELECT COALESCE(MAX(id), 1) FROM products), (SELECT last_value FROM products_id_seq)),
              true);

-- Seed = đúng nội dung ProductDetail.vue đang viết cứng trước thay đổi (owner chốt ghi đè dữ liệu cũ).
-- Id 5 nhận nội dung trước đây viết cho id 52 (combo đã đổi id ở V5).

-- Bột Ngũ Hắc Mè Đen (id 1, 2)
UPDATE products SET
  video_url = '/video/intro.mp4',
  specifications = concat_ws(E'\n',
    'Tên sản phẩm: Bột Ngũ Hắc Mè Đen',
    'Quy cách: Lon thiếc 600g',
    'Hạn sử dụng: 10 tháng kể từ ngày sản xuất',
    'Xuất xứ: Việt Nam'),
  ingredients = 'Yến mạch; Gạo lứt đen; Đậu đỏ có vỏ; Diêm mạch đen; Mè đen; Dâu tằm đen; Đậu đen có vỏ (trắng lòng); Óc chó; Erythritol; FOS, Khoai mỡ.',
  benefits = concat_ws(E'\n',
    'Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo',
    'Dinh dưỡng đen cho tóc chắc khỏe, đen mượt',
    'Bổi bổ sức khoẻ, nuôi dưỡng khí huyết: công thức ngũ hắc dưỡng sinh kết hợp đa dạng nhóm thực phẩm từ hạt, ngũ cốc và quả, cung cấp 4 nhóm dưỡng chất thiết yếu, nuôi dưỡng cơ thể và khí huyết mỗi ngày.',
    'Dinh dưỡng cho giấc ngủ: 5 nguyên liệu đen có tính bình kết hợp cùng hạt óc chó giàu dưỡng chất nuôi dưỡng giấc ngủ',
    'Giàu chất xơ tự nhiên từ thực vật, giúp duy trì chế độ ăn lành mạnh.'),
  target_users = concat_ws(E'\n',
    '> Phù hợp với người lớn và trẻ em trên 03 tuổi',
    'Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng',
    'Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu',
    'Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe',
    'Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng',
    'Phù hợp với người thức khuya mất ngủ làm việc quá sức'),
  highlights = concat_ws(E'\n',
    'Bữa sáng nhanh, dinh dưỡng, nhẹ bụng, chỉ 1 phút pha chế',
    '5 Không: Không chất bảo quản - Không axit béo chuyển hoá - Không dinh dưỡng nhân tạo - Không chất tạo màu - Không đậu nành',
    'Ít calo, giàu dinh dưỡng: Chỉ 105 kcal/ khẩu phần 30(g) < một quả táo (~150 kcal), nhưng vẫn cung cấp đủ protein, chất xơ và chất béo tốt giúp no lâu.',
    'Hàm lượng natri gần như bằng 0, phù hợp với người muốn ăn nhạt, kiểm soát huyết áp.'),
  technology = 'Nghiền bảo tồn siêu mịn giúp tối ưu hàm lượng dinh dưỡng và giữ nguyên hương vị nguyên bản, không lợn cợn, dễ uống.',
  storage = 'Bảo quản ở nơi thoáng mát, tránh ánh nắng trực tiếp chiếu vào sản phẩm. Sản phẩm đã mở nắp cần sử dụng trong vòng 4 tuần.',
  shelf_life = '10 tháng từ ngày sản xuất'
WHERE id IN (1, 2);

-- Bột Ngũ Sắc Hồng Đậu (id 3, 4)
UPDATE products SET
  video_url = '/video/hongdau.mp4',
  specifications = concat_ws(E'\n',
    'Tên sản phẩm: Bột Ngũ Sắc Hồng Đậu',
    'Quy cách: Lon thiếc 600g',
    'Hạn sử dụng: 08 tháng kể từ ngày sản xuất',
    'Xuất xứ: Việt Nam'),
  ingredients = 'Đậu đỏ nhỏ có vỏ, Hạt sen đỏ Củ dền đỏ, Diêm mạch đỏ, Yến mạch, Táo đỏ khô, Konjac Gum, Bột sữa dừa, Nacl, Fructose, Isomalto Oligosaccharide.',
  benefits = concat_ws(E'\n',
    'Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo, phù hợp cho chế độ ăn lành mạnh.',
    'Nuôi dưỡng khí huyết: sự kết hợp của táo đỏ, củ dền, đậu đỏ, diêm mạch đỏ, hạt sen đỏ tạo nên nguồn dưỡng chất nuôi dưỡng cơ thể, khí huyết mỗi ngày.',
    'Dưỡng nhan thuần thực vật: nuôi dưỡng làn da hồng hào, nhuận sắc, rạng rỡ từ bên trong.',
    'Dinh dưỡng cho giấc ngủ: Hạt sen đỏ có tính bình, dưỡng tâm, khi kết hợp hài hòa cùng các nguyên liệu khác tạo nên thức uống ấm bụng, vị dịu nhẹ, giúp cơ thể thư thái, ngủ ngon hơn.'),
  target_users = concat_ws(E'\n',
    '> Phù hợp với người lớn và trẻ em trên 03 tuổi',
    'Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng',
    'Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu',
    'Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe',
    'Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng',
    'Phù hợp với người thức khuya mất ngủ làm việc quá sức'),
  highlights = concat_ws(E'\n',
    'Bữa sáng nhanh, dinh dưỡng, nhẹ bụng, chỉ 1 phút pha chế',
    '5 Không: Không chất bảo quản - Không axit béo chuyển hoá - Không dinh dưỡng nhân tạo - Không chất tạo màu - Không đậu nành',
    'Ít calo, giàu dinh dưỡng: Chỉ 105 kcal/ khẩu phần 30(g) < một quả táo (~150 kcal), nhưng vẫn cung cấp đủ protein, chất xơ và chất béo tốt giúp no lâu.',
    'Giàu sắt, kẽm và phytoestrogen tự nhiên'),
  technology = 'Nghiền bảo tồn siêu mịn giúp tối ưu hàm lượng dinh dưỡng và giữ nguyên hương vị nguyên bản, không lợn cợn, dễ uống.',
  storage = 'Bảo quản ở nơi thoáng mát, tránh ánh nắng trực tiếp chiếu vào sản phẩm. Sản phẩm đã mở nắp cần sử dụng trong vòng 4 tuần.',
  shelf_life = '8 tháng từ ngày sản xuất'
WHERE id IN (3, 4);

-- Combo (id 5, trước là 52): không có THÔNG TIN SẢN PHẨM, THÀNH PHẦN, ƯU ĐIỂM NỔI BẬT riêng
UPDATE products SET
  video_url = '/video/intro.mp4',
  specifications = NULL,
  ingredients = NULL,
  benefits = concat_ws(E'\n',
    '# 1. BỘT NGŨ HẮC MÈ ĐEN',
    'Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo',
    'Dinh dưỡng cho tóc chắc khỏe, đen mượt từ 5 loại hạt đen giàu dưỡng chất',
    'Bổ huyết, bồi bổ sức khỏe: kết hợp đa dạng nhóm thực phẩm từ hạt, ngũ cốc và quả, cung cấp 4 nhóm dưỡng chất thiết yếu, hỗ trợ tuần hoàn máu, bổ sung dưỡng chất cần thiết giúp cơ thể duy trì thể trạng',
    'Dinh dưỡng cho giấc ngủ, giúp cơ thể thư giãn, dễ đi vào giấc ngủ, ngủ ngon và ngủ sâu hơn.',
    'Giàu chất xơ tự nhiên từ thực vật, giúp duy trì chế độ ăn lành mạnh.',
    '# 2. BỘT NGŨ SẮC HỒNG ĐẬU',
    'Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo, phù hợp cho chế độ ăn lành mạnh.',
    'Nuôi dưỡng khí huyết: sự kết hợp của táo đỏ, củ dền, đậu đỏ, diêm mạch đỏ, hạt sen đỏ tạo nên nguồn dưỡng chất nuôi dưỡng cơ thể, khí huyết mỗi ngày.',
    'Dưỡng nhan thuần thực vật: nuôi dưỡng làn da hồng hào, nhuận sắc, rạng rỡ từ bên trong.',
    'Dinh dưỡng cho giấc ngủ: Hạt sen đỏ có tính bình, dưỡng tâm, khi kết hợp hài hòa cùng các nguyên liệu khác tạo nên thức uống ấm bụng, vị dịu nhẹ, giúp cơ thể thư thái, ngủ ngon hơn.'),
  target_users = concat_ws(E'\n',
    '> Phù hợp với người lớn và trẻ em trên 03 tuổi',
    'Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng',
    'Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu',
    'Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe',
    'Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng',
    'Phù hợp với người thức khuya mất ngủ làm việc quá sức'),
  highlights = NULL,
  technology = 'Nghiền bảo tồn siêu mịn giúp tối ưu hàm lượng dinh dưỡng và giữ nguyên hương vị nguyên bản, không lợn cợn, dễ uống.',
  storage = 'Bảo quản ở nơi thoáng mát, tránh ánh nắng trực tiếp chiếu vào sản phẩm. Sản phẩm đã mở nắp cần sử dụng trong vòng 4 tuần.',
  shelf_life = '8 tháng từ ngày sản xuất. Nên dùng sau khi mở nắp trong vòng 4 tuần'
WHERE id = 5;
