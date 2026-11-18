---
type: task-wip
title: "Tài khoản khách hàng: đăng ký, đăng nhập popup, quên và đổi mật khẩu, profile"
status: doing
updated: 2026-11-18
sources:
  - ../../00-request/request-003-customer-accounts.md
---

# Tài khoản khách hàng: đăng ký, đăng nhập popup, quên và đổi mật khẩu, profile

> Tài liệu làm việc (01-wip). Mục 2 là điều owner đã nói. Mục 3 là thiếu sót của spec, mỗi mục có câu hỏi
> `Q-AUTH-xxx` và phương án mặc định `[PROPOSED]`. Mục 4 là thiết kế nháp `[PROPOSED]`, chỉ code sau khi owner
> trả lời các câu chặn ở mục 3 và duyệt mục 4. Khi chốt, tách sang `03-business`, `04-system/srs.md`,
> `05-design/{apis,screens,data-model,permissions}` qua đúng skill sở hữu.

## 1. Hiện trạng trước thay đổi

- Không có tài khoản khách. Bảng `users` (V1) chỉ dùng cho admin: `username` unique, `email NOT NULL`,
  `role` mặc định `ROLE_USER`, seed một tài khoản `admin` (`V6__Fix_admin_password.sql`).
- Trang `AdminUsers.vue` dựng "danh sách khách" từ đơn hàng, không từ bảng `users` (`docs/ai/10-business.md`).
- Đăng nhập duy nhất là admin: `POST /api/dragun/admin/login` (`AdminAuthController`), JWT stateless, hạn
  mặc định 24 giờ (`jwt.expiration`), không có refresh token, đăng xuất chỉ xoá token ở trình duyệt
  (`admin.service.js`). Mật khẩu băm BCrypt (`SecurityConfig.passwordEncoder`).
- FE lưu token ở `localStorage['authToken']` và **http client gắn token này vào mọi request**
  (`services/http/adapters/*.adapter.js`, `content.service.js`).
- Đặt hàng không cần đăng nhập: khách nhập tên, SĐT, địa chỉ ở `Checkout.vue`; đơn lưu `customer_phone`,
  không gắn tài khoản. `GET /api/extend/orders/customer/{phone}` là public: ai biết SĐT là xem được đơn.
- Đánh giá sản phẩm: khách vãng lai gửi, đăng ngay (task-002), tên tự nhập, chống spam bằng giới hạn
  5 lần / 10 phút / IP và honeypot.
- Hệ thống chưa có kênh gửi SMS, email hay OTP nào (không có thư viện mail, SMS, captcha trong `pom.xml`).
- Route `/me` đã dùng cho trang "Câu chuyện Thi Yên", không dùng lại cho trang tài khoản.

## 2. Điều owner đã nói (nguồn: request-003)

| # | Nội dung |
|---|---|
| R1 | Có đủ: đăng ký, đăng nhập, đăng xuất, quên mật khẩu, đổi mật khẩu, thông tin profile |
| R2 | Tên đăng nhập là số điện thoại |
| R3 | Bấm "Viết đánh giá" khi chưa đăng nhập thì hiện popup đăng nhập; đăng nhập xong mới được đánh giá |
| R4 | Đăng nhập bằng popup nổi trên trang đang xem; xong thì cập nhật thông tin người dùng ngay trên trang đó (không chuyển trang) |
| R5 | "những login hiện tại không đổi khi mua hàng". `[INFERRED]` Hiểu là: mua hàng vẫn không bắt buộc đăng nhập, luồng checkout và đăng nhập admin giữ nguyên. Xem Q-AUTH-017 |
| R6 | Q-AUTH-001, Q-AUTH-002: **không có OTP ở phiên bản này**. Đăng ký bằng SĐT + mật khẩu, không xác minh chủ SĐT (owner trả lời 2026-11-18) |
| R7 | Q-AUTH-017: vẫn mua được không cần tài khoản; đã đăng nhập thì checkout tự điền tên, SĐT, địa chỉ và đơn gắn vào tài khoản (owner trả lời 2026-11-18) |
| R8 | Q-AUTH-020: mọi tài khoản đã đăng nhập đều được đánh giá; đánh giá của người đã mua có nhãn "Đã mua hàng" (owner trả lời 2026-11-18) |

### Hệ quả của R6 (không OTP) lên các mục khác `[PROPOSED]`

