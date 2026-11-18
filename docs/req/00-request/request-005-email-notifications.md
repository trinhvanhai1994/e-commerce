# Yêu cầu 005: gửi email thông báo đơn hàng

Nguồn: owner trao đổi qua chat với agent, 2026-11-18. Owner đưa code mẫu của dự án khác
(`dragun-cloud`, `vn.co.cake.helper.EmailService`, gửi qua Brevo REST API) và nhờ phân tích để áp dụng.

## Lời yêu cầu

> Hãy phân tích luồng xử lý và hoạt động code gửi email mẫu để lên plan áp dụng cho dự án này

Agent đề xuất 3 loại mail (báo admin có đơn mới, xác nhận đơn cho khách, quên mật khẩu qua email)
và hỏi giai đoạn 1 làm loại nào. Owner chọn:

> Admin + xác nhận đơn
