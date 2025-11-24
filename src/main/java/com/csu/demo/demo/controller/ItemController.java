package com.csu.demo.demo.controller;

import com.csu.demo.demo.config.UserContext;
import com.csu.demo.demo.service.ItemService;
import com.csu.demo.demo.mapper.ItemThumbnailMapper;
import com.csu.demo.demo.domain.ItemThumbnail;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final ItemThumbnailMapper itemThumbnailMapper;

    public ItemController(ItemService itemService, ItemThumbnailMapper itemThumbnailMapper) {
        this.itemService = itemService;
        this.itemThumbnailMapper = itemThumbnailMapper;
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<?> play(@PathVariable int id) {
        return itemService.preparePlay(id, UserContext.getUserId())
                .<ResponseEntity<?>>map(result -> ResponseEntity.ok()
                        .contentType(result.mediaType())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + result.filename() + "\"")
                        .body(result.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<Resource> thumb(@PathVariable int id) {
        ItemThumbnail thumb = itemThumbnailMapper.selectById(id);
        if (thumb == null || thumb.getThumbPath() == null || thumb.getThumbPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        try {
            UrlResource resource = new UrlResource("file:" + thumb.getThumbPath());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                    .orElse(MediaType.IMAGE_JPEG);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
