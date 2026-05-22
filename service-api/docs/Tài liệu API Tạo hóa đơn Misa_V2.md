## Tài liệu hướng dẫn tích hợp

> **Thiyen (`service-api`)** — spec V2: `/auth/token`, `POST /invoice` (SignType **2** HSM thường, **5** MTT), `/invoice/status`, `/invoice/download`.  
> Nháp / preview V1: **[Tài liệu API Tạp hóa đơn Misa.md](./Tài%20liệu%20API%20Tạp%20hóa%20đơn%20Misa.md)**.  
> Sơ đồ luồng, cấu hình `meinvoice.*`, API admin: **[05-meinvoice-integration-conclusion.md](./05-meinvoice-integration-conclusion.md)**.

- 1. Giới thiệu.................................................................................................................. MISA meInvoice Phát hành hóa đơn
   - 1.1. Thông tin cần trước khi kết nối...........................................................................................
   - 1.2. Thông tin chi tiết để kết nối...............................................................................................
   - 1.3. Mô tả cấu trúc của API MeInvoice....................................................................................
   - 1.4. Hướng dẫn xử lý kết quả của API Response)..................................................................
   - 1.5. Các bước thực hiện...........................................................................................................
   - 1.6. Những lưu ý trước khi tích hợp Hiểu thêm về hóa đơn và API.......................................
- 2. API lấy mã kết nối Get token).....................................................................................
- 3. API lấy danh sách chứng thư số......................................................................
- 4. API lấy danh sách mẫu hóa đơn/ mẫu vé......................................................................
- 5. API xem hóa đơn chưa phát hành................................................................................
- 6. API phát hành hóa đơn ký số USB/File........................................................................
   - 6.1. API tạo XML hóa đơn........................................................................................................
   - 6.2. API ký số dữ liệu XML......................................................................................................
   - 6.3. API phát hành XML đã được ký số..................................................................................
- 7. API phát hành hóa đơn/vé - ký số HSM/ Không ký.......................................................
- 8. API xem hóa đơn đã phát hành..................................................................................
- 9. API tải hóa đơn........................................................................................................
- 10. API điều chỉnh/Thay thế..........................................................................................
- 11. API gửi Email..........................................................................................................
- 12. Lấy danh sách hóa đơn đã phát hành........................................................................
   - 12.1. Lấy trạng thái hóa đơn đã phát hành..............................................................................
   - 12.2. Lấy danh sách hóa đơn đã phát hành...........................................................................
- 13. Chuyển đổi hóa đơn giấy.........................................................................................
- 14. Mô tả đối tượng của hóa đơn...................................................................................
   - 14.1. Template (mẫu hóa đơn)................................................................................................
   - 14.2. Ticket (mẫu vé)..............................................................................................................
   - 14.3. InvoiceData Dữ liệu hóa đơn).......................................................................................
   - 14.4. OriginalInvoiceDetail (dữ liệu dòng hàng hóa, dịch vụ)...............................................
   - 14.5. TaxRateInfo (tổng hợp thuế suất).................................................................................
   - 14.6. OtherInfo (thông tin khác).............................................................................................
   - 14.7. FeeInfo (phí khác)..........................................................................................................
   - 14.8. ClockInfo (thông tin về đồng hồ nước)..........................................................................
   - 14.9. OptionUserDefined (thiết lập người dùng - hiển thị thập phân của tiền).....................
   - 14.10. PublishInvoiceData Dữ liệu phát hành)......................................................................
   - 14.11. InvoiceStatus (trạng thái hóa đơn)...............................................................................
   - 14.12. Chứng thư số................................................................................................................
- 15. Mã lỗi thường gặp..................................................................................................


## 1. Giới thiệu

Tài liệu này dành cho các nhà phát triển ứng dụng muốn ứng dụng của mình có thể
phát hành hóa đơn thông qua việc kết nối với hệ thống phần mềm **MISA MeInvoice**.

Tài liệu sẽ mô tả phương thức kết nối giữa các ứng dụng từ Client (máy chủ/máy trạm
của Khách hàng) tới service của hệ thống MISA MeInvoice.

### 1.1. Thông tin cần trước khi kết nối...........................................................................................

```
Thông tin Diễn giải
app_id Đối tác/KH có nhu cầu kết nối MeInvoice cần đăng ký sử dụng API,
đầu mối tiếp nhận là NVKD.
Ký hiệu hóa đơn Lấy thông tin từ bộ phận kế toán của đơn vị
Hình thức ký số - Ký số qua USB token
```
- Ký số qua HSM/Esign nâng cao
- Không ký số (chỉ áp dụng cho hóa đơn từ MTT
Thông tin sandbox Sẽ được cấp cùng app_id khi KH đăng ký sử dụng

### 1.2. Thông tin chi tiết để kết nối...............................................................................................

```
Thông tin Diễn giải
API_AIO_Url Base url) SandBox: https://testapi.meinvoice.vn/api/integration
Product: https://api.meinvoice.vn/api/integration
Token url (lấy token) /auth/token
Lấy danh sách mẫu hóa đơn có thể
phát hành
```
```
/invoice/templates
```
```
Xem hóa đơn trước khi phát hành /invoice/unpublishview
Phát hành hóa đơn /invoice
Xem hóa đơn đã phát hành /invoice/publishview
Xử lý sai sót hóa đơn /invoice
Tải hóa đơn /invoice/download
Lấy trạng thái hóa đơn /invoice/status
Gửi email hóa đơn đã phát hành /invoice/sendemail
Chuyển đổi hóa đơn giấy /invoice/voucher-paper
```

### 1.3. Mô tả cấu trúc của API MeInvoice....................................................................................

```
Thông tin Diễn giải
```
Url Theo từng API cụ thể

Method Get/Post Theo từng API cụ thể)

Params Theo từng API cụ thể

Header Authorization: Bearer {token}

Body Content-Type: application/json Đối tượng theo từng API cụ thể)

Response {
"success":true/false,
"errorCode": "Mã lỗi",
"descriptionErrorCode": "Mô tả lỗi",
"createInvoiceResult": "Kết quả tạo hóa đơn",
"publishInvoiceResult": "Kết quả phát hành hóa đơn",
"errors": "Danh sách mô tả lỗi",
"data": "Dữ liệu phản hồi"
"customData": "thông tin khác"
}

### 1.4. Hướng dẫn xử lý kết quả của API Response)..................................................................

success Trạng thái phản hồi của API
True: Thành công
False: Thất bại

errorCode Mã lỗi trả về VD InvalidInvoiceData)

descriptionErrorCode Mô tả về thông tin lỗi VD Tham số không hợp lệ)

createInvoiceResult Kết quả trả về của API tạo dữ liệu XML khi dùng SignType = 1

publishInvoiceResult Kết quả trả về của API phát hành hóa đơn

Data Kết quả trả về của mỗi API

Cách kiểm tra Response Kiểm tra success→ kiểm tra errorCode→ xử lý lỗi → Kiểm tra
createInvoiceResult.errorCode hoặc
publishInvoiceResult.errorCode → xử lý lỗi ⇒ lưu log response

Các thông tin cần lưu lại token: hạn 14 ngày
Body của API
Response của API
để phục vụ việc hỗ trợ về sau nếu phát sinh


### 1.5. Các bước thực hiện...........................................................................................................

Đăng ký sử dụng API Đăng ký sử dụng API, liên hệ NVKD của MISA
Nhận về app_id (dùng cho cả sandbox và product) và thông tin
sandbox gồm

- Mã số thuế
- Tài khoản MeInvoice
- Mật khẩu MeInvoice
Khi thực hiện golive, lấy 3 thông tin trên từ bộ phận kế toán

