# Thi Yên E-commerce: agent context
> Generated from 621a5a9 (2026-11-14) by project-onboard, including uncommitted working-tree changes (CMS homepage + blog).
> This is a cache of the codebase, not a specification. Verify anything load-bearing
> against the code before relying on it, and refresh with: "refresh docs/ai".

## What this is
Cửa hàng online bán thực phẩm dưỡng sinh (bột ngũ cốc, "bữa ăn thay thế") thương hiệu Thi Yên: storefront cho khách xem sản phẩm, đặt hàng (COD/checkout, địa chỉ tỉnh/huyện/xã VN), đọc blog; trang admin quản lý sản phẩm, đơn hàng, người dùng, nội dung trang chủ và blog (CMS). Backend đồng bộ sản phẩm/đơn với Pancake POS (`integration/pancake`). Một git repo, hai service: `service-api` (Spring Boot) và `service-ui` (Vue).

## Project profile
```
REPO=e-commerce (single git repo, MULTI_REPO=false), GIT_HOST=github, BASE=main
PROTECTED=main,prod,production,stg,staging,release/*,prod/*,stg/*,staging/*
CI=none

service-api/
  LANG=Java 17, FRAMEWORK=Spring Boot 3.2 (web, webflux client, data-jpa, security, oauth2-client, validation), Lombok, jjwt
  DB=PostgreSQL, MIGRATIONS=Flyway (src/main/resources/db/migration, V1..V18), seed CMS: CmsSeedService + resources/cms-seed/*.json
  BUILD=mvn (needs JDK 17; machine default java is 8 -> set JAVA_HOME), PORT=5678, profiles: dev/test/prod (application-*.yml)
  TEST_CMD=mvn test (37 tests, ~85s, needs Docker: Testcontainers postgres:15 via TestcontainersConfiguration + profile "it"), LINT=not configured
  DOCKER=service-api/Dockerfile, service-api/docker-compose.yml; scripts/{build,run,setup}/*.ps1|*.sh

service-ui/
  LANG=JavaScript, FRAMEWORK=Vue 3.4 (Composition API, <script setup>), Vite 5, Vue Router 4, Pinia, Tailwind 3
  PKG=npm, DEV=npm run dev (port 5173), BUILD=npm run build | build:prod | build:deploy
  TEST=not configured, LINT=eslint script present but no config (not usable), FORMAT=npm run format (prettier)
```

## Entry points
| Kind | Where | Notes |
|---|---|---|
| HTTP API | `service-api/.../EcommerceApplication.java`, controllers in `controller/{external,admin,public,publicapi}` | see 20-api.md |
| Scheduler | `integration/pancake/scheduler/PancakeSyncScheduler.java` | Pancake sync |
| Startup runners | `service/CmsSeedService.java` (ApplicationRunner), `service/LocationDataInitService.java` | seed CMS, location data |
| SPA | `service-ui/src/main.js`, routes `service-ui/src/router/index.js` | public + `/admin/*` |

## Module map
| Path | Responsibility | Depends on |
|---|---|---|
| `service-api/.../controller/external` | API public cho storefront (product, order, location, content) | service |
| `service-api/.../controller/admin` | API admin (`ROLE_ADMIN`) | service |
| `service-api/.../service` | nghiệp vụ, transaction | repository, integration |
| `service-api/.../repository`, `model/entity` | JPA | PostgreSQL |
| `service-api/.../security`, `config` | JWT filter, CORS, security chain | |
| `service-api/.../integration/pancake` | client + mapper + sync Pancake POS | WebClient |
| `service-api/.../cms` | `ImageSlot` (quy cách ảnh CMS) | |
| `service-ui/src/views` | trang public + `Admin*.vue` | services, stores |
| `service-ui/src/services` | http client (adapters, interceptors) + `*.service.js` theo domain | service-api |
| `service-ui/src/content` | nội dung/ảnh mặc định khi CMS lỗi | |

