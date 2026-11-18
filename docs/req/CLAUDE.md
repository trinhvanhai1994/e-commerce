---
type: req-docs-guide
title: "Hướng dẫn bộ tài liệu requirement (đọc trước khi ghi)"
status: reviewed
updated: 2026-11-14
---

# Hướng dẫn bộ tài liệu requirement

> File này là tài liệu hướng dẫn chính cho mọi người và mọi AI agent làm việc trong bộ docs requirement này.
> Đọc trước khi tạo hay sửa bất kỳ tài liệu nào. Mục tiêu: giữ cấu trúc, quy ước ID, provenance,
> và văn phong nhất quán để tài liệu dùng lại được và truy vết được.

## A. Cây 6 tầng và chủ sở hữu từng tài liệu

Bộ docs có 6 tầng cố định (`00` đến `05`), sắp theo workflow và độ trừu tượng (thô, làm việc,
nghiệp vụ, hệ thống, thiết kế). Số thứ tự KHÔNG bắt làm tuần tự hay làm đủ. Mỗi path có **đúng một
chủ sở hữu ghi**; nơi khác chỉ đọc. Cách xếp file, subfolder trong mỗi tầng là **linh hoạt do
người phụ trách quyết** (dự án nhỏ để phẳng; dự án lớn tách theo loại hoặc theo domain). Phần cố
định là 6 tầng + chủ-sở-hữu-từng-tài-liệu.

```
docs-root/
├── 00-request/            RAW: nguồn thô, team tự tổ chức. Dev/QA KHÔNG đọc. Gốc mọi sources:.
│   ├── customer-docs/     (RFP, hearing, mail, CR nguyên văn khách)
│   ├── meetings/          (transcript, notes buổi họp)
│   └── code/ hoặc repo-pointer.md
│
├── 01-wip/                WORKING: BA/BrSE làm song song. Dev/QA KHÔNG đọc.
│   ├── task-<nnn>-<slug>/ (mỗi work item 1 task folder; file tạm disposable)
│   ├── qna/               (chủ: qna. Q&A xuyên tầng; Q-ID toàn cục hoặc Q-<DOMAIN>-xxx)
│   ├── wireframes/        (chủ: screen-wireframe. lo-fi + screen-flow.md; trung gian)
│   └── mockups/           (chủ: screen-mockup. hi-fi + style-guide.md; trung gian)
│
├── 02-shared/             STABLE: dùng chung mọi tầng
│   ├── glossary.md        (chủ: glossary. harvest term + curate)
│   ├── assumptions.md     (chủ: assumption. A-xxx, chờ khách)
│   └── decisions/         (chủ: decision-log. D-xxx; folder, nhiều log)
│
├── 03-business/           STABLE: nghiệp vụ (chủ ngữ: nghiệp vụ)
│   ├── brd.md             (chủ: brd)
│   ├── conceptual-model.md(chủ: conceptual-model. thực thể + quan hệ mức khái niệm, neo tên canonical)
│   ├── stakeholders.md    (chủ: stakeholder. SH-xx toàn cục, 1 file, không split domain)
│   ├── flows/<slug>.md    (chủ: business-flow)
│   ├── functions/<domain>.md (chủ: business-function. BF-<DOMAIN>-xxx; hệ nhỏ: functions.md)
│   └── business-rules.md  (chủ: business-rule. BR-xxx)
│
├── 04-system/             STABLE: SRS, yêu cầu hệ thống (chủ ngữ: hệ thống)
│   └── srs.md             (chủ: srs. REQ-F/N-xxx; hệ lớn: srs/<domain>.md)
│
└── 05-design/             STABLE: thiết kế cơ bản (mức How)
    ├── screens/<slug>.md  (chủ: screen-design. DSN-S)
    ├── reports/<slug>.md  (chủ: report-design. DSN-R)
    ├── apis/<slug>.md     (chủ: api-design. DSN-A)
    ├── data-model.md      (chủ: data-model. DSN-T)
    ├── batches/<slug>.md  (chủ: batch-design. DSN-B)
    ├── nfr.md             (chủ: nfr-design. DSN-G)
    ├── permissions.md     (chủ: permission-design. DSN-P)
    ├── codes.md           (chủ: code-definition. DSN-C)
    └── architecture.md    (chủ: system-architecture)
```

