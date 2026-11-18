# Yêu cầu 003: tài khoản khách hàng (đăng ký, đăng nhập, quên và đổi mật khẩu, profile)

Nguồn: owner trao đổi qua chat với agent, 2026-11-18.

## Lời yêu cầu

> Thực hiện phát triển thêm chức năng login/logout chuẩn cho hệ thống để quản lý user đầy đủ, đăng kí, đăng nhập, quên pass, đổi pass, thông tin profile
> username = số điện thoại.
> Viết đánh giá thì hiển thị popup hiển thị màn hình login => login xong mới được đánh giá.
> Chức năng sử dụng 1 popup nổi lên cho user login => login xong update nội dung user vừa login trên màn hình user đang sử dụng để user dễ dùng.
> những login hiện tại không đổi khi mua hàng, ...
> Là 1 senior BA 10 năm kinh nghiệm Hãy đề xuất những thiếu sót về spec thêm màn hình login này.

## Owner trả lời 4 câu chặn (2026-11-18, chat)

1. Q-AUTH-001 (OTP khi đăng ký): **Không cần OTP**.
2. Q-AUTH-002 (kênh OTP): "chưa cần làm otp ver này".
3. Q-AUTH-017 (checkout khi đã đăng nhập): **Tự điền + gắn đơn** (vẫn mua được không cần tài khoản).
4. Q-AUTH-020 (ai được đánh giá): **Mọi tài khoản + nhãn "Đã mua"**.