Thực hiện lấy đăng nhập
ứng dụng MeInvoice

```
Sandbox: https://testapp3.meinvoice.vn/v3/ban-lam-viec
Product: https://app3.meinvoice.vn/v3/ban-lam-viec
Xem thêm thiết lập hình thức hóa đơn sử dụng
```
Thiết lập hình thức ký số
hóa đơn

- Thiết lập tại đây
- Xem hướng dẫn

Lập tờ khai Xem tại đây

Tạo mẫu hóa đơn Xem tại đây

```
Thao thác với API
```
```
Bước 1 Thực hiện gọi
API lấy token
```
- Xem mô tả
**Lưu ý:**
- Token có hạn 14 ngày, chỉ nên gọi ở đầu phiên làm việc (lần đầu
đăng nhập, đầu ngày, đầu tuần ...)

```
Bước 2 Thực hiện lấy
danh sách mẫu hóa đơn
```
- Xem mô tả
**Lưu ý: Chỉ thực hiện gọi lại khi Ký hiệu hóa đơn có sự thay đổi
(không tính thay đổi năm ký hiệu)**

```
Bước 3 Xem hóa đơn
trước khi phát hành
```
- Xem mô tả

```
Bước 4 Phát hành hóa
đơn
```
- Xem mô tả phát hành dùng USB token
- Xem mô tả phát hành dùng HSM/eSgin nâng cao
**Lưu ý:**
- Mẫu hóa đơn phải đang ở trạng thái **Sử dụng**
- Tờ khai phải đang ở trạng thái **CQT chấp nhận**
- Sử dụng **SignType** đúng với loại hình ký số mong muốn

```
Xử lý với các nghiệp vụ khác nếu cần
```
```
Xem hóa đơn sau khi
phát hành
```
- Xem hướng dẫn

```
Tải hóa đơn - Xem hướng dẫn
```
```
Lấy trạng thái hóa đơn - Xem hướng dẫn
```
```
Điều chỉnh/ thay thế
hóa đơn
```
- Xem hướng dẫn


### 1.6. Những lưu ý trước khi tích hợp Hiểu thêm về hóa đơn và API.......................................

```
Thông tin Diễn giải
```
SignType Kiểu ký số khi sử dụng API

```
1 Ký số qua USB/file mềm
2 Ký số qua HSM, có hiển thị CKS
5 Ký số sau loại Hóa đơn MTT , không hiển thị CKS
```
```
3 Ký số qua HSM, có hiển thị CKS, luồng bất đồng bộ
4 Ký số sau loại Vé không mã , không hiển thị CKS
6 Ký số sau loại Hóa đơn/Vé MTT , không hiển thị CKS,
luồng bất đồng bộ
HSM SignType = 2 hỗ trợ các loại Chứng thư số của
```
- Esign nâng cao
- SoftDream
- Cyber Lotus
- ICORP

Trường hợp dữ liệu có thập
phân

- Việc thể hiện bao nhiêu số thập phân được định nghĩa trong
tag: **OptionUserDefined**
- Việc Round số chữ số thập phân phải khớp với số lượng
được định nghĩa trong **OptionUserDefined**
- Khuyến nghị Round từng phần rồi cộng tổng thay vì cộng
tổng xong Round

Công thức tính toán Tham khảo sheet **Công thức (từ tài liệu được cung cấp hoặc
bảng mô tả đối tượng)**

Ký hiệu hóa đơn - Sẽ thay đổi theo năm.
VD năm 2024, ký hiệu là 1C **24** MYY thì sang năm 2025, ký
hiệu sẽ đổi thành 1C **25** MYY (hệ thống MISA tự động xử lý
theo thông tin InvDate)

Xem hóa đơn đã phát hành tại - Môi trường test: https://testapp3.meinvoice.vn/

- Môi trường product: https://app3.meinvoice.vn
(Menu Báo cáo/Bảng kê hóa đơn đã sử dụng)

Xác định loại hình hóa đơn Dùng ký tự đầu tiên của ký hiệu để xác định: **X** C25TYY
X = 1 hóa đơn GTGT
X = 2 hóa đơn bán hàng
X = 5 vé điện tử
X = 6 phiếu xuất kho
VD 1C25TYY (hóa đơn GTGT

Xác định loại hóa đơn **Có
mã/Không mã**

```
Dùng ký tự thứ 2 của ký hiệu để xác định: 1 X 25MYY
X = C hóa đơn có mã
X = K hóa đơn không mã
```
```
VD 1C25MYY (hóa đơn có mã)
```
Xác định hình thức hóa đơn
**Thường/ HĐ từ máy tính tiền**

```
Dùng ký tự thứ 5 của ký hiệu để xác định: 1C25 X YY
X = T hóa đơn thường
X = M hóa đơn từ máy tính tiền
```
```
VD 1C25MYY (hóa đơn từ máy tính tiền)
```
Các trường hợp gặp mã lỗi
cần retry (phát hành lại)

```
InvoiceDuplicated
InvoiceNumberNotCotinuous
```
Xử lý request Bắt buộc **PHẢI** xử lý request tuần tự với mỗi ký hiệu hóa đơn.

- Vòng lặp: Gửi request ⇒ nhận response ⇒ xử lý response
⇒ lưu lại thông tin hóa đơn]

Postman tham khảo - Tải về


## 2. API lấy mã kết nối Get token).....................................................................................

Sử dụng khi kết nối với ứng dụng MISA MeInvoice để lấy token phục vụ cho các hàm
xử lý nghiệp vụ

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/auth/token
```
```
Method Post
```
```
Body request {
"appid": "AppID",
"taxcode": "{taxcode}",
"username": "{user meinvoice}",
"password": "{pasword meinvoice}"
}
```
```
Response {
"Success": <false/true>,
"Data": " Dữ liệu token trả về> ",
"ErrorCode": "Trống hoặc Mã lỗi>",
"Errors": "",
"CustomData":""
}
```
```
Mã lỗi thường gặp Xem tại đây
```
```
Lưu ý Token có hạn 14 ngày, chỉ nên gọi ở đầu phiên làm việc (lần
đầu đăng nhập, đầu ngày, đầu tuần, thay đổi tài khoản ...)
(kiến nghị của MISA là 7 ngày gọi 1 lần)
```

## 3. API lấy danh sách chứng thư số......................................................................

Sử dụng nếu đơn vị có nhu cầu sử dụng cks

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice/get-certificates
```
```
Method Get
```
```
Response {
"Success": <false/true>,
"Data": " Danh sách Chứng thư số trả về> ",
"ErrorCode": "Trống hoặc Mã lỗi>",
"Errors": "",
"CustomData":""
}
```
```
Mã lỗi thường gặp Xem tại đây
```
```
Lưu ý Nếu Chứng thư số không trả về trong danh sách thì Chứng
thư số đó không đủ điều kiện để phát hành qua API
```

## 4. API lấy danh sách mẫu hóa đơn/ mẫu vé......................................................................

```
Thông tin Diễn giải
```
URL {API_AIO_Urll}/invoice/templates

Method Get

Params invoiceWithCode: true/false (dùng loại hóa đơn có mã hay
không mã)
ticket: true/false (lấy mẫu vé)

Header Content-Type: application/json
Authorization: Bearer {token}

Response {
"Success": <false/true>,
"Data": " **Danh sách mẫu>** ",
"ErrorCode": "Trống hoặc Mã lỗi>",
"Errors": "",
"CustomData":""
}
Lưu lại thông tin **_IsSendSummary, InvSeries_** của mẫu hóa đơn
trong **Data** , để thực hiện khi phát hành hóa đơn