## Where new code goes
| Adding... | Goes in | Copy this reference |
|---|---|---|
| a public endpoint | `controller/external/` + service | `controller/external/ContentController.java` |
| an admin endpoint | `controller/admin/` with `@PreAuthorize("hasRole('ADMIN')")`, path `/api/dragun/admin/...` | `controller/admin/AdminHomepageController.java` |
| a business rule | service layer, never the controller | `service/BlogPostService.java` |
| a table | entity + Flyway `V<next>__*.sql` (never edit an applied migration) | `model/entity/BlogPost.java` + `V16__Create_cms_tables.sql` |
| a homepage CMS block | key in `ContentService.SECTION_KEYS` + `cms-seed/homepage-sections.json` (exact current content) + defaults in `service-ui/src/content/homeDefaults.js` + editor block in `AdminHomepage.vue` `BLOCKS` | section `footer` |
| a FE API call | `service-ui/src/services/*.service.js` | `services/content.service.js` |
| a transactional email | template `res/mail/<name>.html` (th:text only) + a service that builds the model and calls `MailSender`, triggered by an event handled `@Async @TransactionalEventListener(AFTER_COMMIT)` | `integration/mail/OrderMailService.java` + `OrderMailListener.java` |
| an admin page | `views/Admin*.vue` wrapped in `AdminLayout` + route with admin guard | `views/AdminHomepage.vue` |