Bố cục thư mục trong mỗi tầng là sample, có thể tinh chỉnh; điều bất biến là 6 tầng, chủ sở hữu
từng tài liệu, và up-link `sources:` ở khối B.

## B. Luật cốt lõi

### B1. Provenance: khách nói vs mình suy vs mình đề xuất

Kỷ luật hạng nhất. Mọi phát biểu phải phân biệt được nó ở đâu ra:

- **Có nguồn** (khách hoặc tài liệu nói thẳng): mặc định, không tag, kèm neo `sources:`.
- **Mình suy ra**: tag `[INFERRED]` inline; nếu trọng yếu thì thêm `A-xxx` hoặc `Q-xxx`.
- **Mình đề xuất** (chưa ai chốt): tag `[PROPOSED]` inline; nếu trọng yếu thì thêm `D-xxx` hoặc `Q-xxx`.

Không được trộn ba loại. Trộn khách-nói với mình-suy là mất khả năng hỏi lại khách, hỏng nặng
nhất của một bộ tài liệu. Thiếu thông tin thì để stub có đánh dấu (`[TBD]` + `Q-xxx`), KHÔNG bịa đặt số,
tên, hay quy tắc.

### B2. Bộ tứ Q/A/C/D khi gặp chỗ chưa chắc

- **C** (constraint): khách đã nói rõ và cố định (deadline, luật, công nghệ khách chỉ định). Ghi
  thẳng `brd`, không ID riêng.
- **Q-xxx** (`qna`): cần khách trả lời mới biết.
- **A-xxx** (`assumptions`): phải đi tiếp ngay khi chưa có đáp, giữ giả định tạm gắn với Q.
- **D-xxx** (`decisions`): mình chọn giữa các phương án có lý do. `decisions/` là zone CÓ CỔNG, mọi
  thao tác `D` đi qua chủ sở hữu, nơi khác chỉ gắn cờ "nên log D".

### B3. Status và ranh dev/QA đọc

- Deliverable tầng `02` đến `05`: `draft` → `reviewed` → `deprecated`.
- WIP `01-wip` (task đang phân tích): `todo` → `doing` → `done`.
- Ngoại lệ: `qna/` là tài liệu log gửi khách nên dùng `draft` → `reviewed` → `deprecated`.
  `wireframes/` + `mockups/` là trực quan trung gian không official nên dùng `draft` → `deprecated`
  (BỎ `reviewed`).
- **Ranh dev đọc:** `00-request/` và `01-wip/` là hậu trường, dev/QA KHÔNG đọc. Dev/QA chỉ code và
  test từ tài liệu có `status: reviewed` trở lên. Ranh này là field `status`, KHÔNG phải vị trí file.

### B4. Up-link sources: (traceability nhẹ)

Mỗi tầng dưới neo nguồn ngược LÊN heading tầng trên bằng link markdown tương đối, không trỏ xuống.
Ví dụ `srs.md#REQ-F-034` là Linked REQ của một `DSN-S` trong `screens/`. Đọc để hiểu thì tham chiếu lên
nhiều tầng tùy nhu cầu (ghi hết vào `sources:`); nhưng link truy vết phải trỏ chủ sở hữu canonical,
chủ chưa có thì tạo `Q-xxx` chứ không trỏ tạm cho khớp. Gốc rễ mọi `sources:` luôn là một file trong
`00-request/`.

## C. Quy ước ID

**Nguyên tắc:** ID tầng nghiệp vụ, hệ thống, thiết kế mang tiền tố domain khi dự án chia domain; ID
dùng chung xuyên domain thì luôn toàn cục, không tiền tố. `domain` là phân vùng dẫn xuất từ nghiệp
vụ; mã domain viết HOA trong ID (`ORD`, `INV`), thư mục viết thường (`order/`). Dự án nhỏ chưa chia
domain dùng ID phẳng `<TYPE>-xxx`; hệ lớn dùng `<TYPE>-<DOMAIN>-xxx`, số chạy riêng theo domain,
đổi file không đổi số. Người phụ trách quyết cây domain, không tự tạo mã domain.

