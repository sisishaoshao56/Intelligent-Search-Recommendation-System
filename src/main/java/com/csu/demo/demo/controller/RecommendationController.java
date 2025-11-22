package com.csu.demo.demo.controller;

import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.service.RecommendationService;
import com.csu.demo.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.csu.demo.demo.mapper.ItemMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;
    @Autowired
    private ItemMapper itemMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public RecommendationController(RecommendationService recommendationService, UserService userService, StringRedisTemplate stringRedisTemplate) {
        this.recommendationService = recommendationService;
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/hot")
    public ResponseEntity<List<Item>> hotItems(@RequestParam(defaultValue = "20") int limit) {
        List<Integer> ids = recommendationService.hotItemList(limit).stream()
            .map(dto -> dto.getItemId())
            .filter(Objects::nonNull)
            .toList();
        if (ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Item> items = itemMapper.selectBatchIds(ids);
        Map<Integer, Item> map = items.stream()
            .collect(Collectors.toMap(Item::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
        List<Item> ordered = ids.stream()
            .map(map::get)
            .filter(Objects::nonNull)
            .toList();
        return ResponseEntity.ok(ordered);
    }

    @GetMapping("/embedding")
    public ResponseEntity<List<Item>> recommendByEmbedding(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "userId", required = false) Integer userIdParam,
            @RequestParam(defaultValue = "20") int limit) {

        Integer userId = resolveUserId(token, userIdParam);
        if (userId == null || userId <= 0) {
            return hotItems(limit);
        }

        List<Float> userEmbedding = userService.getUserEmbedding(userId);
        if (userEmbedding == null || userEmbedding.isEmpty()) {
            return hotItems(limit);
        }
        return ResponseEntity.ok(recommendationService.recommendByEmbedding(userEmbedding, limit));
    }

    private Integer resolveUserId(String token, Integer userIdParam) {
        if (userIdParam != null && userIdParam > 0) {
            return userIdParam;
        }
        if (token == null || token.isBlank()) {
            return null;
        }
        String raw = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        try {
            String userIdStr = stringRedisTemplate.opsForValue().get("auth:token:" + raw);
            return (userIdStr != null && !userIdStr.isBlank()) ? Integer.valueOf(userIdStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