- **Quên mật khẩu** (R1) không tự làm được: khách bấm "Quên mật khẩu" thì popup hiện hướng dẫn gọi hotline.
  Admin có nút "Đặt lại mật khẩu" cho tài khoản, hệ thống sinh mật khẩu tạm, admin báo cho khách qua điện
  thoại; lần đăng nhập kế tiếp bắt buộc đổi mật khẩu. Admin phải tự xác minh người gọi (ví dụ hỏi mã đơn gần nhất).
- **Q-AUTH-007**: không tự liên kết đơn cũ của khách vãng lai theo SĐT, vì chưa chứng minh được chủ SĐT; nếu
  liên kết thì ai đăng ký bằng SĐT người khác cũng xem được lịch sử đơn và địa chỉ của họ. "Đơn hàng của tôi"
  chỉ gồm đơn đặt khi đã đăng nhập.
- **R8, nhãn "Đã mua hàng"**: chỉ tính đơn đặt khi đã đăng nhập (gắn `customer_id`), trạng thái `DELIVERED`,
  có sản phẩm đó. Không so khớp theo SĐT, vì SĐT chưa xác minh nên dễ giả.
- **Q-AUTH-013** (quy tắc OTP): không áp dụng ở phiên bản này.
- Chống tạo tài khoản hàng loạt bằng SĐT người khác: giới hạn số lần đăng ký theo IP (ví dụ 5 lần / giờ) và
  honeypot như form đánh giá.
- Khi bổ sung OTP ở phiên bản sau: tài khoản cũ cần xác minh lại SĐT ở lần đăng nhập đầu tiên sau đó, rồi mới
  liên kết đơn cũ.

## 3. Thiếu sót của spec (góc nhìn BA)

Mức độ: **Chặn** = phải có câu trả lời trước khi code, làm sai phải làm lại. **Quan trọng** = cần chốt trước
khi xong, có mặc định tạm. **Nên có** = có thể để giai đoạn sau.

### 3.1 Danh tính và đăng ký

- **Q-AUTH-001 (Chặn): Có xác minh số điện thoại bằng OTP khi đăng ký không?**
  Không xác minh thì ai cũng đăng ký được bằng SĐT của người khác, rồi nhìn thấy hoặc nhận vơ đơn hàng và
  đánh giá gắn với SĐT đó. Quên mật khẩu cũng không có cách nào chứng minh chủ SĐT.
  `[PROPOSED]` Bắt buộc OTP khi đăng ký và khi quên mật khẩu.
  **Đã chốt: không OTP ở phiên bản này (R6).**
- **Q-AUTH-002 (Chặn): Gửi OTP qua kênh nào, ai trả phí?**
  Lựa chọn: SMS brandname (cần đăng ký brandname với nhà mạng, mất vài tuần, tính phí từng tin), Zalo ZNS
  (rẻ hơn, cần Zalo OA đã xác thực và mẫu tin được duyệt), Firebase Phone Auth (miễn phí hạn mức thấp,
  phụ thuộc Google). Đây là việc thủ tục và chi phí của owner, quyết định tiến độ của quên mật khẩu.
  `[PROPOSED]` Code qua một interface gửi OTP; môi trường dev ghi OTP ra log; chọn nhà cung cấp thật sau.
  Nếu chưa có nhà cung cấp lúc go-live thì quên mật khẩu tạm làm qua liên hệ hotline (admin đặt lại).
  **Đã chốt: chưa làm OTP ở phiên bản này (R6).**
- **Q-AUTH-003 (Chặn): Lưu tài khoản khách ở đâu?**
  Bảng `users` hiện là của admin, `email NOT NULL`, và username `admin` không phải SĐT. Dùng chung dễ lẫn
  quyền (một lỗi phân quyền là khách thành admin).
  `[PROPOSED]` Bảng riêng `customers`; token khách mang claim loại tài khoản, không bao giờ có `ROLE_ADMIN`.
- **Q-AUTH-004 (Quan trọng): Chuẩn hoá SĐT thế nào?**
  `0912...`, `+84912...`, `84912...`, có dấu cách là cùng một số. Không chuẩn hoá thì một người có nhiều
  tài khoản. `[PROPOSED]` Lưu dạng `0` + 9 số, chấp nhận nhập `+84`/`84`, bỏ dấu cách và dấu chấm; chỉ nhận
  đầu số di động Việt Nam.