Mã lỗi thường gặp Xem tại đây

Mô tả đối tượng **Mô tả đối tượng (Template)**


## 5. API xem hóa đơn chưa phát hành................................................................................

```
Thông tin Diễn giải
```
URL {API_AIO_Url}/invoice/unpublishview

Method Post

Header Content-Type: application/json
Authorization: Bearer {token}

Body {
**InvoiceData**
}

```
Chỉ xem được 1 hóa đơn
```
Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " **link xem hóa đơn** ",
"customData": null
}

Mã lỗi thường gặp Xem tại đây

Mô tả đối tượng **Mô tả đối tượng (InvoiceData)**

Lưu ý Link xem trước chỉ tồn tại ở thời điểm xem trong 5 phút (làm
mới sẽ vô hiệu link cũ)


## 6. API phát hành hóa đơn ký số USB/File........................................................................

**Khuyến nghị sử dụng khi các đơn vị sử dụng app winform, các đơn vị sử dụng web
browser nên sử dụng HSM (vì cần triển khai 1 server giao thức http)**

### 6.1. API tạo XML hóa đơn........................................................................................................

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice
```
```
Method Post
```
```
Header Content-Type: application/json
Authorization: Bearer {token}
```
```
Body {
"SignType": 1,
"InvoiceData": [list InvoiceData],
"PublishInvoiceData": null
}
```
```
Response {
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"createInvoiceResult": [danh sách xml được tạo],
"publishInvoiceResult": null
}
Kiểm tra có createInvoiceResult. ErrorCode là chưa thành
công
```
```
Mã lỗi thường gặp Xem tại đây
```
```
Mô tả đối tượng Mô tả đối tượng (InvoiceData)
```
```
Lưu ý SignType được mô tả ở mục 1.3 Thông tin lưu ý
Dữ liệu XML của hóa đơn là thẻ:
createInvoiceResult. InvoiceData
```

### 6.2. API ký số dữ liệu XML......................................................................................................

```
Thông tin Diễn giải
```
URL {computerName}:12019/api/SignXML

```
computerName: tên/ip của máy cài đặt tool ký số
```
Method Post

Header MisaTokenKey: 491CB943E4664D25B0A97042594F59F

Body {
"PinCode": "xxxxxxx", //mật khẩu của cks
"XmlContent": {đối tượng XML được tạo ở bước 5.1}
}

Response {^
"Status": 200,
"PayLoad": "", //Chuỗi XML đã được ký số
"Message": null //thông báo lỗi nếu cố
}

Link tải công cụ ký
số

```
https://product.misa.vn/misasoftware/eInvoice/SignedService
/R24/MISA_SignedService_Setup_Lastest.exe
```

### 6.3. API phát hành XML đã được ký số..................................................................................

```
Thông tin Diễn giải
```
URL {API_AIO_Urll}/invoice

Method Post

Header Content-Type: application/json
Authorization: Bearer {token}
CompanyTaxCode: {taxcode}

Body {
"SignType": 1,
"InvoiceData": null
"PublishInvoiceData": [danh sách **PublishInvoiceData** ]
}

Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"createInvoiceResult": null,
"publishInvoiceResult": [danh sách thông tin hóa đơn phát
hành]
}

```
Kiểm tra có publishInvoiceResult. ErrorCode là chưa thành
công
```
Mã lỗi thường gặp Xem tại đây

Mô tả đối tượng **Mô tả đối tượng (PublishInvoiceData)**

Lưu ý Kiểm tra **ErrorCode** , nếu null thì hóa đơn mới phát hành thành
công

Xem hóa đơn đã
phát hành

```
Tại đây
```

## 7. API phát hành hóa đơn/vé - ký số HSM/ Không ký.......................................................

```
Thông tin Diễn giải
```
URL {API_AIO_Url}/invoice

Method Post

Header Content-Type: application/json
Authorization: Bearer {token}

Body {
"SignType": 2|5, 2 có hiển thị CKS; 5 Không hiển thị CKS
"CertificateSN": "Mã chứng thư số", (chỉ dùng với SignType
= 2 )
"InvoiceData": [danh sách hóa đơn phát hành]
"PublishInvoiceData": null
}

Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"createInvoiceResult": null,
"publishInvoiceResult": [danh sách thông tin hóa đơn phát
hành]
}

```
Kiểm tra có publishInvoiceResult. ErrorCode là chưa thành
công
```
Mã lỗi thường gặp Xem tại đây

Mô tả đối tượng **Mô tả đối tượng (InvoiceData)**

Lưu ý **Xem mô tả về thông tin SignType**
Kiểm tra **publishInvoiceResult**. **ErrorCode** , nếu empty thì hóa
đơn mới được phát hành thành công

Xem hóa đơn đã
phát hành

```
Tại đây
```
Ví dụ của **response
lỗi** , kiểm tra nội

dung ErrorCode cụ
thể (retry nếu gặp 2
mã lỗi ở *result:
**InvoiceDuplicated** và
**InvoiceNumberNotCo
tinuous)**


## 8. API xem hóa đơn đã phát hành..................................................................................

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice/publishview
```
```
Method Post
```
```
Header Content-Type: application/json
Authorization: Bearer {token}
CompanyTaxCode: {taxcode}
```
```
Body ["Danh^ sách^ TransactionID"]^
```
```
VD ["TranID1", "TranID1", ...]
```
```
TranID 1  Mã tra cứu hóa đơn
```
```
Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " link xem hóa đơn ",
"customData": null
}
```
```
Mã lỗi thường gặp Xem tại đây
```
```
Lưu ý Link xem chỉ tồn tại trong 5 phút
Nếu muốn xem lâu dài có thể tải file pdf
```

## 9. API tải hóa đơn........................................................................................................

```
Thông tin Diễn giải
```
```
URL {API_AIO_Urll}/invoice/download
```
```
Method Post
```
```
Header Authorization: Bearer {token}
```
```
Params invoiceWithCode: true/false (hóa đơn có mã/không mã)
invoiceCalcu: true/false là hóa đơn phát hành từ máy tính tiền
downloadDataType: xml/pdf/All
```
```
Body ["Danh sách TransactionID"]
```
```
VD ["TranID1", "TranID1", ...]
```
```
TranID 1  Mã tra cứu hóa đơn, tối đa 50 TransID/lần call
```
```
Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " InvoiceDataPublished ",
"customData": null
}
```
```
Mô tả InvoiceDataPublished
[
{
"TransactionID": “Mã tra cứu hóa đơnˮ,
"Data": “Dữ liệu của hóa đơn (dang base64ˮ,
}
]
```
```
Mã lỗi thường gặp Xem tại đây
```

## 10. API điều chỉnh/Thay thế..........................................................................................

Tương tự với việc phát hành hóa đơn, bổ sung những thông tin (ở master) dưới đây.
Nếu chưa có thông tin hóa đơn gốc có thể sử dụng API lấy trạng thái hóa đơn đã phát
hành rồi gán thông tin cho dữ liệu bị điều chỉnh bên dưới

```
Thông tin Kiểu dữ liệu Diễn giải
```
```
ReferenceType int Tính chất hóa đơn
1 Thay thế
2 Điều chỉnh
```
```
OrgInvoiceType int Loại hóa đơn bị thay thế/điều chỉnh
1 Hóa đơn NĐ 123
```
```
OrgInvTemplateNo string Mẫu số hóa đơn bị thay thế/điều chỉnh
```
```
VD Ký hiệu của hóa đơn theo ND123 bị thay thế
là 1C24MAA
thì OrgInvTemplateNo = 1 (ký tự đầu tiên)
```
```
OrgInvSeries string Ký hiệu hóa đơn theo ND123 bị thay thế/điều
chỉnh
```
```
VD Ký hiệu của hóa đơn bị thay thế là 1C24MAA
thì OrgInvSeries = C24MAA 6 ký tự cuối cùng)
```
```
OrgInvNo string Số hóa đơn bị thay thế/điều chỉnh
```
```
OrgInvDate string Ngày hóa đơn bị thay thế/điều chỉnh
```
```
InvoiceNote string Lý do thay thế/điều chỉnh
```

