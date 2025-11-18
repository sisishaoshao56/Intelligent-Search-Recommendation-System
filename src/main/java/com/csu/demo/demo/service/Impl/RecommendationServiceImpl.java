package com.csu.demo.demo.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.csu.demo.demo.domain.Item;
import com.csu.demo.demo.dto.ItemScoreDTO;
import com.csu.demo.demo.mapper.ItemMapper;
import com.csu.demo.demo.service.HotItemService;
import com.csu.demo.demo.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final int MIN_TAG_FETCH = 20;
    private static final int MAX_TAG_FETCH = 200;

    private final HotItemService hotItemService;
    private final ItemMapper itemMapper;
    private final ElasticsearchClient esClient;
    private final String itemIndex;

    public RecommendationServiceImpl(
            HotItemService hotItemService,
            ItemMapper itemMapper,
            ElasticsearchClient esClient,
            @Value("${elasticsearch.index.items}") String itemIndex) {

        this.hotItemService = hotItemService;
        this.itemMapper = itemMapper;
        this.esClient = esClient;
        this.itemIndex = itemIndex;
    }

    @Override
    public List<ItemScoreDTO> hotItemList(int limit) {
        return hotItemService.listTopItems(limit);
    }

    @Override
    public List<Item> recommendByTags(String[] tags) {
        if (tags == null || tags.length == 0) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> queryTags = new LinkedHashSet<>();
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            queryTags.add(trimmed);
            String lower = trimmed.toLowerCase(Locale.ROOT);
            normalized.add(lower);
            if (!lower.equals(trimmed)) {
                queryTags.add(lower);
            }
        }

        if (queryTags.isEmpty()) {
            return Collections.emptyList();
        }

        int fetchLimit = Math.min(MAX_TAG_FETCH,
                Math.max(MIN_TAG_FETCH, normalized.size() * 10));

        List<Item> matches = itemMapper.selectByTags(new ArrayList<>(queryTags), fetchLimit);
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> matchTargets = new HashSet<>(normalized);
        Map<Integer, Integer> scoreCache = new HashMap<>(matches.size());
        List<Item> filtered = new ArrayList<>();
        for (Item item : matches) {
            int score = overlapScore(item, matchTargets);
            if (score > 0) {
                filtered.add(item);
                scoreCache.put(item.getId(), score);
            }
        }

        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        filtered.sort((a, b) -> {
            int cmp = Integer.compare(scoreCache.getOrDefault(b.getId(), 0),scoreCache.getOrDefault(a.getId(), 0));
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a.getId(), b.getId());
        });

        return filtered;
    }
    
    @Override
    public List<Item> recommendByEmbedding(List<Float> userEmbedding, int limit) {

        if (userEmbedding == null || userEmbedding.isEmpty()) {
            return Collections.emptyList();
        }

        int k = Math.max(limit, 1);
        int candidates = Math.max(k * 2, 20);

        try {
            SearchResponse<Void> response = esClient.search(s -> s
                    .index(itemIndex)
                    .knn(knn -> knn
                            .field("embedding")
                            .queryVector(userEmbedding)
                            .k(k)
                            .numCandidates(candidates)
                    ),
                    Void.class
            );

            List<Integer> itemIds = response.hits().hits().stream()
                    .map(hit -> {
                        try {
                            return Integer.parseInt(hit.id());
                        } catch (NumberFormatException ex) {
                            log.warn("ES 返回了非数字 ID: {}", hit.id());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (itemIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<Item> items = itemMapper.selectBatchIds(itemIds);
            Map<Integer, Item> map = items.stream().collect(
                    Collectors.toMap(Item::getId, it -> it, (a, b) -> a, LinkedHashMap::new)
            );

            List<Item> ordered = new ArrayList<>();//推荐的返回顺序
            for (Integer id : itemIds) {
                Item it = map.get(id);
                if (it != null && ordered.size() < limit) {
                    ordered.add(it);
                }
            }

            return ordered;

        } catch (IOException e) {
            log.error("KNN 搜索失败", e);
            return Collections.emptyList();
        }
    }

    private static int overlapScore(Item item, Set<String> targets) {
        if (item == null || targets.isEmpty()) {
            return 0;
        }
        int matches = 0;
        for (String tag : parseTags(item.getTags_json())) {
            if (targets.contains(tag)) {
                matches++;
            }
        }
        return matches;
    }

    private static List<String> parseTags(String raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (trimmed.length() < 2 || trimmed.charAt(0) != '[' ||
                trimmed.charAt(trimmed.length() - 1) != ']') {
            return Collections.emptyList();
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = body.split(",");
        List<String> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            String cleaned = stripQuotes(part.trim());
            if (!cleaned.isEmpty()) {
                result.add(cleaned.toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private static String stripQuotes(String input) {
        if (input.length() >= 2 && input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1).trim();
        }
        return input.trim();
    }
}
