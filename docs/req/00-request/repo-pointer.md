# Repo pointer

Nguồn code tham chiếu cho bộ docs requirement (cùng git repo `e-commerce`):

- `service-api/`: backend Spring Boot (Java), PostgreSQL + Flyway.
- `service-ui/`: frontend Vue 3 + Vite + Tailwind (storefront + trang admin).

Phần CMS trang chủ và blog (chưa commit tại 2026-11-14):

- BE: `service-api/src/main/java/com/dragun/ecommerce/{cms,service/ContentService,service/BlogPostService,service/CmsImageService,service/CmsSeedService,controller/admin/AdminHomepageController,controller/admin/AdminBlogPostController,controller/external/ContentController}`
- DB: `service-api/src/main/resources/db/migration/V16..V18`, seed `service-api/src/main/resources/cms-seed/`
- FE: `service-ui/src/views/{Home,AdminHomepage,AdminBlogList,AdminBlogEditor,Blog,ArticleDetail}.vue`, `service-ui/src/content/homeDefaults.js`, `service-ui/src/components/cms/CmsImageUploader.vue`, `service-ui/src/App.vue` (logo)