- **Q-AUTH-005 (Quan trọng): Đăng ký cần những trường nào?**
  `[PROPOSED]` Bắt buộc: SĐT, họ tên, mật khẩu, nhập lại mật khẩu, tick đồng ý Điều khoản và Chính sách bảo
  mật. Tuỳ chọn: email. Không hỏi ngày sinh, giới tính lúc đăng ký (để ở profile).
- **Q-AUTH-006 (Quan trọng): Chính sách mật khẩu?**
  `[PROPOSED]` 8 đến 64 ký tự, có cả chữ và số, không trùng SĐT; có nút hiện/ẩn mật khẩu.
- **Q-AUTH-007 (Quan trọng): SĐT đã từng đặt hàng (khách vãng lai) khi đăng ký thì sao?**
  `[PROPOSED]` Sau khi xác minh OTP, các đơn cũ có cùng SĐT tự hiện trong "Đơn hàng của tôi". Không xác minh
  OTP thì không liên kết (xem Q-AUTH-001).
  **Theo R6: không liên kết đơn cũ ở phiên bản này** (xem mục "Hệ quả của R6").
- **Q-AUTH-008 (Quan trọng): Đồng ý xử lý dữ liệu cá nhân.**
  Nghị định 13/2023/NĐ-CP yêu cầu sự đồng ý rõ ràng, nêu mục đích, và quyền xoá dữ liệu. Trang `/privacy`
  và `/terms` đã có, nhưng nội dung cần owner rà lại cho tài khoản.
  `[PROPOSED]` Checkbox đồng ý bắt buộc khi đăng ký, lưu thời điểm đồng ý và phiên bản chính sách.

### 3.2 Đăng nhập và phiên

- **Q-AUTH-009 (Quan trọng): Phiên đăng nhập kéo dài bao lâu, có "Ghi nhớ đăng nhập" không?**
  Hiện JWT 24 giờ, không refresh, đăng xuất không thu hồi được token.
  `[PROPOSED]` Access token 1 giờ, refresh token 30 ngày lưu ở server (thu hồi được). Đăng xuất, đổi mật
  khẩu, đặt lại mật khẩu đều thu hồi refresh token.
- **Q-AUTH-010 (Chặn về kỹ thuật, không cần owner): Token khách và token admin đè nhau.**
  FE gắn `localStorage['authToken']` vào mọi request. Nếu token khách cũng lưu ở khoá này thì admin và khách
  cùng một trình duyệt sẽ đăng xuất lẫn nhau, và token khách bị gửi lên API admin.
  `[PROPOSED]` Khoá lưu riêng cho khách, chỉ gắn vào các API của khách.
- **Q-AUTH-011 (Quan trọng): Chống dò mật khẩu.**
  `[PROPOSED]` Sai 5 lần liên tiếp thì khoá đăng nhập tài khoản đó 15 phút; giới hạn theo IP; thông báo lỗi
  chung "SĐT hoặc mật khẩu không đúng" (không nói SĐT có tồn tại hay không).
- **Q-AUTH-012 (Nên có): Đăng nhập bằng OTP không cần mật khẩu, hoặc qua Google, Facebook, Zalo?**
  `[PROPOSED]` Ngoài phạm vi giai đoạn 1.

### 3.3 Quên và đổi mật khẩu

- **Q-AUTH-013 (Quan trọng): Quy tắc OTP.**
  Mỗi tin SMS tốn tiền, nên chống spam là yêu cầu chi phí chứ không chỉ bảo mật.
  `[PROPOSED]` OTP 6 số, hết hạn sau 5 phút, sai tối đa 5 lần thì huỷ OTP; gửi lại sau 60 giây; tối đa 5 OTP
  mỗi SĐT mỗi ngày và giới hạn theo IP; chỉ lưu bản băm của OTP.
  **Hoãn theo R6** (áp dụng khi làm OTP).
- **Q-AUTH-014 (Quan trọng): Đổi mật khẩu.**
  `[PROPOSED]` Cần mật khẩu cũ; đổi xong đăng xuất các thiết bị khác, giữ thiết bị hiện tại.
