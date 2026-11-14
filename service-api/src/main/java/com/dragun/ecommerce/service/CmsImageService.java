package com.dragun.ecommerce.service;

import com.dragun.ecommerce.cms.ImageSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsImageService {

    private final FileStorageService fileStorageService;

    public String upload(MultipartFile file, String slotKey, String entityType, String entityId) throws IOException {
        ImageSlot slot = ImageSlot.fromKey(slotKey);
        validate(file, slot);
        return store(file, slot, entityType, entityId);
    }

    /**
     * All checks of {@link #upload} without writing anything; throws IllegalArgumentException with a user-facing
     * message. Lets callers with several files (product reviews) reject the batch before storing any of them.
     */
    public void validate(MultipartFile file, ImageSlot slot) throws IOException {
        validateFile(file, slot);

        int[] size = readDimensions(file);
        int width = size[0];
        int height = size[1];

        if (width < slot.getMinWidth() || (slot.getMinHeight() > 0 && height < slot.getMinHeight())) {
            throw new IllegalArgumentException(String.format(
                "Ảnh quá nhỏ cho slot %s. Tối thiểu %dx%d, hiện tại %dx%d.",
                slot.getKey(), slot.getMinWidth(), Math.max(slot.getMinHeight(), 1), width, height
            ));
        }

        // Client should already crop/resize; allow small aspect tolerance for output-sized images
        boolean nearOutput = Math.abs(width - slot.getOutputWidth()) <= 2
            && Math.abs(height - slot.getOutputHeight()) <= 2;
        if (!nearOutput && !slot.isAspectAllowed(width, height)) {
            throw new IllegalArgumentException(String.format(
                "Ảnh không đúng tỷ lệ mẫu %s (cần ~%.2f). Kích thước hiện tại %dx%d.",
                slot.getKey(), slot.getAspectRatio(), width, height
            ));
        }
    }

    /** Writes a file that already passed {@link #validate}; returns its public /images/... path. */
    public String store(MultipartFile file, ImageSlot slot, String entityType, String entityId) throws IOException {
        // Extension from the verified format, not the client file name
        String ext = "image/png".equals(file.getContentType().toLowerCase(Locale.ROOT)) ? "png" : "jpg";
        String relativePath = buildRelativePath(slot, entityType, entityId, ext);
        String stored = fileStorageService.uploadFile(file, relativePath);
        log.info("CMS upload slot={} path={}", slot.getKey(), stored);
        return stored;
    }

    /**
     * Uploads are already cropped and resized by the admin UI, so anything near this size is suspicious.
     * Checked from the image header before any decoding: a small PNG can declare a huge canvas and exhaust the heap.
     */
    static final long MAX_PIXELS = 16_000_000L;

    private static final java.util.Map<String, String> MIME_TO_FORMAT = java.util.Map.of(
        "image/jpeg", "jpeg",
        "image/png", "png"
    );

    /** Returns {width, height}; rejects files whose content does not match the declared type or exceeds MAX_PIXELS. */
    private int[] readDimensions(MultipartFile file) throws IOException {
        String expectedFormat = MIME_TO_FORMAT.get(file.getContentType().toLowerCase(Locale.ROOT));
        try (InputStream in = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = iis == null ? java.util.Collections.emptyIterator() : ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Không đọc được ảnh. Chỉ chấp nhận JPEG/PNG hợp lệ.");
            }
            ImageReader reader = readers.next();
            try {
                String actualFormat = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (expectedFormat == null || !expectedFormat.equals(actualFormat)) {
                    throw new IllegalArgumentException("Nội dung file không khớp định dạng khai báo (" + file.getContentType() + ").");
                }
                reader.setInput(iis, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException(String.format(
                        "Ảnh quá lớn (%dx%d). Tối đa %d điểm ảnh.", width, height, MAX_PIXELS));
                }
                // Full decode only after the size check, to reject truncated or corrupt files.
                reader.read(0);
                return new int[] {width, height};
            } catch (javax.imageio.IIOException e) {
                throw new IllegalArgumentException("Ảnh bị hỏng hoặc không đọc được.");
            } finally {
                reader.dispose();
            }
        }
    }

    private void validateFile(MultipartFile file, ImageSlot slot) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        if (file.getSize() > slot.getMaxBytes()) {
            throw new IllegalArgumentException(String.format(
                "File vượt quá %dKB cho slot %s", slot.getMaxBytes() / 1024, slot.getKey()
            ));
        }
        String contentType = file.getContentType();
        if (contentType == null || !slot.getAllowedMimeTypes().contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Định dạng ảnh không được hỗ trợ cho slot " + slot.getKey());
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (name.contains("..") || name.contains("\\") || name.contains("\0")) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
    }

    private String buildRelativePath(ImageSlot slot, String entityType, String entityId, String ext) {
        String safeEntity = sanitizeSegment(entityId != null && !entityId.isBlank() ? entityId : "common");
        String ts = String.valueOf(System.currentTimeMillis());
        String rand = UUID.randomUUID().toString().substring(0, 8);

        if ("product".equalsIgnoreCase(entityType) || slot.name().startsWith("PRODUCT_")) {
            if (slot == ImageSlot.PRODUCT_MAIN) {
                return "products/" + safeEntity + "/main_" + ts + "." + ext;
            }
            return "products/" + safeEntity + "/gallery/" + ts + "_" + rand + "." + ext;
        }
        return slot.folderPrefix() + "/" + safeEntity + "/" + slot.getKey() + "_" + ts + "_" + rand + "." + ext;
    }

    private String sanitizeSegment(String value) {
        String cleaned = value.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.contains("..")) {
            throw new IllegalArgumentException("entityId không hợp lệ");
        }
        return cleaned;
    }
}