## Gotchas: what a newcomer gets wrong here
- **Backend tests need Docker.** Context tests (`@SpringBootTest`) use `@ActiveProfiles("it")` + `@Import(TestcontainersConfiguration.class)`: a throwaway PostgreSQL 15, all Flyway migrations, Hibernate `ddl-auto: validate` (`src/test/resources/application-it.yml`), so an entity column missing from migrations fails `EcommerceApplicationTests`. Never use profile `test` in tests: that is the deployed TEST environment and reads DB credentials from env. `testcontainers.version` is pinned to 1.21.4 in `pom.xml` because Boot 3.2's 1.19.3 cannot talk to Docker Engine 29+. `@Tag("external")` tests (Pancake POS, real shop) are excluded by surefire. FE has no tests and no ESLint config.
- **JDK 17 required** but the machine default is Java 8: run Maven with `JAVA_HOME` pointing at a JDK 17.
- **Orders API is public.** `/api/extend/orders/**` is `permitAll` (`util/Constants.java:9-10`), including listing all orders and changing status; the admin UI calls these same routes. Do not copy this pattern.
- **Public route list lives in two places**: `Constants.PUBLIC_ENDPOINTS` and `JwtAuthenticationFilter.shouldNotFilter`; change both. Since task-003 the filter deliberately still runs on `/api/dragun/products/**` and `/api/extend/orders` (customer token needed for reviews and to attach orders), so the skip list is no longer a copy of the public list.
- **Two kinds of JWT** (task-003): admin (`users`) and customer (`customers`, claim `typ=customer`, `ROLE_CUSTOMER`, principal `CustomerPrincipal`). The storefront keeps the customer session in `localStorage['customerSession']` (`stores/auth.js`) and sends it only on customer calls; `localStorage['authToken']` stays the admin token that `httpClient` adds to every request (adapters now keep an `Authorization` header the caller sets). Customer calls go through `customerFetch` in `services/customer.service.js` and `authStore.authorized(fn)` (refresh on 401, then login popup). Gate a customer-only action with `authStore.requireLogin(action, reason)`.
- **401/403 bodies are written in `SecurityConfig.exceptionHandling`**, not via `/error`: the error dispatch re-runs the chain without the token (the filter runs once per request) and would turn every 403 into a 401. MockMvc tests do not do that dispatch, so check status codes on the running server too.
- **Bulk JPQL updates** (`@Modifying`) use `flushAutomatically = true, clearAutomatically = true`; otherwise an entity already loaded in the same transaction keeps stale values (seen with revoked refresh tokens).
- **CMS seed = live content.** `CmsSeedService` inserts only missing keys from `cms-seed/*.json`; the JSON must equal what the site shows today, so a deploy changes nothing until an admin saves. `content/page-sections.seed.json` (FE) is copied from the BE seed by `service-ui/scripts/sync-cms-seed.mjs` (npm `predev`/`prebuild`); edit the BE file. Blog posts have no FE copy: list/detail show an error with retry when the API fails, and the Home blog block hides. Story (`/me`) and philosophy (`/healthcare`) pages read `GET /content/pages/{pageKey}` via `composables/usePageContent.js`; their admin is `views/AdminPageContent.vue` (`/admin/pages/story|philosophy`).
- **Storefront fallbacks**: Home/App merge API sections over `homeDefaults.js`; a section absent from the API keeps its default, `enabled:false` hides it. Never filter disabled sections out of the public API.
- **`@PreUpdate` + optimistic lock**: CMS services use `saveAndFlush` and MICROS-truncated timestamps so the `updatedAt` returned equals the stored one (`ContentService.updateSection`).
- **Product reviews are published immediately, moderated after.** Logged-in customers (task-003; one review per product per account, author name from the profile) POST to `/api/dragun/products/{id}/reviews` (stored `APPROVED` with `approved_by = auto` since 2026-11-18, rate-limited in memory per IP, honeypot `website`); only `APPROVED` rows are public and feed the rating summary; admin hides (`HIDDEN`) or edits later. The public GET is cached 30s, so the storefront refetches with a `_` query param after a submit. Admin: `views/AdminProductReviews.vue` (`/admin/reviews`). Image slot `review_image` exists in both `ImageSlot.java` and `imageSlots.js`.
- **Order mails leave after commit, on another thread** (task-005): `OrderService` publishes `OrderPlacedEvent`; `OrderMailListener` (`@Async` + `@TransactionalEventListener(AFTER_COMMIT)`) calls `OrderMailService`, which reloads the order. A rolled-back order sends nothing, and a Brevo failure is only logged (no retry, no outbox). Without `MAIL_ENABLED=true`, `MAIL_FROM_EMAIL` and `BREVO_API_KEY`, `MailConfig` wires `LoggingMailSender` (dev, profile `it`). A `@TransactionalEventListener` must not itself be `@Transactional` (REQUIRED): keep the transactional work in a separate bean. Tests that need the listener cannot be `@Transactional` (no commit): see `OrderPlacedMailFlowTest`, which cleans up its orders.
- **Dev/test profiles use `ddl-auto: update`** with `validate-on-migrate: false`; prod uses `validate`. A column missing from migrations shows up only in prod.
- **`CmsImageUploader` default toggle is opt-in**: bind `v-model:use-default` only when the slot has a packaged default image (CMS pages). Left unbound (product main/gallery images) the "Dùng ảnh mặc định" checkbox is hidden and upload is always available; before 2026-11-18 the prop defaulted to `true`, so unticking on the product page left no upload button and saving failed.
- **Image upload never silently crops** (2026-11-18): `utils/imageSlotPipeline.js` loads and validates (`loadImageForSlot`), then renders onto the slot's exact output size (`renderImageForSlot`). An image whose ratio differs from the slot opens `components/cms/ImageCropDialog.vue` (drag, zoom, "Vừa toàn bộ ảnh" = fit with white/transparent padding, "Lấp đầy khung"); what the frame shows is what is saved. JPEG output gets a white background (transparent WebP used to turn black). Customer review photos use `processImageForSlot(file, 'review_image', 'fit')` (no dialog, never cropped).
- **Display frames must match the slot ratio**: render a slot image in a box of the same ratio (`aspect-square`, `aspect-[16/10]`...) with `object-cover`, or at natural height (`w-full h-auto`). A fixed `h-*` with `w-full object-cover` crops at most widths. `@tailwindcss/aspect-ratio` replaces the theme's aspect values, so `tailwind.config.js` re-adds `square`/`video`; before that `aspect-square`/`aspect-video` generated no CSS. Square items in a fixed-height flex column also need `flex-shrink-0`.
- **Default images resolve through the API host** (`getImageUrlFromApi`): `/images/...` defaults live in `service-ui/public/images`, so they appear broken when the API host does not serve them (local dev).

## Index
- `10-business.md`: domain entities, rules, state, authorization, known gaps (144 lines)
- `20-api.md`: 38 routes, auth, envelopes, FE callers, non-HTTP entry points (170 lines)
- `30-data-model.md`: tables, constraints, jsonb shapes, migrations V1-V18, ER diagram (219 lines)
- `40-conventions.md`: reference implementations, naming, layering, errors, open decisions (225 lines)
- `50-stack-patterns.md`: transactions, lifecycle hooks, locking, security, FE patterns, anti-patterns (127 lines)
- Not generated: `25-fe-be-contract.md` (single repo, MULTI_REPO=false; FE callers are listed per route in `20-api.md`). `docs/ai/governance/` is human-owned and not part of the pack.
- Requirements and specs: `../req/` (start at `../req/CLAUDE.md`).

## Known unknowns
- Production topology (which host serves `/images`, reverse proxy, CORS origins per env): not in repo; ask the team.
- Whether the commented-out tests are meant to be restored.
