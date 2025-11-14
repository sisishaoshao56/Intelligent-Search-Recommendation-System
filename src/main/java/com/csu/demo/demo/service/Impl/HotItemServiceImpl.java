package com.csu.demo.demo.service.Impl;

import com.csu.demo.demo.dto.ItemScoreDTO;
import com.csu.demo.demo.service.HotItemService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HotItemServiceImpl implements HotItemService {

    private static final String HOT_ITEMS_KEY = "hot:items";
    private final StringRedisTemplate stringRedisTemplate;

    public HotItemServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void increaseScore(Long itemId, double delta) {
        stringRedisTemplate.opsForZSet()
            .incrementScore(HOT_ITEMS_KEY, String.valueOf(itemId), delta);
    }

    @Override
    public List<ItemScoreDTO> listTopItems(int limit) {
        ZSetOperations<String, String> ops = stringRedisTemplate.opsForZSet();
        return Objects.requireNonNullElse(
                ops.reverseRangeWithScores(HOT_ITEMS_KEY, 0, Math.max(limit - 1, 0)),
                List.<ZSetOperations.TypedTuple<String>>of()
            ).stream()
            .map(tuple -> {
                ItemScoreDTO dto = new ItemScoreDTO();
                dto.setItemId(Integer.valueOf(tuple.getValue()));
                dto.setTotalScore(tuple.getScore());
                return dto;
            })
            .collect(Collectors.toList());
    }
}
