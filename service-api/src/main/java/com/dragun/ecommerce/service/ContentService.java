package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.request.HomepageSectionRequest;
import com.dragun.ecommerce.model.dto.response.BlogPostResponse;
import com.dragun.ecommerce.model.dto.response.HomepageContentResponse;
import com.dragun.ecommerce.model.dto.response.HomepageSectionResponse;
import com.dragun.ecommerce.model.entity.HomepageSection;
import com.dragun.ecommerce.repository.BlogPostRepository;
import com.dragun.ecommerce.repository.HomepageSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContentService {

    public static final String PAGE_HOME = "home";

    /** Editable sections per public page. Keys outside this map are rejected. */
    public static final Map<String, Set<String>> PAGE_SECTIONS = Map.of(
        PAGE_HOME, Set.of("hero", "story", "quality", "meal_gallery", "floating_chat", "header_logo", "footer"),
        "story", Set.of("hero", "intro", "feature_1", "feature_2", "vision", "mission", "values", "nutrition"),
        "philosophy", Set.of("hero", "article", "principles", "food")
    );

    /** Max items of any list inside a story/philosophy section (paragraphs, cards, bullet items). */
    private static final int PAGE_MAX_LIST_ITEMS = 40;

    private static final int FOOTER_MAX_POLICY_LINKS = 8;
    private static final int FOOTER_MAX_SOCIALS = 6;
    private static final Set<String> FOOTER_SOCIAL_TYPES = Set.of("facebook", "tiktok", "youtube", "instagram", "zalo");

    private final HomepageSectionRepository sectionRepository;
    private final BlogPostRepository blogPostRepository;

    @Transactional(readOnly = true)
    public HomepageContentResponse getPublicHomepage() {
        // Disabled sections are returned with enabled=false so the storefront hides them instead of
        // filling the gap with its packaged defaults; a key absent here means "not in DB yet" → keep default.
        Map<String, HomepageSectionResponse> sections = getPublicPageSections(PAGE_HOME);
        List<BlogPostResponse> featured = blogPostRepository.findFeaturedForHome().stream()
            .limit(3)
            .map(BlogPostService::toSummaryResponse)
            .toList();
        return HomepageContentResponse.builder()
            .sections(sections)
            .featuredPosts(featured)
            .build();
    }

    /** All sections of a page keyed by section_key, disabled ones included (enabled=false hides them on the storefront). */
    @Transactional(readOnly = true)
    public Map<String, HomepageSectionResponse> getPublicPageSections(String pageKey) {
        requirePage(pageKey);
        Map<String, HomepageSectionResponse> sections = new LinkedHashMap<>();
        for (HomepageSection section : sectionRepository.findByPageKeyOrderBySortOrderAsc(pageKey)) {
            sections.put(section.getSectionKey(), toSectionResponse(section));
        }
        return sections;
    }

    @Transactional(readOnly = true)
    public List<HomepageSectionResponse> getAllSectionsAdmin() {
        return getAllSectionsAdmin(PAGE_HOME);
    }

    @Transactional(readOnly = true)
    public List<HomepageSectionResponse> getAllSectionsAdmin(String pageKey) {
        requirePage(pageKey);
        return sectionRepository.findByPageKeyOrderBySortOrderAsc(pageKey).stream()
            .map(this::toSectionResponse)
            .toList();
    }

    @Transactional
    public HomepageSectionResponse updateSection(String sectionKey, HomepageSectionRequest request, String username) {
        return updateSection(PAGE_HOME, sectionKey, request, username);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public HomepageSectionResponse updateSection(String pageKey, String sectionKey, HomepageSectionRequest request, String username) {
        if (!requirePage(pageKey).contains(sectionKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "section_key không hợp lệ");
        }
        HomepageSection section = sectionRepository.findByPageKeyAndSectionKey(pageKey, sectionKey)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section không tồn tại"));

        if (request.getUpdatedAt() == null || section.getUpdatedAt() == null
            || !section.getUpdatedAt().toInstant().equals(request.getUpdatedAt().toInstant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Section đã được cập nhật bởi người khác. Hãy tải lại.");
        }

        Map<String, Object> extra = request.getExtraJson() != null ? request.getExtraJson() : Map.of();
        boolean home = PAGE_HOME.equals(pageKey);
        if (home) {
            validateExtraJson(sectionKey, extra);
        } else {
            validatePageExtra(extra, 0);
        }
        validateLink(request.getCtaLink(), "CTA link");

        boolean useDefaultDesktop = request.getUseDefaultDesktop() == null || Boolean.TRUE.equals(request.getUseDefaultDesktop());
        boolean useDefaultMobile = request.getUseDefaultMobile() == null || Boolean.TRUE.equals(request.getUseDefaultMobile());

        section.setTitle(request.getTitle());
        section.setBody(request.getBody());
        section.setCtaText(request.getCtaText());
        section.setCtaLink(request.getCtaLink());
        section.setUseDefaultDesktop(useDefaultDesktop);
        section.setUseDefaultMobile(useDefaultMobile);
        section.setImageDesktop(useDefaultDesktop ? null : sanitizeImagePath(request.getImageDesktop()));
        section.setImageMobile(useDefaultMobile ? null : sanitizeImagePath(request.getImageMobile()));
        section.setExtraJson(new HashMap<>(home
            ? normalizeExtraImageFlags(sectionKey, extra)
            : (Map<String, Object>) normalizePageImages(extra)));
        if (request.getEnabled() != null) {
            section.setEnabled(request.getEnabled());
        }
        if (request.getSortOrder() != null) {
            section.setSortOrder(request.getSortOrder());
        }
        section.setUpdatedBy(username);

        // Flush so @PreUpdate sets updatedAt before the response is built; otherwise the admin receives a
        // timestamp that differs from the stored one and the next save fails the optimistic-lock check.
        return toSectionResponse(sectionRepository.saveAndFlush(section));
    }

    private Set<String> requirePage(String pageKey) {
        Set<String> keys = PAGE_SECTIONS.get(pageKey);
        if (keys == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trang không tồn tại");
        }
        return keys;
    }

    /**
     * story/philosophy sections are free-form JSON shaped by the admin editor. Limits list sizes and nesting,
     * and checks every "link"/"to" value with the same rules as the homepage.
     */
    private void validatePageExtra(Object node, int depth) {
        if (depth > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung lồng quá sâu");
        }
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String k = String.valueOf(e.getKey());
                if (("link".equals(k) || "to".equals(k)) && (e.getValue() == null || e.getValue() instanceof String)) {
                    validateLink((String) e.getValue(), "Link");
                } else {
                    validatePageExtra(e.getValue(), depth + 1);
                }
            }
        } else if (node instanceof List<?> list) {
            if (list.size() > PAGE_MAX_LIST_ITEMS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách tối đa " + PAGE_MAX_LIST_ITEMS + " mục");
            }
            for (Object item : list) {
                validatePageExtra(item, depth + 1);
            }
        }
    }

    /**
     * Any object carrying "useDefaultImage" follows the three-state image rule used on the homepage:
     * true → "image" cleared (storefront shows its packaged default); false → "image" must be a /images/ path or empty.
     */
    @SuppressWarnings("unchecked")
    private Object normalizePageImages(Object node) {
        if (node instanceof Map<?, ?> map) {
            Map<String, Object> out = new HashMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(k), normalizePageImages(v)));
            if (out.containsKey("useDefaultImage")) {
                boolean useDefault = !Boolean.FALSE.equals(out.get("useDefaultImage"));
                out.put("useDefaultImage", useDefault);
                out.put("image", useDefault ? null : sanitizeImagePathNullable(asString(out.get("image"))));
            }
            return out;
        }
        if (node instanceof List<?> list) {
            return list.stream().map(this::normalizePageImages).toList();
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeExtraImageFlags(String sectionKey, Map<String, Object> extra) {
        Map<String, Object> out = new HashMap<>(extra);
        if ("quality".equals(sectionKey)) {
            Object itemsObj = out.get("items");
            if (itemsObj instanceof List<?> list) {
                List<Map<String, Object>> items = list.stream().map(item -> {
                    Map<String, Object> m = item instanceof Map<?, ?> map
                        ? new HashMap<>((Map<String, Object>) map)
                        : new HashMap<>();
                    boolean useDefault = !Boolean.FALSE.equals(m.get("useDefaultIcon"));
                    m.put("useDefaultIcon", useDefault);
                    if (useDefault) {
                        m.put("icon", null);
                    } else {
                        m.put("icon", sanitizeImagePathNullable(asString(m.get("icon"))));
                    }
                    return m;
                }).toList();
                out.put("items", items);
            }
        }
        if ("meal_gallery".equals(sectionKey)) {
            boolean useDefaultBanner = !Boolean.FALSE.equals(out.get("useDefaultBanner"));
            out.put("useDefaultBanner", useDefaultBanner);
            if (useDefaultBanner) {
                out.put("banner", null);
            } else {
                out.put("banner", sanitizeImagePathNullable(asString(out.get("banner"))));
            }
            Object tilesObj = out.get("tiles");
            if (tilesObj instanceof List<?> list) {
                List<Map<String, Object>> tiles = list.stream().map(tile -> {
                    Map<String, Object> m = tile instanceof Map<?, ?> map
                        ? new HashMap<>((Map<String, Object>) map)
                        : new HashMap<>();
                    boolean useDefault = !Boolean.FALSE.equals(m.get("useDefault"));
                    m.put("useDefault", useDefault);
                    if (useDefault) {
                        m.put("src", null);
                    } else {
                        m.put("src", sanitizeImagePathNullable(asString(m.get("src"))));
                    }
                    return m;
                }).toList();
                out.put("tiles", tiles);
            }
        }
        return out;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** Blank → null; non-blank validated. Used when override path may be intentionally empty. */
    private String sanitizeImagePathNullable(String path) {
        if (path == null || path.isBlank() || "null".equals(path)) {
            return null;
        }
        return sanitizeImagePath(path);
    }

    @SuppressWarnings("unchecked")
    private void validateExtraJson(String sectionKey, Map<String, Object> extra) {
        switch (sectionKey) {
            case "quality" -> {
                Object items = extra.get("items");
                if (!(items instanceof List<?> list) || list.size() != 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quality.extra_json.items phải có đúng 2 phần tử");
                }
            }
            case "meal_gallery" -> {
                Object tiles = extra.get("tiles");
                if (!(tiles instanceof List<?> list) || list.size() != 4) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "meal_gallery.extra_json.tiles phải có đúng 4 phần tử");
                }
            }
            case "floating_chat" -> {
                validateLink(asString(extra.get("messengerUrl")), "Messenger URL");
                validateLink(asString(extra.get("zaloUrl")), "Zalo URL");
            }
            case "footer" -> validateFooter(extra);
            default -> {
                // hero/story: empty or free-form allowed
            }
        }
    }

    private void validateFooter(Map<String, Object> extra) {
        Object policyLinks = extra.get("policyLinks");
        if (policyLinks != null) {
            if (!(policyLinks instanceof List<?> list) || list.size() > FOOTER_MAX_POLICY_LINKS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "footer.policyLinks tối đa " + FOOTER_MAX_POLICY_LINKS + " link");
            }
            for (Object item : list) {
                if (item instanceof Map<?, ?> link) {
                    validateLink(asString(link.get("to")), "Link chính sách");
                }
            }
        }
        Object socials = extra.get("socials");
        if (socials != null) {
            if (!(socials instanceof List<?> list) || list.size() > FOOTER_MAX_SOCIALS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "footer.socials tối đa " + FOOTER_MAX_SOCIALS + " mục");
            }
            for (Object item : list) {
                if (item instanceof Map<?, ?> social) {
                    String type = asString(social.get("type"));
                    if (type == null || !FOOTER_SOCIAL_TYPES.contains(type)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại mạng xã hội không hợp lệ: " + type);
                    }
                    validateLink(asString(social.get("url")), "Link mạng xã hội");
                }
            }
        }
        validateLink(asString(extra.get("faqLink")), "Link FAQ");
    }

    /** Allows empty, "#", internal paths, http(s), tel: and mailto:. Rejects javascript:, data: and others. */
    private void validateLink(String link, String label) {
        if (link == null || link.isBlank()) {
            return;
        }
        String v = link.trim().toLowerCase(java.util.Locale.ROOT);
        boolean ok = v.equals("#")
            || (v.startsWith("/") && !v.startsWith("//"))
            || v.startsWith("https://") || v.startsWith("http://")
            || v.startsWith("tel:") || v.startsWith("mailto:");
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " không hợp lệ");
        }
    }

    private String sanitizeImagePath(String path) {
        if (path == null || path.isBlank() || "null".equals(path)) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.contains("..") || trimmed.contains("\0")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đường dẫn ảnh không hợp lệ");
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận path /images/...");
        }
        if (!trimmed.startsWith("/images/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh phải bắt đầu bằng /images/");
        }
        return trimmed;
    }

    private HomepageSectionResponse toSectionResponse(HomepageSection section) {
        return HomepageSectionResponse.builder()
            .id(section.getId())
            .pageKey(section.getPageKey())
            .sectionKey(section.getSectionKey())
            .title(section.getTitle())
            .body(section.getBody())
            .ctaText(section.getCtaText())
            .ctaLink(section.getCtaLink())
            .imageDesktop(section.getImageDesktop())
            .imageMobile(section.getImageMobile())
            .useDefaultDesktop(section.getUseDefaultDesktop() == null || section.getUseDefaultDesktop())
            .useDefaultMobile(section.getUseDefaultMobile() == null || section.getUseDefaultMobile())
            .extraJson(section.getExtraJson())
            .enabled(section.getEnabled())
            .sortOrder(section.getSortOrder())
            .updatedAt(section.getUpdatedAt())
            .updatedBy(section.getUpdatedBy())
            .build();
    }
}