- **Q-AUTH-015 (Quan trọng): Có cho đổi số điện thoại (tên đăng nhập) không?**
  `[PROPOSED]` Phiên bản này không cho tự đổi; khách liên hệ để admin hỗ trợ. Khi có OTP thì cho đổi, xác minh số mới.

### 3.4 Profile

- **Q-AUTH-016 (Quan trọng): Profile gồm những gì?**
  `[PROPOSED]` Họ tên, email, ngày sinh, giới tính (tuỳ chọn); SĐT chỉ xem. Một địa chỉ giao hàng mặc định
  (tỉnh, huyện, xã dùng lại `LocationService`) để tự điền ở checkout. Không có ảnh đại diện (dùng chữ cái đầu).
- **Q-AUTH-017 (Chặn): "Không đổi khi mua hàng" nghĩa là gì khi khách đã đăng nhập?**
  Ba cách hiểu: (a) checkout giữ nguyên hoàn toàn, không biết ai đang đăng nhập; (b) vẫn không bắt buộc đăng
  nhập, nhưng đã đăng nhập thì tự điền tên, SĐT, địa chỉ và gắn đơn vào tài khoản; (c) bắt buộc đăng nhập.
  `[PROPOSED]` (b). Khách vẫn mua không cần tài khoản.
  **Đã chốt: (b) (R7).**
- **Q-AUTH-018 (Quan trọng): Có trang "Đơn hàng của tôi" không?**
  `[PROPOSED]` Có, chỉ xem (mã đơn, ngày, tổng tiền, trạng thái). Sau khi có trang này, nên đóng API public
  `GET /api/extend/orders/customer/{phone}` (đang lộ đơn của bất kỳ SĐT nào). Đóng API này đụng tới admin UI,
  làm ở task riêng.
- **Q-AUTH-019 (Quan trọng): Xoá tài khoản.**
  Quyền của chủ thể dữ liệu (NĐ 13). `[PROPOSED]` Có nút "Xoá tài khoản" (xác nhận bằng mật khẩu): ẩn danh
  thông tin cá nhân, giữ đơn hàng cho kế toán, đánh giá chuyển tên thành "Khách hàng".

### 3.5 Đánh giá sản phẩm khi đã có tài khoản

- **Q-AUTH-020 (Chặn): Ai được đánh giá?**
  (a) Mọi tài khoản đã đăng nhập; (b) chỉ tài khoản có đơn đã giao (`DELIVERED`) chứa sản phẩm đó.
  (b) chống đánh giá ảo tốt hơn nhưng khách mới đăng ký sẽ không đánh giá được.
  `[PROPOSED]` (a), và gắn nhãn "Đã mua hàng" cho đánh giá có đơn đã giao khớp SĐT.
  **Đã chốt: (a) + nhãn (R8)**; nhãn chỉ tính đơn đặt khi đã đăng nhập, không khớp theo SĐT (xem "Hệ quả của R6").
- **Q-AUTH-021 (Quan trọng): Mỗi tài khoản đánh giá một sản phẩm mấy lần, có sửa hoặc xoá được không?**
  `[PROPOSED]` Một đánh giá mỗi sản phẩm mỗi tài khoản; khách sửa hoặc xoá được đánh giá của mình; sửa thì vẫn
  đăng ngay như task-002.
- **Q-AUTH-022 (Quan trọng): Tên hiển thị trên đánh giá.**
  `[PROPOSED]` Lấy họ tên trong profile, che bớt (ví dụ "Nguyễn T. H."), không hiện SĐT. Đổi tên ở profile thì
  đánh giá cũ đổi theo.
- **Q-AUTH-023 (Quan trọng): Đánh giá cũ không có tài khoản (khách vãng lai, seed, admin thêm) xử lý sao?**
  `[PROPOSED]` Giữ nguyên, vẫn hiện; không gắn vào tài khoản nào.
- **Q-AUTH-024 (Quan trọng): Đang gõ dở đánh giá thì phiên hết hạn.**
  `[PROPOSED]` Bấm "Viết đánh giá" thì kiểm tra đăng nhập trước khi mở form. Nếu lúc gửi phiên đã hết hạn thì
  hiện popup đăng nhập và giữ nguyên nội dung, ảnh đã chọn; đăng nhập xong gửi tiếp.

### 3.6 Popup và giao diện

