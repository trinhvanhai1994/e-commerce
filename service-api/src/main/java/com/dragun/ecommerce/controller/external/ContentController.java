package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.BlogPostResponse;
import com.dragun.ecommerce.model.dto.response.HomepageContentResponse;
import com.dragun.ecommerce.model.dto.response.HomepageSectionResponse;
import com.dragun.ecommerce.service.BlogPostService;
import com.dragun.ecommerce.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/dragun/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final BlogPostService blogPostService;

    @GetMapping("/homepage")
    public ResponseEntity<ApiResponse<HomepageContentResponse>> homepage() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(contentService.getPublicHomepage()));
    }

    /** Sections of a content page (story, philosophy). A section missing here keeps the storefront default. */
    @GetMapping("/pages/{pageKey}")
    public ResponseEntity<ApiResponse<Map<String, HomepageSectionResponse>>> page(@PathVariable String pageKey) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(contentService.getPublicPageSections(pageKey)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<BlogPostResponse>>> posts() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(blogPostService.listPublished()));
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> post(@PathVariable String slug) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(blogPostService.getPublishedBySlug(slug)));
    }
}
