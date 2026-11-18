# Thi Yên E-commerce

Monorepo gồm backend, frontend và bộ tài liệu yêu cầu.

| Thư mục | Nội dung | Bắt đầu |
|---|---|---|
| [`service-api/`](service-api/README.md) | Backend Spring Boot 3.2, Java 17, PostgreSQL + Flyway. Chạy tại `http://localhost:5678` | `service-api/README.md`, `service-api/docs/` |
| [`service-ui/`](service-ui/README.md) | Frontend Vue 3 + Vite + Tailwind (storefront + admin). Chạy tại `http://localhost:5173` | `service-ui/README.md`, `service-ui/docs/` |
| [`docs/req/`](docs/req/CLAUDE.md) | Tài liệu yêu cầu theo 6 tầng (nguồn → phân tích → nghiệp vụ → SRS → thiết kế) | `docs/req/CLAUDE.md` |

## Chạy local

1. Database + backend: theo `service-api/README.md` (JDK 17, PostgreSQL, `scripts/run/run-dev.*`).
2. Frontend: `cd service-ui && npm install && npm run dev`.

## Luồng làm việc

Yêu cầu (`docs/req`) → thiết kế (`docs/req/05-design`) → backend (`service-api`) → frontend (`service-ui`). Chi tiết cho người và AI agent: [CLAUDE.md](CLAUDE.md).