- **Q-AUTH-025 (Quan trọng): Popup gồm những màn nào?**
  `[PROPOSED]` Một popup, chuyển qua lại: Đăng nhập, Đăng ký (một bước, không OTP theo R6), Quên mật khẩu
  (hướng dẫn gọi hotline theo R6), Đổi mật khẩu bắt buộc khi dùng mật khẩu tạm. Điện thoại thì popup chiếm toàn màn hình. Có trạng thái đang gửi, lỗi từng ô,
  phím Esc và nút X để đóng, giữ focus trong popup.
- **Q-AUTH-026 (Quan trọng): Sau khi đăng nhập thì trang thay đổi gì?** (chi tiết hoá R4)
  `[PROPOSED]` Header: nút "Đăng nhập" đổi thành tên khách kèm menu (Tài khoản, Đơn hàng của tôi, Đăng xuất);
  quay lại đúng hành động đang dở (ví dụ mở form đánh giá); trang checkout tự điền thông tin; không tải lại
  trang. Đăng xuất ở tab này thì các tab khác cũng cập nhật.
- **Q-AUTH-027 (Quan trọng): Profile, đổi mật khẩu, đơn hàng nằm ở đâu?**
  `[PROPOSED]` Trang riêng `/tai-khoan` (các tab Thông tin, Đổi mật khẩu, Đơn hàng). Chưa đăng nhập mà mở
  trang này thì hiện popup đăng nhập.
- **Q-AUTH-028 (Quan trọng): Header hiện chưa có chỗ cho tài khoản.**
  Cần vị trí cho biểu tượng tài khoản ở cả máy tính và điện thoại (header có logo, menu, tìm kiếm, giỏ hàng).
  `[PROPOSED]` Biểu tượng người cạnh giỏ hàng, cả hai giao diện.

### 3.7 Admin

- **Q-AUTH-029 (Quan trọng): Admin quản lý tài khoản khách thế nào?**
  `[PROPOSED]` Trang Users hiện có thêm danh sách tài khoản đã đăng ký: tìm theo SĐT/tên, xem đơn và đánh giá
  của khách, khoá/mở khoá tài khoản. Admin không xem được mật khẩu. Theo R6, admin có "Đặt lại mật khẩu" sinh
  mật khẩu tạm, khách bắt buộc đổi ở lần đăng nhập sau.
- **Q-AUTH-030 (Nên có): Có cần lịch sử đăng nhập, thiết bị đang đăng nhập không?**
  `[PROPOSED]` Ghi log sự kiện (đăng ký, đăng nhập sai, đặt lại mật khẩu) ở server; chưa hiện cho khách.

### 3.8 Ngoài phạm vi cần owner xác nhận

- Đồng bộ khách hàng sang Pancake POS: `[PROPOSED]` không làm ở giai đoạn này.
- Điểm thưởng, mã giảm giá theo tài khoản, wishlist, giỏ hàng lưu theo tài khoản (giỏ hiện lưu ở trình
  duyệt): `[PROPOSED]` không làm ở giai đoạn này.
- Thông báo (SMS, Zalo) khi đơn đổi trạng thái: không làm.

## 4. Thiết kế nháp `[PROPOSED]` (owner duyệt "Start phase 1", 2026-11-18; cái đã làm thật xem mục 6)

### 4.1 Dữ liệu (migration V22)

- `customers`: `id`, `phone` (unique, đã chuẩn hoá), `password_hash`, `full_name`, `email`, `birth_date`,
  `gender`, `default_address` (tên người nhận, SĐT nhận, địa chỉ, mã tỉnh/huyện/xã), `status`
  (`ACTIVE|LOCKED|DELETED`), `phone_verified_at` (để trống ở phiên bản này, dành cho OTP sau),
  `must_change_password`, `failed_login_count`, `locked_until`, `consent_at`, `consent_version`,
  `created_at`, `updated_at`.
- Không có bảng OTP ở phiên bản này (R6).
- `customer_refresh_tokens`: `customer_id`, `token_hash`, `expires_at`, `revoked_at`, `user_agent`, `created_at`.
- `product_reviews`: thêm `customer_id` (nullable, FK), unique (`product_id`, `customer_id`) khi `customer_id`
  khác null.
- `orders`: thêm `customer_id` (nullable, FK) theo R7.

### 4.2 API

