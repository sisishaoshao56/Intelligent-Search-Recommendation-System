package com.csu.demo.demo.controller;

import com.csu.demo.demo.domain.Event;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.EventService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemMapper itemMapper;
    private final EventService eventService;
    private final StringRedisTemplate stringRedisTemplate;

    public ItemController(ItemMapper itemMapper, EventService eventService, StringRedisTemplate stringRedisTemplate) {
        this.itemMapper = itemMapper;
        this.eventService = eventService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/{id}/play")
    public ResponseEntity<Resource> play(@PathVariable int id,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestParam(value = "token", required = false) String tokenParam) throws MalformedURLException {
        Item item = itemMapper.selectById(id);
        if (item == null || item.getPath() == null || item.getPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        UrlResource resource = new UrlResource("file:" + item.getPath());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String filename = Paths.get(item.getPath()).getFileName().toString();

        try {
            Integer userId = resolveUserId(authHeader, tokenParam);
            if (userId != null && userId > 0) {
                Event event = new Event();
                event.setUser_id(userId);
                event.setItem_id(id);
                event.setAction("play");
                event.setScore(1f);
                event.setTs(LocalDateTime.now());
                eventService.recordEvent(event);
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private Integer resolveUserId(String authHeader, String tokenParam) {
        String raw = null;
        if (tokenParam != null && !tokenParam.isBlank()) {
            raw = tokenParam.trim();
        } else if (authHeader != null && !authHeader.isBlank()) {
            raw = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String userIdStr = stringRedisTemplate.opsForValue().get("auth:token:" + raw);
        if (userIdStr == null || userIdStr.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
