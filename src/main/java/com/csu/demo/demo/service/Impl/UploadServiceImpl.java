package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.domain.ItemThumbnail;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.mapper.ItemThumbnailMapper;
import com.csu.demo.demo.service.ClipService;
import com.csu.demo.demo.service.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);
    private static final Path UPLOAD_ROOT = Paths.get("D:\\Code\\JAVA\\demo\\Video\\data\\Uploads");

    private final ClipService clipService;
    private final ItemMapper itemMapper;
    private final ItemThumbnailMapper itemThumbnailMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UploadServiceImpl(ClipService clipService,
                             ItemMapper itemMapper,
                             ItemThumbnailMapper itemThumbnailMapper) {
        this.clipService = clipService;
        this.itemMapper = itemMapper;
        this.itemThumbnailMapper = itemThumbnailMapper;
    }

    @Override
    public Item upload(String title, String tags, MultipartFile video, MultipartFile cover) {
        validate(title, video);

        try {
            String safeName = sanitizeName(title);
            Path folder = UPLOAD_ROOT.resolve(safeName + "-" + timestamp());
            Files.createDirectories(folder);

            Path videoPath = folder.resolve(pickName(video.getOriginalFilename(), safeName + ".mp4"));
            Files.copy(video.getInputStream(), videoPath);

            Path coverPath = null;
            if (cover != null && !cover.isEmpty()) {
                coverPath = folder.resolve(pickName(cover.getOriginalFilename(), safeName + "-cover.jpg"));
                Files.copy(cover.getInputStream(), coverPath);
            }

            List<Float> tagVec = encodeTags(tags);
            List<Float> imgVec = encodeImageSafe(cover);
            List<Float> finalVec = mergeVectors(tagVec, imgVec);
            String embeddingJson = objectMapper.writeValueAsString(finalVec);

            Item item = new Item();
            item.setTitle(title);
            item.setTagsJson(toJsonTags(tags));
            item.setPath(videoPath.toString());
            item.setEmbeddingVector(embeddingJson);
            itemMapper.insert(item);

            if (coverPath != null) {
                ItemThumbnail thumb = new ItemThumbnail();
                thumb.setItemId(item.getId());
                thumb.setThumbPath(coverPath.toString());
                itemThumbnailMapper.insert(thumb);
                item.setThumbPath(coverPath.toString());
            }
            return item;
        } catch (IOException e) {
            throw new RuntimeException("上传失败", e);
        }
    }

    private void validate(String title, MultipartFile video) {
        if (!StringUtils.hasText(title) || video == null || video.isEmpty()) {
            throw new IllegalArgumentException("标题和视频不能为空");
        }
    }

    private String sanitizeName(String raw) {
        String cleaned = raw.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return cleaned.isEmpty() ? "upload" : cleaned;
    }

    private String timestamp() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    }

    private String pickName(String original, String fallback) {
        if (original == null || original.isBlank()) return fallback;
        return Paths.get(original).getFileName().toString();
    }

    private List<Float> encodeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        try {
            return clipService.encodeText(tags);
        } catch (Exception e) {
            log.warn("encodeText 失败，使用空向量。tags={}", tags, e);
            return List.of();
        }
    }

    private List<Float> encodeImageSafe(MultipartFile cover) {
        if (cover == null || cover.isEmpty()) {
            return List.of();
        }
        try {
            return clipService.encodeImage(cover);
        } catch (Exception e) {
            log.warn("encodeImage 失败，使用空向量", e);
            return List.of();
        }
    }

    private List<Float> mergeVectors(List<Float> a, List<Float> b) {
        if (a == null || a.isEmpty()) return b == null ? List.of() : b;
        if (b == null || b.isEmpty()) return a;
        int len = Math.min(a.size(), b.size());
        List<Float> merged = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            merged.add((a.get(i) + b.get(i)) / 2f);
        }
        return merged;
    }

    private String toJsonTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return "[]";
        }
        String trimmed = tags.trim();
        // 如果前端已经传了 JSON 数组，直接解析并规整
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                List<?> raw = objectMapper.readValue(trimmed, List.class);
                List<String> normalized = raw.stream()
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                return objectMapper.writeValueAsString(normalized);
            } catch (Exception e) {
                log.warn("解析 JSON 标签失败，fallback 按分隔符拆分: {}", tags, e);
            }
        }
        // 否则按分隔符拆分再序列化
        try {
            List<String> list = Arrays.stream(tags.split("[,，;；\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("标签序列化失败，返回空数组: {}", tags, e);
            return "[]";
        }
    }
}
