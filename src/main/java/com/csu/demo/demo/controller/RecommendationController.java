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

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;
    @Autowired
    private ItemMapper itemMapper;

    public RecommendationController(RecommendationService recommendationService, UserService userService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
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
    public ResponseEntity<List<Item>> recommendByEmbedding(@RequestParam int userId, @RequestParam(defaultValue = "20") int limit) {
        List<Float> userEmbedding = userService.getUserEmbedding(userId);
        if (userEmbedding == null || userEmbedding.isEmpty()) {
            return hotItems(limit);
        }
        return ResponseEntity.ok(recommendationService.recommendByEmbedding(userEmbedding, limit));
    }
}