| Method | Path | Quyền | Ghi chú |
|---|---|---|---|
| POST | `/api/dragun/auth/register` | public | `{phone, fullName, email?, password, consent}` → token; giới hạn theo IP |
| POST | `/api/dragun/auth/login` | public | `{phone, password}` → access + refresh token, thông tin khách |
| POST | `/api/dragun/auth/refresh` | public | đổi refresh token lấy access token mới |
| POST | `/api/dragun/auth/logout` | khách | thu hồi refresh token |
| GET/PUT | `/api/dragun/account/profile` | khách | PUT có `updatedAt` (khoá lạc quan, 409) |
| PUT | `/api/dragun/account/password` | khách | `{currentPassword, newPassword}` |
| GET | `/api/dragun/account/orders` | khách | chỉ đơn đặt khi đã đăng nhập (không liên kết đơn cũ theo SĐT, xem R6) |
| DELETE | `/api/dragun/account` | khách | xoá tài khoản theo Q-AUTH-019 |
| POST/PUT/DELETE | `/api/dragun/products/{id}/reviews[/{reviewId}]` | khách | đổi: bắt buộc đăng nhập |
| GET/PATCH | `/api/dragun/admin/customers[/{id}/status]` | ADMIN | danh sách, khoá/mở khoá |
| POST | `/api/dragun/admin/customers/{id}/reset-password` | ADMIN | sinh mật khẩu tạm, bật `must_change_password` (thay quên mật khẩu tự phục vụ, R6) |
| POST | `/api/extend/orders` | public | không đổi hợp đồng; có token khách hợp lệ thì gắn `customer_id` vào đơn (R7) |

Đăng nhập admin (`/api/dragun/admin/login`) và luồng đặt hàng không đổi.

### 4.3 Giao diện

- `components/auth/AuthModal.vue`: popup Đăng nhập, Đăng ký; "Quên mật khẩu" chỉ hiện hướng dẫn gọi hotline
  (R6); màn bắt buộc đổi mật khẩu khi đăng nhập bằng mật khẩu tạm.
- `stores/auth.js` (Pinia): khách hiện tại, token, `requireLogin(action)` để mở popup rồi chạy tiếp hành
  động đang dở; đồng bộ giữa các tab.
- `services/auth.service.js`, `services/account.service.js`.
- Header (`App.vue`): biểu tượng tài khoản và menu.
- `views/Account.vue` tại `/tai-khoan`.
- `ProductDetail.vue`: "Viết đánh giá" gọi `requireLogin`; bỏ ô nhập tên; nút sửa/xoá đánh giá của mình.
- `Checkout.vue`: tự điền khi đã đăng nhập, gửi kèm token để đơn gắn tài khoản (R7).
- Admin: thêm danh sách tài khoản vào `AdminUsers.vue`, nút khoá/mở khoá và "Đặt lại mật khẩu".

### 4.4 Test

- `CustomerAuthServiceTest`: đăng ký, trùng SĐT, chuẩn hoá SĐT, thiếu đồng ý điều khoản, đăng nhập đúng và
  sai, khoá sau 5 lần sai, tài khoản bị khoá, đổi mật khẩu thu hồi phiên khác, mật khẩu tạm bắt buộc đổi.
- `OrderServiceTest`: đặt hàng không token vẫn chạy như cũ; có token thì đơn gắn `customer_id`.
- `ProductReviewServiceTest`: chưa đăng nhập bị từ chối, một đánh giá mỗi tài khoản, sửa và xoá của mình,
  không sửa được của người khác.
- Test phân quyền: token khách gọi API admin trả 403.

### 4.5 Đề xuất chia giai đoạn

1. **Giai đoạn 1:** đăng ký, đăng nhập, đăng xuất, popup, header, profile, đổi mật khẩu, đánh giá bắt buộc
   đăng nhập, tự điền checkout và gắn đơn vào tài khoản, đơn hàng của tôi.
2. **Giai đoạn 2:** admin quản lý tài khoản (khoá, đặt lại mật khẩu), nhãn "Đã mua hàng", xoá tài khoản.
3. **Phiên bản sau:** OTP (đăng ký, quên mật khẩu tự phục vụ), liên kết đơn cũ theo SĐT đã xác minh, đóng API
   tra đơn theo SĐT.

## 6. Giai đoạn 1 đã triển khai (2026-11-18)