| ID | Tài liệu sở hữu | Phạm vi |
|---|---|---|
| `B-xx`, `BF-xxx`, `K-xx` (`-<DOMAIN>-`) | `functions*.md` (danh mục nghiệp vụ, chức năng, vấn đề) | nhỏ: toàn cục; lớn: theo domain. `BF` là điểm neo canonical cho REQ trỏ lên |
| `BR-xxx` (`BR-<DOMAIN>-xxx`) | `business-rules.md` | nhỏ: toàn cục; lớn: theo domain |
| `REQ-F-xxx`, `REQ-N-xxx` (`REQ-<DOMAIN>-F-xxx`) | `srs.md` | nhỏ: toàn cục; lớn: theo domain |
| `SCR-xxx`, `RPT-xxx`, `IF-xxx` | `srs.md` (yêu cầu màn hình, báo cáo, giao diện ngoài, mức yêu cầu) | 05-design trỏ lên; thêm `<DOMAIN>` khi tách |
| `DSN-S/R/A/T/B/G/P/C-xxx` (`DSN-<DOMAIN>-S-xxx`) | `05-design/*` | S=screen, R=report, A=api, T=table, B=batch, G=nfr, P=permission, C=code |
| `Q-xxx` (`Q-<DOMAIN>-xxx`) | `qna/` | + category `business`/`system`/`design`/`cross`; câu shared dùng `Q-cross-xxx` |
| `A-xxx` | `assumptions.md` | **toàn cục** (xuyên domain, không tiền tố) |
| `D-xxx` | `decisions/` | **toàn cục** (xuyên domain, không tiền tố) |
| `SH-xx` | `stakeholders.md` | **toàn cục** (ngoại lệ trong tầng business: 1 file, không split domain) |

- `glossary` KHÔNG có ID số: term canonical chính là key.
- `conceptual-model` KHÔNG có ID số: neo bằng tên thực thể canonical (anchor).
- `wireframes/` và `mockups/` KHÔNG có ID riêng: danh tính là slug (tên file), gắn `Linked: DSN-S-xxx`
  khi màn đã có spec.
- KHÔNG tự tạo ID convention mới ngoài bảng này. Thấy thiếu thật thì nêu ở tầng bộ skill, không chắp vá.

## D. Ngôn ngữ và văn phong

- `00-request/` (nguồn khách): giữ nguyên bản gốc.
- Từ `01` trở đi (gồm mọi deliverable): tài liệu nội bộ, **tiếng Việt là chính**.
- Frontmatter, `type`, ID (`REQ`, `DSN`...), tên file: tiếng Anh.
- Dùng thuật ngữ tiếng Anh khi nó chuẩn và gọn hơn bản dịch (batch, cache, deadline, stakeholder).
- Thuật ngữ trong nguồn ngoại ngữ: giữ gốc kèm tạm dịch lần đầu; `glossary.md` chốt bản canonical.
- **Không dùng tiếng Nhật trong deliverable** trừ khi có yêu cầu (bản giao khách tiếng Nhật ngoài
  phạm vi mặc định).
- Văn phong: rõ ràng, ngắn gọn, **ưu tiên bullet**, một ý một dòng. Hạn chế jargon sâu.
- **Không dùng em-dash.** Thay bằng dấu phẩy, hai chấm, ngoặc đơn, hoặc tách câu. Dấu gạch nối trong
  ID hoặc khoảng số vẫn dùng bình thường.
- Tránh từ sáo rỗng (vô cùng, mạnh mẽ, liền mạch), câu đệm thừa (cần lưu ý rằng), và lặp ý.

<!-- BEGIN project-rules -->
## E. Quy tắc riêng của dự án này

(Chưa có quy tắc riêng nào được ghi. Điền các quy ước, ràng buộc, hoặc lưu ý riêng cho bộ docs này
khi khởi tạo. Để trống nếu chưa có; không bịa đặt nội dung.)
<!-- END project-rules -->

## Lịch sử

- 2026-11-14: Khởi tạo từ skill init-doc-folder (chưng cất từ chuẩn docs-structure + language-policy).