## 11. API gửi Email..........................................................................................................

**Chỉ thực hiện gửi ở Product**

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice/sendemail
```
```
Method Post
```
```
Header Authorization: Bearer {token}
```
```
Body {
"SendEmailDatas":
[
{
"TransactionID": "Mã tra cứu",
"ReceiverName": "Tên người nhận",
"ReceiverEmail": "Email nhận hóa đơn",
"CCEmail": "Email cc",
"ReplyEmail": "Email reply phản hồi"
}
], //Danh sách gửi email của hóa đơn
"IsInvoiceCode": true/false, //(hóa đơn có mã/không mã)
"IsInvoiceCalculatingMachine": true/false //(hóa đơn
MTT/thường)
}
Nếu gửi nhiều email, mỗi email cách nhau bởi dấu ";"
(chấm phảy), chỉ gửi 1 loại hình hóa đơn/ loại hóa đơn trong
1 request
```
```
Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": "",
"customData": null
}
```
```
Mã lỗi thường gặp Xem tại đây
```

## 12. Lấy danh sách hóa đơn đã phát hành........................................................................

### 12.1. Lấy trạng thái hóa đơn đã phát hành..............................................................................

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice/status
```
```
Method Post
```
```
Header Authorization: Bearer {token}
```
```
Params invoiceWithCode: true/false (hóa đơn có mã/không mã)
invoiceCalcu: true/false là hóa đơn phát hành từ máy tính tiền
inputType: 1
```
```
inputType = 1 lấy theo TransactionID
inputType = 2 lấy theo RefID
```
```
Body ["Danh sách TransactionID"]
```
```
VD ["TranID1", "TranID1", ...]
```
```
TranID 1  Mã tra cứu/RefID của hóa đơn, tối đa 50 mã/lần call
```
```
Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " List InvoiceStatus ",
"customData": null
}
```
```
Mã lỗi thường gặp Xem tại đây
```

### 12.2. Lấy danh sách hóa đơn đã phát hành...........................................................................

```
Thông tin Diễn giải
```
URL {API_AIO_Url}/invoice/paging

Method Post

Header Authorization: Bearer {token}

Params InvoiceWithCode: true/false (hóa đơn có mã/không mã)

Body {
"FromDate": "20251101", //lấy từ ngày
"ToDate": "20251130", //lấy đến ngày
"Skip": 0, //số lượng bỏ qua
"Take": 100, // số lượng lấy (tối đa 100
"ListInvTemplate": [] //danh sách ký hiệu hóa đơn
}

Response {^
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " **PagingData** ",
"customData": null
}

```
PagingData có dạng:
{\"PageData\":[ List
InvoiceData ],\"TotalCount\"0,\"Summary\":null}
```
Mã lỗi thường gặp Xem tại đây


## 13. Chuyển đổi hóa đơn giấy.........................................................................................

```
Thông tin Diễn giải
```
```
URL {API_AIO_Url}/invoice/voucher-paper
```
```
Method Post
```
```
Header Authorization: Bearer {token}
```
```
Params invoiceWithCode: true/false (hóa đơn có mã/không mã)
invoiceCalcu: true/false là hóa đơn phát hành từ máy tính tiền
Converter: người chuyển đổi
ConvertDate: ngày chuyển đổi (định dạng yyyy-MM-dd)
```
```
Body ["Danh sách TransactionID"]
```
```
VD ["TranID1", "TranID1", ...]
```
```
TranID 1  Mã tra cứu/RefID của hóa đơn, tối đa 50 mã/lần call
```
```
Response {
"success": true,
"errorCode": null,
"descriptionErrorCode": null,
"errors": [],
"data": " InvoiceDataPublished ",
"customData": null
}
```
```
Mô tả InvoiceDataPublished
[
{
"TransactionID": “Mã tra cứu hóa đơnˮ,
"Data": “Dữ liệu của hóa đơnˮ,
}
]
```
```
Mã lỗi thường gặp Xem tại đây
```

## 14. Mô tả đối tượng của hóa đơn...................................................................................

### 14.1. Template (mẫu hóa đơn)................................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
IPTemplateID string ID mẫu hóa đơn
CompanyID integer ID của công ty
TemplateName string Tên mẫu hóa đơn
InvTemplateNo string Mẫu số hóa đơn
InvSeries string Ký hiệu hóa đơn
OrgInvSeries string Ký hiệu hóa đơn rút gọn
CreatedDate string Ngày tạo
ModifiedDate string Ngày sửa đổi
Inactive boolean Trạng thái không hoạt động
IsInheritFromOldTemplate boolean Xác định có kế thừa từ mẫu cũ không
IsSendSummary boolean Xác định có gửi bảng tổng hợp không
IsTemplatePetrol boolean Xác định có phải là mẫu hóa đơn xăng dầu không
IsMoreVATRate boolean Xác định có nhiều mức thuế VAT không
```
### 14.2. Ticket (mẫu vé)..............................................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
TicketTemplateID string ID mẫu hóa đơn (trong JSON
TemplateName string Tên mẫu hóa đơn
InvSeries string Ký hiệu hóa đơn
Inactive boolean Trạng thái không hoạt động
TicketType integer Loại vé (trong JSON
ServiceName string Tên dịch vụ (trong JSON, không có trong bảng
mẫu)
IsInheritFromOldTemplate boolean Xác định có kế thừa từ mẫu cũ không
IsTicketCode boolean (trong JSON, không có trong bảng mẫu)
```

### 14.3. InvoiceData Dữ liệu hóa đơn).......................................................................................

```
Tên trường Kiểu dữ
liệu
```
```
Bắt buộc Diễn giải
```
**Dữ liệu chung**

RefID guid x Mã tham chiếu của hóa đơn (check trùng
theo key này, lưu lại để xử lý nghiệp vụ về
sau)

InvSeries string x Ký hiệu hóa đơn

InvDate string x Ngày phát hành hóa đơn (định dạng
yyyy-MM-dd)

