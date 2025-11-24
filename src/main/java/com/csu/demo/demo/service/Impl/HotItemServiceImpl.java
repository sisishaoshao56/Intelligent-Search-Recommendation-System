package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.dto.ItemScoreDTO;
import com.csu.demo.demo.mapper.EventMapper;
import com.csu.demo.demo.service.HotItemService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HotItemServiceImpl implements HotItemService {

    private static final String HOT_ITEMS_KEY = "hot:items";
    private static final int FALLBACK_HOURS = 24;
    private final StringRedisTemplate stringRedisTemplate;
    private final EventMapper eventMapper;

    public HotItemServiceImpl(StringRedisTemplate stringRedisTemplate, EventMapper eventMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.eventMapper = eventMapper;
    }

    @Override
    public void increaseScore(Long itemId, double delta) {
        stringRedisTemplate.opsForZSet()
            .incrementScore(HOT_ITEMS_KEY, String.valueOf(itemId), delta);
    }

    @Override
    public List<ItemScoreDTO> listTopItems(int limit) {
        ZSetOperations<String, String> ops = stringRedisTemplate.opsForZSet();
        List<ZSetOperations.TypedTuple<String>> tuples = new ArrayList<>(
                Objects.requireNonNullElse(
                        ops.reverseRangeWithScores(HOT_ITEMS_KEY, 0, Math.max(limit - 1, 0)),
                        Collections.<ZSetOperations.TypedTuple<String>>emptySet()
                )
        );
        if (tuples.isEmpty()) {
            List<ItemScoreDTO> fromDb = eventMapper.selectHotItemScores(FALLBACK_HOURS, limit);
            fromDb.forEach(dto ->
                    ops.add(HOT_ITEMS_KEY, dto.getItemId().toString(), dto.getTotalScore()));
            return fromDb;
        }
        return tuples.stream()
            .map(tuple -> {
                ItemScoreDTO dto = new ItemScoreDTO();
                dto.setItemId(Integer.valueOf(tuple.getValue()));
                dto.setTotalScore(tuple.getScore());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
