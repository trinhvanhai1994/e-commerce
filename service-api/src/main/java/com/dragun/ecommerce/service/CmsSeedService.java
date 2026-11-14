package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.BlogPost;
import com.dragun.ecommerce.model.entity.HomepageSection;
import com.dragun.ecommerce.repository.BlogPostRepository;
import com.dragun.ecommerce.repository.HomepageSectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class CmsSeedService implements ApplicationRunner {

    private final HomepageSectionRepository sectionRepository;
    private final BlogPostRepository blogPostRepository;
    private final ObjectMapper objectMapper;

    /**
     * Deliberately not @Transactional: each save commits on its own, so one failing row cannot mark a shared
     * transaction rollback-only and abort application startup with UnexpectedRollbackException.
     */
    @Override
    public void run(ApplicationArguments args) {
        runStep("cms-seed/homepage-sections.json", () -> seedSections("cms-seed/homepage-sections.json"));
        runStep("cms-seed/page-sections.json", () -> seedSections("cms-seed/page-sections.json"));
        runStep("cms-seed/blog-posts.json", this::seedPosts);
    }

    @FunctionalInterface
    private interface SeedStep {
        void run() throws Exception;
    }

    private void runStep(String name, SeedStep step) {
        try {
            step.run();
        } catch (Exception e) {
            log.error("CMS seed {} failed: {}", name, e.getMessage(), e);
        }
    }

    /** Inserts only the (page_key, section_key) pairs missing from the DB; never overwrites admin edits. */
    private void seedSections(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("{} not found", path);
            return;
        }
        try (InputStream in = resource.getInputStream()) {
            List<JsonNode> nodes = objectMapper.readValue(in, new TypeReference<>() {});
            int inserted = 0;
            for (JsonNode node : nodes) {
                String pageKey = node.path("page_key").asText(ContentService.PAGE_HOME);
                String key = node.path("section_key").asText();
                if (sectionRepository.existsByPageKeyAndSectionKey(pageKey, key)) {
                    continue;
                }
                Map<String, Object> extra = objectMapper.convertValue(
                    node.path("extra_json"),
                    new TypeReference<Map<String, Object>>() {}
                );
                if (extra == null) {
                    extra = new HashMap<>();
                }
                boolean useDefaultDesktop = !node.has("use_default_desktop") || node.path("use_default_desktop").asBoolean(true);
                boolean useDefaultMobile = !node.has("use_default_mobile") || node.path("use_default_mobile").asBoolean(true);
                HomepageSection section = HomepageSection.builder()
                    .pageKey(pageKey)
                    .sectionKey(key)
                    .title(textOrNull(node, "title"))
                    .body(textOrNull(node, "body"))
                    .ctaText(textOrNull(node, "cta_text"))
                    .ctaLink(textOrNull(node, "cta_link"))
                    .useDefaultDesktop(useDefaultDesktop)
                    .useDefaultMobile(useDefaultMobile)
                    .imageDesktop(useDefaultDesktop ? null : textOrNull(node, "image_desktop"))
                    .imageMobile(useDefaultMobile ? null : textOrNull(node, "image_mobile"))
                    .extraJson(extra)
                    .enabled(!node.has("enabled") || node.path("enabled").asBoolean(true))
                    .sortOrder(node.path("sort_order").asInt(0))
                    .updatedAt(OffsetDateTime.now())
                    .updatedBy("seed")
                    .build();
                try {
                    sectionRepository.save(section);
                    inserted++;
                } catch (DataIntegrityViolationException e) {
                    log.warn("CMS seed skipped section {}/{}: {}", pageKey, key, e.getMostSpecificCause().getMessage());
                }
            }
            log.info("CMS seed {} inserted={}", path, inserted);
        }
    }

    private void seedPosts() throws Exception {
        ClassPathResource resource = new ClassPathResource("cms-seed/blog-posts.json");
        if (!resource.exists()) {
            log.warn("cms-seed/blog-posts.json not found");
            return;
        }
        try (InputStream in = resource.getInputStream()) {
            List<JsonNode> nodes = objectMapper.readValue(in, new TypeReference<>() {});
            int inserted = 0;
            for (JsonNode node : nodes) {
                String slug = node.path("slug").asText();
                // Soft-deleted rows count too: a post the admin deleted must not be re-seeded on the next restart.
                if (blogPostRepository.existsBySlug(slug)) {
                    continue;
                }
                List<Map<String, Object>> content = objectMapper.convertValue(
                    node.path("content_json"),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                Boolean showOnHome = node.path("show_on_home").asBoolean(false);
                Integer homeOrder = node.hasNonNull("home_order") ? node.path("home_order").asInt() : null;
                OffsetDateTime publishedAt = OffsetDateTime.now();
                if (node.hasNonNull("published_at")) {
                    publishedAt = OffsetDateTime.parse(node.path("published_at").asText().replace("Z", "+00:00"));
                }
                BlogPost post = BlogPost.builder()
                    .slug(slug)
                    .title(node.path("title").asText())
                    .excerpt(node.path("excerpt").asText())
                    .coverImage(textOrNull(node, "cover_image"))
                    .contentJson(content != null ? content : List.of())
                    .status(node.path("status").asText("PUBLISHED"))
                    .showOnHome(showOnHome)
                    .homeOrder(showOnHome ? homeOrder : null)
                    .publishedAt(publishedAt)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .createdBy("seed")
                    .updatedBy("seed")
                    .build();
                try {
                    blogPostRepository.save(post);
                    inserted++;
                } catch (DataIntegrityViolationException e) {
                    // e.g. home_order already taken by an admin-managed post
                    log.warn("CMS seed skipped post {}: {}", slug, e.getMostSpecificCause().getMessage());
                }
            }
            log.info("CMS seed posts inserted={}", inserted);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String v = node.get(field).asText();
        return v == null || v.isBlank() ? null : v;
    }
}
