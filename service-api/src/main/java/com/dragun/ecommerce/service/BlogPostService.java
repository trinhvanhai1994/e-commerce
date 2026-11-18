package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.request.BlogPostRequest;
import com.dragun.ecommerce.model.dto.response.BlogPostResponse;
import com.dragun.ecommerce.model.entity.BlogPost;
import com.dragun.ecommerce.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final Set<String> BLOCK_TYPES = Set.of("p", "h2", "ul", "img", "cta", "table");

    private final BlogPostRepository blogPostRepository;

    @Transactional(readOnly = true)
    public List<BlogPostResponse> listPublished() {
        return blogPostRepository.findByStatusAndDeletedAtIsNullOrderByPublishedAtDesc("PUBLISHED").stream()
            .map(BlogPostService::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getPublishedBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlugAndDeletedAtIsNull(slug)
            .filter(p -> "PUBLISHED".equals(p.getStatus()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));
        return toDetailResponse(post);
    }

    @Transactional(readOnly = true)
    public List<BlogPostResponse> listAdmin(boolean includeDeleted) {
        if (includeDeleted) {
            return blogPostRepository.findAll().stream().map(BlogPostService::toDetailResponse).toList();
        }
        return blogPostRepository.findByDeletedAtIsNullOrderByUpdatedAtDesc().stream()
            .map(BlogPostService::toDetailResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getAdminById(Long id) {
        BlogPost post = blogPostRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));
        return toDetailResponse(post);
    }

    @Transactional
    public BlogPostResponse create(BlogPostRequest request, String username) {
        validateSlug(request.getSlug());
        if (blogPostRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug đã tồn tại");
        }
        List<Map<String, Object>> content = validateContent(request.getContentJson());
        applyHomeRules(null, request);

        BlogPost post = BlogPost.builder()
            .slug(request.getSlug().trim())
            .title(sanitizeText(request.getTitle()))
            .excerpt(sanitizeText(request.getExcerpt()))
            .coverImage(sanitizeImage(request.getCoverImage()))
            .contentJson(content)
            .status(normalizeStatus(request.getStatus(), "DRAFT"))
            .showOnHome(Boolean.TRUE.equals(request.getShowOnHome()))
            .homeOrder(Boolean.TRUE.equals(request.getShowOnHome()) ? request.getHomeOrder() : null)
            .createdBy(username)
            .updatedBy(username)
            .build();

        if ("PUBLISHED".equals(post.getStatus())) {
            post.setPublishedAt(OffsetDateTime.now());
        }
        return toDetailResponse(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse update(Long id, BlogPostRequest request, String username) {
        BlogPost post = blogPostRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));

        // Same rule as homepage sections: updatedAt is mandatory, otherwise a client could silently overwrite
        // another admin's edit by omitting it.
        if (request.getUpdatedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu updatedAt. Hãy tải lại bài viết rồi lưu lại.");
        }
        if (post.getUpdatedAt() == null
            || !post.getUpdatedAt().toInstant().equals(request.getUpdatedAt().toInstant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bài viết đã được cập nhật bởi người khác. Hãy tải lại.");
        }

        // slug immutable after first publish
        if ("PUBLISHED".equals(post.getStatus()) && request.getSlug() != null
            && !request.getSlug().equals(post.getSlug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không đổi slug sau khi đã publish");
        }
        if (!"PUBLISHED".equals(post.getStatus()) && request.getSlug() != null
            && !request.getSlug().equals(post.getSlug())) {
            validateSlug(request.getSlug());
            if (blogPostRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug đã tồn tại");
            }
            post.setSlug(request.getSlug().trim());
        }

        applyHomeRules(post.getId(), request);
        post.setTitle(sanitizeText(request.getTitle()));
        post.setExcerpt(sanitizeText(request.getExcerpt()));
        post.setCoverImage(sanitizeImage(request.getCoverImage()));
        post.setContentJson(validateContent(request.getContentJson()));
        post.setShowOnHome(Boolean.TRUE.equals(request.getShowOnHome()));
        post.setHomeOrder(Boolean.TRUE.equals(request.getShowOnHome()) ? request.getHomeOrder() : null);
        if (request.getStatus() != null) {
            String next = normalizeStatus(request.getStatus(), post.getStatus());
            if ("PUBLISHED".equals(next) && post.getPublishedAt() == null) {
                post.setPublishedAt(OffsetDateTime.now());
            }
            post.setStatus(next);
        }
        post.setUpdatedBy(username);
        return toDetailResponse(blogPostRepository.saveAndFlush(post));
    }

    @Transactional
    public BlogPostResponse updateStatus(Long id, String status, String username) {
        BlogPost post = blogPostRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));
        String next = normalizeStatus(status, post.getStatus());
        if ("PUBLISHED".equals(next) && post.getPublishedAt() == null) {
            post.setPublishedAt(OffsetDateTime.now());
        }
        if ("DRAFT".equals(next)) {
            post.setShowOnHome(false);
            post.setHomeOrder(null);
        }
        post.setStatus(next);
        post.setUpdatedBy(username);
        return toDetailResponse(blogPostRepository.saveAndFlush(post));
    }

    @Transactional
    public void softDelete(Long id, String username) {
        BlogPost post = blogPostRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));
        post.setDeletedAt(OffsetDateTime.now());
        post.setShowOnHome(false);
        post.setHomeOrder(null);
        post.setStatus("DRAFT");
        post.setUpdatedBy(username);
        blogPostRepository.save(post);
    }

    private void applyHomeRules(Long currentId, BlogPostRequest request) {
        if (!Boolean.TRUE.equals(request.getShowOnHome())) {
            return;
        }
        if (request.getHomeOrder() == null || request.getHomeOrder() < 1 || request.getHomeOrder() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "home_order phải từ 1 đến 3 khi hiện Home");
        }
        long count = currentId == null
            ? blogPostRepository.countByShowOnHomeTrueAndDeletedAtIsNull()
            : blogPostRepository.countByShowOnHomeTrueAndDeletedAtIsNullAndIdNot(currentId);
        boolean alreadyOnHome = false;
        if (currentId != null) {
            alreadyOnHome = blogPostRepository.findByIdAndDeletedAtIsNull(currentId)
                .map(p -> Boolean.TRUE.equals(p.getShowOnHome()))
                .orElse(false);
        }
        if (!alreadyOnHome && count >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đã đủ 3 bài hiện trên Home");
        }
        Long idForCheck = currentId == null ? -1L : currentId;
        if (blogPostRepository.existsByHomeOrderAndShowOnHomeTrueAndDeletedAtIsNullAndIdNot(request.getHomeOrder(), idForCheck)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "home_order đã được dùng bởi bài khác");
        }
    }

    private List<Map<String, Object>> validateContent(List<Map<String, Object>> content) {
        if (content == null) {
            return List.of();
        }
        List<Map<String, Object>> cleaned = new ArrayList<>();
        for (Map<String, Object> block : content) {
            if (block == null || block.get("type") == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block thiếu type");
            }
            String type = String.valueOf(block.get("type"));
            if (!BLOCK_TYPES.contains(type)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block type không hợp lệ: " + type);
            }
            Map<String, Object> copy = new java.util.LinkedHashMap<>(block);
            if (copy.get("text") instanceof String text) {
                copy.put("text", sanitizeText(text));
            }
            if ("ul".equals(type)) {
                Object items = copy.get("items");
                if (!(items instanceof List<?>)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block ul cần items[]");
                }
            }
            if ("img".equals(type)) {
                Object src = copy.get("src");
                if (!(src instanceof String s) || s.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Block img cần src");
                }
                copy.put("src", sanitizeImage(s));
            }
            if ("cta".equals(type)) {
                Object to = copy.get("to");
                if (!(to instanceof String) && to != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cta.to phải là path string");
                }
            }
            cleaned.add(copy);
        }
        return cleaned;
    }

    private void validateSlug(String slug) {
        if (slug == null || !SLUG_PATTERN.matcher(slug.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug không hợp lệ");
        }
    }

    private String normalizeStatus(String status, String fallback) {
        if (status == null || status.isBlank()) {
            return fallback;
        }
        String s = status.trim().toUpperCase();
        if (!s.equals("DRAFT") && !s.equals("PUBLISHED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status phải là DRAFT hoặc PUBLISHED");
        }
        return s;
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("(?i)<script.*?>.*?</script>", "")
            .replaceAll("(?i)<.*?on\\w+\\s*=.*>", "")
            .trim();
    }

    private String sanitizeImage(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        String trimmed = path.trim();
        if (trimmed.contains("..") || trimmed.startsWith("data:") || trimmed.startsWith("http")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đường dẫn ảnh không hợp lệ");
        }
        if (!trimmed.startsWith("/images/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh phải bắt đầu bằng /images/");
        }
        return trimmed;
    }

    public static BlogPostResponse toSummaryResponse(BlogPost post) {
        return BlogPostResponse.builder()
            .id(post.getId())
            .slug(post.getSlug())
            .title(post.getTitle())
            .excerpt(post.getExcerpt())
            .coverImage(post.getCoverImage())
            .status(post.getStatus())
            .showOnHome(post.getShowOnHome())
            .homeOrder(post.getHomeOrder())
            .publishedAt(post.getPublishedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }

    public static BlogPostResponse toDetailResponse(BlogPost post) {
        BlogPostResponse r = toSummaryResponse(post);
        r.setContentJson(post.getContentJson());
        r.setUpdatedBy(post.getUpdatedBy());
        return r;
    }
}