CurrencyCode string Mã tiền tệ (mặc định VND

ExchangeRate decimal Tỷ giá (mặc định 1

PaymentMethodName string x Hình thức thanh toán (mặc định TM/CK

IsInvoiceSummary boolean x True: nếu hóa đơn gửi theo bảng tổng
hợp (đối với hóa đơn không mã, lấy từ api
template) mặc định false

IsSendEmail bool Có gửi email khi phát hành thành công
không ( **Chỉ thực hiện gửi ở Product** )

ReceiverName string Tên người nhận email

ReceiverEmail string Địa chỉ nhận email, phân tách bởi dấu ;
(chấm phảy)

SellerShopCode string Mã cửa hàng

SellerShopName string Tên cửa hàng

**Dữ liệu về người mua**

BuyerCode string Mã của người mua

BuyerLegalName string x (nếu có
BuyerTaxCo
de)

```
Tên đơn vị
```
BuyerTaxCode string Mã số thuế của người mua

BuyerAddress string x (nếu có
BuyerTaxCo
de)

```
Địa chỉ của người mua
```
BuyerFullName string Họ và tên của người mua

BuyerPhoneNumber string Số điện thoại của người mua

BuyerEmail string Email của người mua (chỉ được phép 1
email)

BuyerBankAccount string Số tài khoản ngân hàng của người mua

BuyerBankName string Tên ngân hàng của người mua

BuyerIDNumber string Số định danh cá nhân
Một chuỗi gồm 12 ký tự dạng số, không


```
validate theo cấu trúc chi tiết , Không bắt
buộc
Chỉ sử dụng ở ND70
```
BuyerPassport string Số hộ chiếu
Cho phép nhập dạng chuỗi gồm 20 ký tự
**Chỉ sử dụng ở ND70**

BuyerBudgetCode string Mã số đơn vị có quan hệ với Ngân sách
(gồm 7 ký tự). Khi nhập MSĐVCQHVNS
người mua thì bắt buộc nhập Tên đơn vị,
Địa chỉ
**Chỉ sử dụng ở ND70**

BuyerSalesChannel string Kênh bán hàng, tối đa 255 ký tự

BuyerShopName string Tên gian hàng, tối đa 255 ký tự

BuyerOrderCode string Mã đơn hàng, tối đa 255 ký tự

**Dữ liệu về tổng tiền**

TotalSaleAmountOC decimal x Tổng số tiền bán hàng chưa thuế, chưa
chiết khấu - nguyên tệ
Sum(AmountOC, itemType = 1 -
Sum(AmountOC, itemType = 3

TotalSaleAmount decimal x Tổng số tiền bán hàng - quy đổi
= TotalSaleAmountOC * ExchangeRate

TotalDiscountAmountOC decimal x Tổng số tiền chiết khấu - nguyên tệ
Sum(DiscountAmountOC, itemType= 1
+ Sum(AmountOC, itemType= 3

TotalDiscountAmount decimal x Tổng số tiền chiết khấu - quy đổi
= TotalDiscountAmountOC
ExchangeRate

TotalAmountWithoutVATOC decimal x Tổng số tiền chưa bao gồm VAT, đã trừ
chiết khấu - nguyên tệ
= Sum(AmountWithoutVATOC, itemtype
1 -
Sum(AmountWithoutVATOC, itemtype 3

TotalAmountWithoutVAT decimal x Tổng số tiền chưa bao gồm VAT - quy đổi
= TotalAmountWithoutVATOC *
ExchangRate

TotalVATAmountOC decimal x Tổng số tiền thuế - nguyên tệ
Sum(VATAmountOC, itemType = 1 -
Sum(VATAmountOC, itemType 3

TotalVATAmount decimal x Tổng số tiền thuế - quy đổi
TotalVATAmountOC * ExchangRate

TotalAmountOC decimal x Tổng tiền thanh toán - nguyên tệ
= TotalAmountWithoutVATOC +
TotalVATAmountOC

TotalAmount decimal x Tổng tiền thanh toán - quy đổi


```
= TotalAmountOC * ExchangRate
```
TotalAmountInWords string x Tổng số tiền bằng chữ (không truyền sẽ
lấy theo đọc tiền bằng chữ do MISA quy
định)

TotalAmountInWordsByEN
G

```
string Tổng tiền bằng chữ tiếng anh
```
TotalAmountInWordsUnsign
NormalVN

```
string Số tiền bằng chữ không dấu
```
**Dữ liệu về trường mở rộng**

CustomField1 string Trường tuỳ chỉnh 1

CustomField2 string Trường tuỳ chỉnh 2

CustomField3 string Trường tuỳ chỉnh 3

CustomField4 string Trường tuỳ chỉnh 4

CustomField5 string Trường tuỳ chỉnh 5

CustomField6 string Trường tuỳ chỉnh 6

CustomField7 string Trường tuỳ chỉnh 7

CustomField8 string Trường tuỳ chỉnh 8

CustomField9 string Trường tuỳ chỉnh 9

CustomField10 string Trường tuỳ chỉnh 10

**Dữ liệu về điện/nước (sử dụng với hóa đơn điện nước) - Chỉ sử dụng với hóa đơn đặc thù**

PeriodName string Kỳ thanh toán (sử dụng với hóa đơn
điện/nước)

PeriodFromDate DateTime Từ ngày

PeriodToDate DateTime Đến ngày

NewIndex decimal Chỉ số mới

OldIndex decimal Chỉ số cũ

AmountOfConsumption decimal Tổng điện tiêu thụ (sử dụng với hóa đơn
điện)

WaterUsed decimal Tổng số nước thiêu thụ (sử dụng với hóa
đơn nước)

EnvironmmentFeeRate decimal % Phí bảo vệ môi trường

EnvironmmentFeeAmount decimal Tiền phí bảo vệ môi trường

ClockInfos List<Clock
Infos>

```
Danh sách đồng hồ
```
**Dữ liệu về khách sạn (sử dụng với hóa đơn khách sạn) - Chỉ sử dụng với hóa đơn đặc thù**

CheckIn DateTime Ngày đến

CheckOut DateTime Ngày đi


ExciseTaxRate decimal thuế suất tiêu thụ đặc biệt

ServiceFeeRate decimal Max của ServiceFeeRate ở Details

ServiceAmountOC decimal Tổng tiền phí dịch vụ - nguyên tệ

ServiceAmount decimal Tổng tiền phí dịch vụ - quy đổi

ExciseTaxRate decimal Max của ExciseTaxRate ở Details

ExciseTaxAmountOC decimal Tổng tiền thuế TTDB - nguyên tệ

ExciseTaxAmount decimal Tổng tiền thuế TTDB - quy đổi

AccountObjectIdentificatio
nNumber

```
string Căn cước công dân
```
**Dữ liệu về hóa đơn Điều chỉnh/thay thế (chỉ bổ sung nếu phát hành hóa đơn thay thế/ điều
chỉnh)**

ReferenceType int Tính chất hóa đơn
1 thay thế
2 điều chỉnh

OrgInvoiceType int Loại hóa đơn bị thay thế/điều chỉnh
1 Hóa đơn NĐ 123
3 Hóa đơn NĐ 51

OrgInvTemplateNo string Mẫu số hóa đơn bị thay thế/điều chỉnh
VD ký hiệu của hóa đơn bị thay thế là
1C24MAA
thì OrgInvTemplateNo = 1 (ký hiệu đầu
tiên)

OrgInvSeries string Ký hiệu hóa đơn bị thay thế/điều chỉnh
VD ký hiệu của hóa đơn bị thay thế là
1C24MAA
thì OrgInvSeries = C24MAA (bỏ ký hiệu
đầu tiên)

OrgInvNo string Số hóa đơn bị thay thế/điều chỉnh

OrgInvDate string Ngày hóa đơn bị thay thế/điều chỉnh

InvoiceNote string Lý do thay thế/điều chỉnh

**Dữ liệu về Phiếu xuất kho (chỉ bổ sung thêm khi phát hành PXK - Chỉ sử dụng với hóa đơn là
PXK**

StockOutLegalName string Tên Tên người xuất hàng)

StockOutTaxCode string Mã số thuế MST người xuất hàng)

InternalCommand string Lệnh điều động nội bộ

StockOutAddress string Địa chỉ Địa chỉ kho xuất hàng)

TransporterName string Tên người vận chuyển

Transport string Phương tiện vận chuyển

StockOutFullName string Họ và tên người xuất hàng

TransportContractCode string Hợp đồng số Hợp đồng vận chuyển)


StockInLegalName string Tên Tên người nhận hàng)

StockInTaxCode string Mã số thuế Mã số thuế người nhận hàng)

StockInFullName string Họ và tên người nhận hàng

StockInAddress string Địa chỉ Địa chỉ kho nhận hàng)

ContractCode string Mã số thuế MST người xuất hàng)

ContractDate DateTime Hợp đồng kinh tế ngày

InternalCommandNo string Lệnh điều động số

InternalCommandOwner string Lệnh điều động của

InternalCommandDate DateTime Lệnh điều động ngày

JournalMemo string Về việc

StockTotalAmountOC decimal Tổng tiền xuất kho nguyên tệ

StockTotalAmount decimal Tổng tiền xuất kho quy đổi

**Dữ liệu về đối tượng liên quan**

OriginalInvoiceDetail List<Origi
nalInvoice
Detail>

```
x Danh sách hàng hóa (được mô tả theo
danh sách bên dưới)
```
TaxRateInfo List<TaxR
ateInfo>

```
x (bắt buộc
với hóa đơn
GTGT
```
```
Danh sách tổng hợp thuế suất (được mô
tả theo danh sách bên dưới)
```
OptionUserDefined OptionUse
rDefined

```
x (nếu không
truyền sẽ lấy
mặc định =
0
```
```
Thông tin thiết lập số thập phân (được mô
tả theo danh sách bên dưới)
```
OtherInfo List<Other
Info>

```
Chỉ sử dụng
với hóa đơn
đặc thù
```
```
Danh sách thông tin khác (được mô tả
theo danh sách bên dưới)
```
FeeInfo List<FeeIn
fo>

```
Chỉ sử dụng
với hóa đơn
đặc thù
```
```
Danh sách phí khác (được mô tả theo
danh sách bên dưới)
```

### 14.4. OriginalInvoiceDetail (dữ liệu dòng hàng hóa, dịch vụ)...............................................

```
Tên trường Kiểu dữ liệu Bắt buộc Diễn giải
```
**Dữ liệu chung về hàng hóa**

ItemType integer x Loại hàng hóa
1 Hàng hóa thường
2 Khuyến mại
3 Dòng hàng Chiết khấu thương mại
4 Ghi chú/diễn giải
5 Hàng hóa đặc thù vận tải

SortOrder integer x STT của dòng hàng (null với ItemType =
3 hoặc ItemType = 4 (bắt đầu từ 1

LineNumber integer x Vị trí của dòng hàng (bắt đầu từ 1

ItemCode string Mã hàng hóa

ItemName string x Tên hàng hóa

UnitName string Đơn vị tính

ExpiryDate DateTime Hạn sử dụng

ChassisNumber string Số khung

EngineNumber string Số máy

LotNo string Số lô

**Dữ liệu về tiền hàng**

Quantity decimal x Số lượng

UnitPrice decimal x Đơn giá trước thuế, trước chiết khấu

AmountOC decimal x Thành tiền trước thuế, trước chiết khấu -
nguyên tệ Quantity*UnitPrice)

Amount decimal x Thành tiền trước thuế, trước chiết khấu -
quy đổi

DiscountRate decimal x Tỷ lệ chiết khấu

DiscountAmountOC decimal x Tiền chiết khấu - nguyên tệ
AmountOCDiscountRate/100

DiscountAmount decimal x Tiền chiết khấu - quy đổi

AmountWithoutVATOC decimal x Thành tiền sau chiết khấu - nguyên tệ
AmountOC - DiscountAmountOC

AmountWithoutVAT decimal x Thành tiền sau chiết khấu - quy đổi

VATRateName string x Loại thuế suất:
KCT không chịu thuế
KKKNT kê khai nộp thuế
0% thuế suất 0%
5% thuế suất 5%
8% thuế suất 8%
10% thuế suất 10%


```
KHAC:x% loại thuế suất khác Ví dụ:
KHAC3.5%
Lưu ý:
nếu là dòng ghi chú, diễn giải
“ItemTypeˮ: 4 thì “VATRateNameˮ: null
```
VATAmountOC decimal x Tiền thuế - nguyên tệ
AmountWithoutVATOCVATRate/100

VATAmount decimal x Tiền thuế - quy đổi

WageAmount decimal Tiền công - quy đổi

WageAmountOC decimal Tiền công - nguyên tệ

WagePriceAmount decimal Đơn giá công

WagePriceDiscountAmount decimal Chiết khấu đơn giá công

WageDiscountAmountOC decimal Chiết khấu tiền công - nguyên tệ

InWard decimal Thực nhập (dùng với PXK

**Dữ liệu về thuế TTDB - Chỉ sử dụng với hóa đơn đặc thù**

ExciseTaxAmountOC decimal Tiền phí tiêu thụ đặc biệt - nguyên tệ
AmountOC - DiscountAmountOC +
ServiceAmountOC * ExciseTaxRate)/100

ExciseTaxAmount decimal Tiền phí tiêu thụ đặc biệt - quy đổi

ExciseTaxRate decimal % phí tiêu thụ đặc biệt

UnitPriceAfterExciseVAT decimal Đơn giá sau thuế tiêu thụ đặc biệt

AmountAfterExciseTaxOC decimal Thành tiền sau thuế tiêu thụ đặc biệt -
nguyên tệ

AmountAfterExciseTax decimal Thành tiền sau thuế tiêu thụ đặc biệt -
quy đổi

**Dữ liệu về Phí dịch vụ - Chỉ sử dụng với hóa đơn đặc thù khách sạn**

ServiceAmountOC decimal Tiền phí phục vụ - nguyên tệ
AmountOC - DiscountAmountOC *
ServiceFeeRate)/100

ServiceAmount decimal Tiền phí phục vụ - quy đổi

ServiceFeeRate decimal % phí phục vụ


### 14.5. TaxRateInfo (tổng hợp thuế suất).................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
VATRateName string Loại thuế suất:
KCT không chịu thuế
KKKNT không kê khai nộp thuế
0% thuế suất 0%
5% thuế suất 5%
8% thuế suất 8%
10% thuế suất 10%
KHAC:x% loại thuế suất khác Ví dụ: KHAC3.5%

AmountWithoutVATOC decimal Tổng tiền trước thuế của dòng hàng
Sum(AmountWithoutVATOC, itemtype 1 -
Sum(AmountWithoutVATOC, itemtype 3

VATAmountOC decimal Tổng tiền thuế của dòng hàng
Sum(VATAmountOC, itemType = 1 -
Sum(VATAmountOC, itemType 3

### 14.6. OtherInfo (thông tin khác).............................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
FieldName string Tên loại thông tin khác

DataType string Kiểu dữ liệu

FieldValue string Dữ liệu

### 14.7. FeeInfo (phí khác)..........................................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
FeeName string Tên loại phí

FeeAmountOC decimal Tiền phí


### 14.8. ClockInfo (thông tin về đồng hồ nước)..........................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
ClockOrder int STT dòng mặt hàng (bắt đầu từ 1

ClockCode string Mã đồng hồ

ClockSeri string Seri đồng hồ

LastIndex decimal Chỉ số mới

FirstIndex decimal Chỉ số cũ

Coefficient decimal Hệ số của đồng hồ

RefuelerNo string Xe tra nạp

LotNo string Số lô với hóa đơn thuốc

StartMeter decimal Số công tơ đầu

EndMeter decimal Số công tơ cuối

Temperature decimal Nhiệt độ

Density decimal Tỉ trọng

Gallon decimal Gallon

Liters decimal Số lít

ClockStatus int Trạng thái của đồng hồ 1 là thay mới đồng hồ ⇒
truyền thêm thông tin đồng hồ cũ vào các trường
có Old)

ClockCodeOld string Mã đồng hồ cũ

ClockSeriOld string Seri đồng hồ cũ

LastIndexOld decimal Chỉ số mới của đồng hồ cũ

FirstIndexOld decimal Chỉ số cũ của đồng hồ cũ

CoefficientOld decimal Hệ số của đồng hồ cũ


### 14.9. OptionUserDefined (thiết lập người dùng - hiển thị thập phân của tiền).....................

### tiền)

```
Tên trường Kiểu dữ liệu Diễn giải
```
MainCurrency string Mặc định luôn là VND

QuantityDecimalDigits string Số chữ số thập phân của số lượng

UnitPriceOCDecimalDigits string Số chữ số thập phân của đơn giá - nguyên tệ

UnitPriceDecimalDigits string Số chữ số thập phân của đơn giá - quy đổi

AmountOCDecimalDigits string Số chữ số thập phân của các loại dữ liệu tiền -
nguyên tệ

AmountDecimalDigits string Số chữ số thập phân của các loại tiền sau tính
toán - quy đổi

CoefficientDecimalDigits string Số chữ số thập phân của loại phần trăm

ExchangRateDecimalDigits string Số chữ số thập phân của tỷ giá

### 14.10. PublishInvoiceData Dữ liệu phát hành)......................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
RefID string Mã tham chiếu (truyền đúng RefID của hóa đơn ở
bước khởi tạo XML

TransactionID string Mã tra cứu (lấy ở bước tạo XML

InvSeries string Ký hiệu hóa đơn (lấy ở bước tạo XML

InvoiceData string Dữ liệu hóa đơn (dưới dạng XML đã ký ở bước ký)

IsInvoiceCalculatingMachine boolean Key đánh dấu hóa đơn có phát hành qua máy tính
tiền không
True: có

IsSendEmail boolean Có gửi hóa đơn khi phát hành thành công không

ReceiverName string Tên người nhận email

ReceiverEmail string Địa chỉ người nhận email, phân tách bằng ký tự ';'
(dấu chấm phảy)


### 14.11. InvoiceStatus (trạng thái hóa đơn)...............................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
RefID string Mã tham chiếu hóa đơn

InvSeries string Ký hiệu hóa đơn

InvTempl string Mẫu số hóa đơn

BuyerName string Tên đơn vị người mua

BuyerTaxCode string Mã số thuế người mua

BuyerCode string Mã người mua

BuyerFullName string Họ tên người mua

TransactionID string Mã tra cứu hóa đơn

PublishStatus int Trạng thái phát hành hóa đơn
1 Đã phát hành

EInvoiceStatus int Trạng thái hóa đơn:
1 hóa đơn gốc
2 hóa đơn bị hủy

```
3 hóa đơn thay thế
5 hóa đơn điều chỉnh
```
```
7 hóa đơn bị thay thế
8 hóa đơn bị điều chỉnh
```
ReferenceType int Tính chất hóa đơn
0 Gốc
1 Thay thế
2 Điều chỉnh
5 Chiết khấu thương mại

InvoiceCode string Mã của CQT

SourceType string Nguồn dữ liệu hệ thống phát hành

SendTaxStatus number Trạng thái gửi CQT
0 Chưa gửi CQT
1 Đã gửi CQT
2 HĐ hợp lệ Thuế chấp nhận)
3 HĐ không hợp lệ (thuế từ chối)
4 TĐ không hợp lệ

PublishedTime DateTime Thời gian phát hành

InvNo string Số hóa đơn

ReceiverStatus int Trạng thái nhận hóa đơn của người mua
0 chưa nhận
1 đã nhận

IsSendMail bool Có gửi email cho người mua không


### 14.12. Chứng thư số................................................................................................................

```
Tên trường Kiểu dữ liệu Diễn giải
```
TaxCode string Mã số thuế của Chứng thư số

CertificateSN string Mã chứng thư số (sử dụng ở bước phát hành)

UserName string Tài khoản Chứng thư số

AuthOrganizeName string Tên đơn vị chủ thể của Chứng thư số

EffectiveTime DateTime Thời gian bắt đầu có hiệu lực

ExpirationTime DateTime Thời gian hết hiệu lưc


## 15. Mã lỗi thường gặp..................................................................................................

```
Mã lỗi Mô tả Cách xử lý
InvalidAppID Sai thông tin AppID Liên hệ MISA để nhận AppID
InActiveAppID Ứng dụng ngừng theo
dõi
```
```
Liên hệ MISA để nhận AppID
```
```
UnAuthorize Sai mã token Đăng nhập lại để lấy token mới
TokenExpiredCode Token hết hạn Cần gọi hàm RefreshToken
InvalidTokenCode Token lỗi cần đăng
nhập lại
```
```
Đăng nhập lại để lấy token mới
```
```
DuplicateTemplateName Lỗi trùng tên mẫu Thay đổi tên mẫu hóa đơn
DuplicateTemplateNo Lỗi trùng ký hiệu Thay đổi ký hiệu mẫu hóa đơn
DuplicateRefID Trùng RefID Đổi RefID
InvoiceTemplateNotExist Mẫu hóa đơn không tồn
tại
```
```
Tạo mẫu hóa đơn/ kiểm tra lại thông tin
InvSeries
CreateInvoiceDataError Tạo XML hóa đơn lỗi
không xác định
InvoiceDetail_0 Nếu mã lỗi bắt đầu
bằng InvoiceDetail_0
thì thông tin có tên
trường dữ liệu phía sau
không hợp lệ
```
- Không thuộc loại được
cho phép
- Giá trị bắt buộc -
không được trống
- Vượt quá giới hạn
MaxLength cho phép

```
Lỗi thông tin nào kiểm tra thông tin đó,
kiểm tra về kiểu dữ liệu, số lượng ký tự,
tính đúng đắn của dữ liệu
```
```
StockInTaxCode_NotInfo_0_{
1
```
```
Nếu mã lỗi bắt đầu
bằng
StockInTaxCode_NotInf
o_0_1 thì thông tin
MST có nhưng thông tin
đơn vị không có
0 Loại phiếu xuất kho
1 thông tin thiếu
```
```
Lỗi thông tin nào kiểm tra thông tin đó,
kiểm tra về kiểu dữ liệu, số lượng ký tự,
tính đúng đắn của dữ liệu
```
```
RequireError_0 Nếu bắt đầu bằng
RequireError_0 thì
thông tin có tên trường
dữ liệu phía sau không
hợp lệ
```
- Không thuộc loại được
cho phép
- Giá trị bắt buộc -
không được trống

```
Lỗi thông tin nào kiểm tra thông tin đó,
kiểm tra về kiểu dữ liệu, số lượng ký tự,
tính đúng đắn của dữ liệu
```

- Vượt quá giới hạn
MaxLength cho phép

TaxRateInfo_VATRateName Tên loại thuế suất trong
Bảng tổng hợp thuế
suất của hóa đơn có dữ
liệu không hợp lệ

```
Bổ sung thêm thông tin tổng hợp thuế
suất TaxRateInfo
```
InvoiceQuantityTooLarge Số lượng hóa đơn gửi
lên trong 1 Request quá
số lượng cho phép

```
Nếu dữ liệu của 1 hóa đơn lớn, nên gửi
tối đa 30 hóa đơn/request
```
XMLTooLong File XML quá dài Nếu số lượng dòng hàng quá lớn, nên
tách thành nhiều hóa đơn, mỗi hóa đơn
200 dòng hàng

LicenseInfo_NotBuy Chưa mua tài nguyên Liên hệ MISA để đăng ký/ mua dịch vụ

LicenseInfo_OutOfInvoice Số lượng tài nguyên còn
lại không đủ để phát
hành toàn bộ các hóa
đơn gửi lên

```
Mua thêm tài nguyên phát hành hóa
đơn
```
LicenseInfo_Expired Tài nguyên chưa thanh
toán hoặc đã hết hạn

```
Liên hệ kinh doanh MISA để thanh
toán/cấp tài nguyên
```
InvalidTransactionID Mã tra cứu không hợp
lệ

```
Mã tra cứu được hệ thống MISA cung
cấp, KH không tự ý thay đổi
```
DuplicateTransactionID Trùng Mã tra cứu Thực hiện lại bước tạo hóa đơn để lấy
mã tra cứu mới (thường gặp khi ký số
qua tool)

SignatureEmpty Chữ ký số bị bỏ trống Thực hiện ký số (thường gặp khi ký số
qua tool)

InvalidSignature Chữ ký số không hợp lệ Kiểm tra lại chữ ký số với chữ ký số
đăng ký trên tờ khai được chấp thuận

CertRevocation Chữ ký số đã bị thu hồi Lỗi về CKS, liên hệ các bên liên quan

InvalidCertByRegistration Chữ ký số không tồn tại
trong tờ khai

```
Tạo tờ khai hoặc ký số hóa đơn đúng
với CKS đã đăng ký ở tờ khai
```
HasRegistrationStopUseCert Tồn tại tờ khai Ngừng
sử dụng chứng thư số

```
Tạo tờ khai hoặc ký số hóa đơn đúng
với CKS đã đăng ký ở tờ khai
```
SigningTimeNotInRegistration Ngày ký không thuộc
khoảng thời gian có
hiệu lực của chứng thư
số đã đăng ký với cơ
quan thuế và được CQT
chấp nhận

```
Tạo tờ khai hoặc ký số hóa đơn đúng
với CKS đã đăng ký ở tờ khai
```
InvalidXMLData XML không hợp lệ Kiểm tra lại định dạng của XML

InvalidInvNo Số hóa đơn không hợp
lệ

```
Số hóa đơn thường có 8 ký tự, do hệ
thống cấp tự động, người dùng k can
thiệp vào dữ liệu của những field liên
```

```
quan đến số hóa đơn
```
InvalidTaxCode Mã số thuế không hợp
lệ

```
Kiểm tra, tra cứu MST đang sử dụng
```
DuplicateInvoiceRefID Trùng RefID của hóa
đơn

- gọi đầu API lấy trạng thái hóa đơn
theo RefID ⇒ cập nhật lại trạng thái
xuống client (số hóa đơn, mã tra cứu,
mã CQT cấp, ...)

**InvoiceNumberNotCotinuous** Số hóa đơn không liên
tục

- PA chung là: gặp mã lỗi này thì retry
gọi lại hàm phát hành
- có 2 TH mã lỗi này thường gặp:
- TH1 kỹ thuật đơn vị dùng vòng lặp for
gửi request liên tục
⇒ Xử lý: Báo kỹ thuật xử lý mỗi khi phát
hành xong 1 request thì sleep khoảng
3s
- TH2 có nhiều điểm máy trạm phát
hành đồng thời
⇒ Xử lý: Tư vấn đơn vị mỗi 1 điểm máy
trạm phát hành sẽ theo 1 ký hiệu khác
nhau

SignSoftDream78Exception Lỗi ký số HSM Liên hệ MISA

SignEsignHSMError Lỗi ký số HSM Liên hệ MISA

CallSignServiceFail Lỗi ký số CKS của đơn vị không đáp ứng việc ký
số hóa đơn

**InvoiceDuplicated** Trùng hóa đơn - hóa
đơn đã được phát hành

- Giả sử khi gọi api phát hành lỗi nó
đang trả về số hóa đơn là 15
- PA chung là: gặp mã lỗi này thì retry
gọi lại hàm phát hành
⇒ Xử lý: trường hợp retry vẫn bị thì liên
hệ kỹ thuật MISA

Exception Thực hiện bị Exception

- Không rõ nguyên
nhân

```
Liên hệ kỹ thuật MISA để tìm hiểu
nguyên nhân
```
DeclarationNotExist Chưa tồn tại Tờ
khai/Thay đổi thông tin

```
Lập tờ khai và chờ CQT chấp nhận thì
thực hiện phát hành hóa đơn
```
InvalidDeclaration Chưa tồn tại Tờ
khai/Thay đổi thông tin
có trạng thái CQT chấp
nhận

```
Lập tờ khai và chờ CQT chấp nhận thì
thực hiện phát hành hóa đơn
```
ExistDeclarationNotReceive Tồn tại tờ khai có trạng
thái: Đã gửi CQT/ CQT
Tiếp nhận

```
Chờ CQT chấp nhận tờ khai
```
ExistsInvoiceNextYear Tồn tại hóa đơn của
năm tiếp theo

```
Lập hóa đơn/ gửi tờ khai mới với loại
hóa đơn sử dụng (thường liên quan đến
```

```
hóa đơn có mã/không mã)
```
InvoiceTemplateNotValidInDec
laration

```
Tờ khai/Thay đổi thông
tin không chứa loại hóa
đơn đang phát hành
```
```
Lập hóa đơn/ gửi tờ khai mới với loại
hóa đơn sử dụng (thường liên quan đến
hóa đơn có mã/không mã)
```
InvalidInvoiceDate Nếu ngày hóa đơn
không hợp lệ, nhỏ hơn
ngày của hóa đơn cuối
cùng đã phát hành

```
Ngày hóa đơn phải đáp ứng không được
nhỏ hơn ngày của số hóa đơn lớn nhất
theo ký hiệu đang sử dụng.
```
InvoiceCannotReplace Không thể thay thế hóa
đơn đã hủy

```
Sai về nghiệp vụ, không có phương án
xử lý
```
InvoiceCannotAdjust Không thể điều chỉnh
hóa đơn đã hủy/thay
thế

```
Sai về nghiệp vụ, không có phương án
xử lý
```
TaxReductionDateInValid Ngày hóa đơn giảm
thuế không hợp lệ

```
Ngày phát hành hóa đơn giảm thuế
không nằm trong thời gian giảm thuế
theo Quyết định
```
X509SubjectName Không có SubjectName
trên file XML đã ký

```
Lỗi về CKS, liên hệ các bên liên quan
```
X509Certificate Không có chứng thư số Lỗi về CKS, liên hệ các bên liên quan

InvoiceCannotReplaceByStatu
sNew

```
Hóa đơn gốc chưa được
hủy, không thể thay thế
```
```
Cần hủy hóa đơn gốc trước khi phát
hành/điều chỉnh hóa đơn
```
HasAdjustmentInvoice Hóa đơn đã được lập
hóa đơn điều chỉnh,
không thể thay thế

```
Đã tồn tại hóa đơn điều chỉnh/thay thế,
không thể thực hiện các nghiệp vụ điều
chỉnh/thay thế nữa
```
BuyerTaxCode_TaxCodeInvali
d

```
Mã số thuế người mua
hàng không hợp lệ
```
- Có thể người dùng nhập thừa dấu
cách ⇒ dùng hàm trim() cắt bỏ khoảng
trắng thừa
- Có thể do người dùng nhập ko đúng
định dạng mã số thuế ⇒ báo người
dùng nhập đúng định dạng

InvalidXMLContainEmoji trong dữ liệu phát hành
hóa đơn có truyền ký tự
đặc biệt (ví dụ biểu
tượng mặt cười)

- kiểm tra xem trong dữ liệu khách hàng
nhập có ký tự đặc biệt không? Nếu có
thì báo KH xóa ký tự này đi.

Không hỗ trợ hình thức
ký số