- **BE:** `V22__Create_customer_accounts.sql`; `Customer`, `CustomerRefreshToken`; `CustomerAuthService`
  (đăng ký, đăng nhập, refresh xoay vòng, đăng xuất, đổi mật khẩu), `CustomerAccountService` (profile);
  `CustomerAuthController` (`/api/dragun/auth/*`), `CustomerAccountController` (`/api/dragun/account/*`);
  `JwtAuthenticationFilter` nhận token khách; đánh giá bắt buộc đăng nhập; đơn đặt khi đăng nhập gắn `customer_id`.
  Chi tiết route: `docs/ai/20-api.md` mục 2.1b.
- **FE:** `stores/auth.js`, `services/customer.service.js`, `components/auth/AuthModal.vue` (popup), menu tài khoản
  trên header (máy tính và điện thoại), `views/Account.vue` tại `/tai-khoan` (Thông tin, Đổi mật khẩu, Đơn hàng),
  `ProductDetail.vue` (Viết đánh giá gọi popup, bỏ ô tên, phiên hết hạn thì giữ nội dung), `Checkout.vue` (tự điền,
  gửi token).
- **Khác với nháp:**
  - `[PROPOSED]` Tên trên đánh giá là họ tên đầy đủ trong profile, chưa che bớt như đề xuất ở Q-AUTH-022; khách tự
    quyết tên hiển thị qua profile. Cần owner xác nhận.
  - Chưa làm sửa/xoá đánh giá của chính khách (Q-AUTH-021): khách đã đánh giá thì nhận thông báo "Bạn đã đánh giá
    sản phẩm này rồi". Chuyển sang giai đoạn 2.
  - `must_change_password` có cột nhưng chưa có màn bắt buộc đổi; dùng khi làm "Đặt lại mật khẩu" của admin (giai đoạn 2).
- **Kiểm tra:** `mvn test` 58 test xanh (thêm `CustomerAuthServiceTest` 10, `CustomerAccountServiceTest` 3,
  `CustomerSecurityFlowTest` 6, `ProductReviewServiceTest` sửa thành 9). Trên trình duyệt đã thử: popup đăng ký
  (kiểm tra dữ liệu, SĐT `+84` được chuẩn hoá), header cập nhật không tải lại trang, menu tài khoản, đăng xuất,
  đăng nhập sai và đúng; gọi API từ trình duyệt: đánh giá khi chưa đăng nhập 401, đã đăng nhập 200, lần hai 409,
  đặt hàng có token hiện trong "Đơn hàng của tôi", token khách gọi API admin 403.
- **Chưa kiểm trên màn hình** (pane trình duyệt bị ẩn nên trang chuyển route không hiện): nút "Viết đánh giá" mở popup
  trên trang sản phẩm, trang `/tai-khoan`, tự điền ở checkout.

## 5. Rủi ro

- `[INFERRED]` Giới hạn đăng ký/đăng nhập theo IP nằm trong bộ nhớ một instance API (như giới hạn đánh giá); chạy
  nhiều instance thì mỗi instance đếm riêng.

- `[INFERRED]` Không có OTP (R6): người khác có thể đăng ký trước bằng SĐT của khách thật; khách thật khi đó
  không đăng ký được và phải gọi hotline. Admin cần quy trình xử lý tranh chấp SĐT (khoá tài khoản giả, cấp lại).
- `[INFERRED]` Đặt lại mật khẩu qua hotline dựa vào việc admin xác minh người gọi; xác minh lỏng thì bị chiếm
  tài khoản. Tài khoản khách hiện chưa chứa dữ liệu thanh toán nên thiệt hại chủ yếu là lộ địa chỉ và đơn.
- `[INFERRED]` Bắt buộc đăng nhập để đánh giá sẽ làm số đánh giá mới giảm mạnh, vì thêm bước đăng ký.
- `[INFERRED]` Chi phí SMS tăng theo số lượt đăng ký và lượt bị spam; cần giới hạn ở Q-AUTH-013 trước khi bật
  nhà cung cấp thật.
- `[INFERRED]` Token khách lưu ở trình duyệt có thể bị lấy nếu trang có lỗi XSS; phiên ngắn và refresh token thu
  hồi được giảm thiệt hại.
- `[INFERRED]` Trang tra đơn theo SĐT đang public; thêm tài khoản mà không đóng nó thì vẫn lộ dữ liệu như cũ.
