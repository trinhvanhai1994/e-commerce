# Yêu cầu 002: quản lý đánh giá sản phẩm

Nguồn: owner trao đổi qua chat với agent, 2026-11-14.

## Lời yêu cầu

> trên màn hình product detail products/{id} có mục đánh giá sản phẩm. Hãy code thêm CRM thêm cho đánh giá
> sản phẩm. thực hiện đánh giá riêng cho từng sản phẩm.

("CRM" được hiểu là trang quản trị nội dung, giống CMS trang chủ/blog đã có.)

## Owner chốt khi được hỏi (4 câu hỏi lựa chọn)

1. Ai tạo đánh giá: **khách gửi, admin duyệt**. Khách gửi qua nút "Viết đánh giá", lưu DB ở trạng thái chờ
   duyệt; admin duyệt/ẩn/sửa/xoá và tự thêm đánh giá. Chỉ đánh giá đã duyệt hiện công khai.
2. 5 đánh giá viết cứng đang hiện cho mọi sản phẩm: **seed vào từng sản phẩm**, trang trông y như cũ; admin
   sửa/xoá riêng từng sản phẩm sau.
3. Ảnh: **có, tối đa 4 ảnh** mỗi đánh giá.
4. Số sao trung bình và số lượng ở đầu trang: **tính từ đánh giá đã duyệt** (bỏ con số cứng "32").

Owner duyệt kế hoạch triển khai ("Làm theo kế hoạch").

## Thay đổi 2026-11-18 (owner, chat với agent)

> sửa lại đánh giá sản phẩm để user viết đánh giá xong được duyệt và hiển thị lên page public luôn,
> Admin sẽ sửa hoặc ẩn sau

Thay mục 1 ở trên: bỏ bước chờ duyệt, đánh giá khách gửi được đăng ngay; admin hậu kiểm (sửa, ẩn).
